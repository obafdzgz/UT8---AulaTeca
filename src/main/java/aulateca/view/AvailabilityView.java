package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Reservation;
import aulateca.model.Resource;
import aulateca.model.TimeSlot;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class AvailabilityView extends JDialog {

    private DatePicker datePickerFecha;
    private JComboBox<Resource> comboRecurso;
    private JTable tablaDisponibilidad;
    private DefaultTableModel modeloTabla;

    private final GenericDAO<Resource> recursoDAO = new GenericDAO<>();
    private final GenericDAO<TimeSlot> franjaDAO = new GenericDAO<>();
    private final GenericDAO<Reservation> reservationDAO = new GenericDAO<>();

    public AvailabilityView(Frame parent) {
        super(parent, "Consultar Disponibilidad", true);

        // ampliamos un poco las medidas de la ventana para que los filtros tengan espacio
        setSize(850, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDesplegables();
    }

    private void inicializarComponentes() {
        // usamos gridbaglayout para asegurarnos de que la tabla no aplasta los componentes al cambiar el tamano
        JPanel panelBuscador = new JPanel(new GridBagLayout());
        panelBuscador.setBorder(BorderFactory.createTitledBorder("Criterios de Búsqueda"));

        // configuramos la cuadricula para que los elementos se expandan horizontalmente
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelBuscador.add(new JLabel("Fecha:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        datePickerFecha = new DatePicker();

        // le pasamos una politica de veto al calendario para desactivar dias concretos
        datePickerFecha.getSettings().setVetoPolicy(new DateVetoPolicy() {
            @Override
            public boolean isDateAllowed(LocalDate date) {
                // cortamos por lo sano si la fecha es anterior a hoy
                if (date.isBefore(LocalDate.now())) return false;
                // bloqueamos sabados y domingos porque el centro estara cerrado
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) return false;
                return true;
            }
        });

        // pillamos la fecha actual para ponerla por defecto
        LocalDate fechaSugerida = LocalDate.now();
        // si resulta que hoy es fin de semana le vamos sumando dias hasta caer en lunes
        while (fechaSugerida.getDayOfWeek() == DayOfWeek.SATURDAY || fechaSugerida.getDayOfWeek() == DayOfWeek.SUNDAY) {
            fechaSugerida = fechaSugerida.plusDays(1);
        }
        datePickerFecha.setDate(fechaSugerida);
        panelBuscador.add(datePickerFecha, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        // metemos un margen izquierdo mas grande aqui para separar el bloque de recurso del bloque de fecha
        gbc.insets = new Insets(10, 25, 10, 10);
        panelBuscador.add(new JLabel("Recurso:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        // restauramos los margenes normales para el selector
        gbc.insets = new Insets(10, 10, 10, 10);
        comboRecurso = new JComboBox<>();
        configurarRenderizadoRecurso(comboRecurso);
        panelBuscador.add(comboRecurso, gbc);

        gbc.gridx = 4; gbc.gridy = 0;
        gbc.insets = new Insets(10, 25, 10, 10);

        JButton btnBuscar = new JButton("Buscar Huecos Libres");

        // desvinculamos el estilo nativo de windows o mac para que el boton respete los colores personalizados que le pasamos abajo
        btnBuscar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnBuscar.setBackground(new Color(0, 123, 255));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setFont(new Font("Arial", Font.BOLD, 13));

        // matamos los efectos feos que trae java por defecto al hacer click
        btnBuscar.setFocusPainted(false);
        btnBuscar.setBorderPainted(false);
        btnBuscar.setOpaque(true);
        // le damos padding interno para que quede mas ancho y no tan pegado al texto
        btnBuscar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnBuscar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBuscador.add(btnBuscar, gbc);

        add(panelBuscador, BorderLayout.NORTH);

        // creamos el modelo sobreescribiendo el iscelleditable para que la tabla sea de solo lectura
        modeloTabla = new DefaultTableModel(new String[]{"Franja Horaria", "Horario", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaDisponibilidad = new JTable(modeloTabla);

        tablaDisponibilidad.setRowHeight(30);
        aplicarColoresATabla();

        add(new JScrollPane(tablaDisponibilidad), BorderLayout.CENTER);

        // disparamos la funcion de busqueda cuando le den al boton
        btnBuscar.addActionListener(e -> buscarDisponibilidad());
    }

    private void buscarDisponibilidad() {
        LocalDate fechaSeleccionada = datePickerFecha.getDate();
        Resource recursoSeleccionado = (Resource) comboRecurso.getSelectedItem();

        // control basico por si le dan a buscar sin haber elegido nada
        if (fechaSeleccionada == null || recursoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una fecha y un recurso.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // vaciamos las filas que hubiera de una busqueda anterior
        modeloTabla.setRowCount(0);

        // envolvemos la lectura masiva en un bloque de seguridad
        try {
            List<TimeSlot> todasLasFranjas = franjaDAO.findAll(TimeSlot.class);
            List<Reservation> todasLasReservas = reservationDAO.findAll(Reservation.class);

            // recorremos todos los tramos horarios para ir construyendo la vista de ese dia
            for (TimeSlot franja : todasLasFranjas) {
                String estado = "LIBRE";

                // revisamos las reservas a ver si alguna pisa justo este hueco
                for (Reservation r : todasLasReservas) {
                    if (r.getResource().getId().equals(recursoSeleccionado.getId()) &&
                            r.getDate().equals(fechaSeleccionada) &&
                            r.getTimeSlot().getId().equals(franja.getId())) {

                        estado = "OCUPADO (por " + r.getUser().getUsername() + ")";
                        break;
                    }
                }

                modeloTabla.addRow(new Object[]{
                        franja.getName(),
                        franja.getStartTime() + " - " + franja.getEndTime(),
                        estado
                });
            }
        } catch (Exception ex) {
            // si peta la consulta paramos la ejecucion y sacamos el mensaje
            JOptionPane.showMessageDialog(this, "error al consultar el cuadrante", "fallo de base de datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDesplegables() {
        try {
            // intentamos traer los recursos de la base de datos para rellenar el combo
            for (Resource r : recursoDAO.findAll(Resource.class)) {
                comboRecurso.addItem(r);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error al cargar la lista de recursos", "error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarRenderizadoRecurso(JComboBox<Resource> combo) {
        // metemos un renderizador para evitar que en el desplegable salga el id de memoria del objeto
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // extraemos el nombre del recurso para mostrar texto legible en lugar de la referencia
                if (value instanceof Resource) setText(((Resource) value).getName());
                return this;
            }
        });
    }

    private void aplicarColoresATabla() {
        // aplicamos el cambio de color unicamente a la columna que indica el estado
        tablaDisponibilidad.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String estado = (String) value;

                // aplicamos verde o rojo de forma dinamica segun el string que traiga la celda
                if ("LIBRE".equals(estado)) {
                    c.setForeground(new Color(40, 167, 69));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(220, 53, 69));
                    c.setFont(c.getFont().deriveFont(Font.PLAIN));
                }
                return c;
            }
        });
    }
}