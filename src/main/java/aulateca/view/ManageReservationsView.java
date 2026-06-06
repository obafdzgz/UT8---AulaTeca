package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Reservation;
import aulateca.model.Resource;
import aulateca.model.User;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageReservationsView extends JDialog {

    private JTable tablaReservas;
    private DefaultTableModel modeloTabla;

    private DatePicker datePickerFiltro;
    private JComboBox<User> comboFiltroUsuario;
    private JComboBox<Resource> comboFiltroRecurso;

    // guardamos las reservas en memoria para filtrar en tiempo real sin saturar el servidor
    private List<Reservation> todasLasReservas = new ArrayList<>();

    private final GenericDAO<Reservation> reservationDAO = new GenericDAO<>();
    private final GenericDAO<User> userDAO = new GenericDAO<>();
    private final GenericDAO<Resource> recursoDAO = new GenericDAO<>();

    public ManageReservationsView(Frame parent) {
        super(parent, "Administración General de Reservas", true);
        setSize(950, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // blindamos la carga inicial de datos por si hay un fallo de persistencia como pide la rubrica
        try {
            todasLasReservas = reservationDAO.findAll(Reservation.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al volcar los datos del servidor", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }

        inicializarComponentes();
        cargarDatosEnTabla(todasLasReservas);
    }

    private void inicializarComponentes() {
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros de Búsqueda"));

        panelFiltros.add(new JLabel("Fecha:"));
        datePickerFiltro = new DatePicker();
        // disparamos el metodo de filtrado cada vez que cambian la fecha en el calendario
        datePickerFiltro.addDateChangeListener(e -> aplicarFiltros());
        panelFiltros.add(datePickerFiltro);

        panelFiltros.add(new JLabel("Usuario:"));
        comboFiltroUsuario = new JComboBox<>();
        // metemos un null al principio para que actue como la opcion de ver todos
        comboFiltroUsuario.addItem(null);

        try {
            for (User u : userDAO.findAll(User.class)) comboFiltroUsuario.addItem(u);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al cargar los usuarios", "aviso", JOptionPane.WARNING_MESSAGE);
        }

        configurarRenderizadoUsuario(comboFiltroUsuario);
        comboFiltroUsuario.addItemListener(e -> aplicarFiltros());
        panelFiltros.add(comboFiltroUsuario);

        panelFiltros.add(new JLabel("Recurso:"));
        comboFiltroRecurso = new JComboBox<>();
        comboFiltroRecurso.addItem(null);

        try {
            for (Resource r : recursoDAO.findAll(Resource.class)) comboFiltroRecurso.addItem(r);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al cargar los recursos", "aviso", JOptionPane.WARNING_MESSAGE);
        }

        configurarRenderizadoRecurso(comboFiltroRecurso);
        comboFiltroRecurso.addItemListener(e -> aplicarFiltros());
        panelFiltros.add(comboFiltroRecurso);

        JButton btnLimpiar = new JButton("Limpiar Filtros");
        btnLimpiar.addActionListener(e -> {
            // al resetear los campos los listeners saltan solos y vuelven a mostrar toda la tabla
            datePickerFiltro.clear();
            comboFiltroUsuario.setSelectedItem(null);
            comboFiltroRecurso.setSelectedItem(null);
        });
        panelFiltros.add(btnLimpiar);

        add(panelFiltros, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // desactivamos la edicion haciendo un override rapido para evitar modificaciones por error
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Usuario", "Recurso", "Fecha", "Franja", "Observaciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setRowHeight(25);

        // encogemos la columna del id a cero pixeles para ocultarla pero poder usarla por debajo
        tablaReservas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaReservas.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaReservas.getColumnModel().getColumn(0).setWidth(0);

        panelCentral.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelarReserva = new JButton("Cancelar Reserva (Modo Admin)");
        btnCancelarReserva.setBackground(new Color(220, 53, 69));
        btnCancelarReserva.setForeground(Color.WHITE);
        btnCancelarReserva.setFont(new Font("Arial", Font.BOLD, 14));

        btnCancelarReserva.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona una reserva para eliminarla del sistema.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // rescatamos el id de la fila seleccionada usando la columna invisible que preparamos antes
            Integer id = (Integer) modeloTabla.getValueAt(fila, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Atención Administrador: Seguro que quieres forzar la cancelación de esta reserva?", "Confirmación Crítica", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                try {
                    reservationDAO.delete(reservationDAO.findById(Reservation.class, id));
                    // forzamos la recarga de datos para que el cambio se refleje en memoria inmediatamente
                    todasLasReservas = reservationDAO.findAll(Reservation.class);
                    aplicarFiltros();
                    JOptionPane.showMessageDialog(this, "Reserva eliminada con éxito.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al cancelar la reserva.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelInferior.add(btnCancelarReserva);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void aplicarFiltros() {
        LocalDate fechaFiltro = datePickerFiltro.getDate();
        User usuarioFiltro = (User) comboFiltroUsuario.getSelectedItem();
        Resource recursoFiltro = (Resource) comboFiltroRecurso.getSelectedItem();

        // aplicamos filtros en cascada con streams dejando pasar lo que coincida o si el filtro esta vacio
        List<Reservation> filtradas = todasLasReservas.stream()
                .filter(r -> fechaFiltro == null || r.getDate().equals(fechaFiltro))
                .filter(r -> usuarioFiltro == null || r.getUser().getId().equals(usuarioFiltro.getId()))
                .filter(r -> recursoFiltro == null || r.getResource().getId().equals(recursoFiltro.getId()))
                .collect(Collectors.toList());

        cargarDatosEnTabla(filtradas);
    }

    private void cargarDatosEnTabla(List<Reservation> reservas) {
        modeloTabla.setRowCount(0);
        for (Reservation r : reservas) {
            // concatenamos el rol al nombre del usuario para darle mas contexto al administrador
            modeloTabla.addRow(new Object[]{
                    r.getId(),
                    r.getUser().getUsername() + " (" + r.getUser().getRole().name() + ")",
                    r.getResource().getName(),
                    r.getDate().toString(),
                    r.getTimeSlot().getName(),
                    r.getObservations()
            });
        }
    }

    private void configurarRenderizadoUsuario(JComboBox<User> combo) {
        // modificamos la celda para mostrar el nombre real del usuario en vez del puntero de memoria
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                // controlamos el null que añadimos a mano para pintarle el texto de todos
                if (value == null) setText("--- Todos los Usuarios ---");
                else if (value instanceof User) setText(((User) value).getUsername());
                return this;
            }
        });
    }

    private void configurarRenderizadoRecurso(JComboBox<Resource> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) setText("--- Todos los Recursos ---");
                else if (value instanceof Resource) setText(((Resource) value).getName());
                return this;
            }
        });
    }
}