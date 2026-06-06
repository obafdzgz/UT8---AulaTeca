package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.TimeSlot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TimeSlotView extends JDialog {

    private JTable tablaFranjas;
    private DefaultTableModel modeloTabla;
    private JTextField txtNombre;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;

    // instanciamos el dao generico para comunicar con la base de datos
    private final GenericDAO<TimeSlot> dao = new GenericDAO<>();

    public TimeSlotView(Frame parent) {
        super(parent, "Gestión de Franjas Horarias", true);
        setSize(550, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        // montamos el formulario con gridbag para controlar mejor las alineaciones
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Añadir Franja Horaria"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre (Ej: 1ª Hora):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        txtNombre = new JTextField(15);
        panelFormulario.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Hora Inicio (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtHoraInicio = new JTextField(15);
        panelFormulario.add(txtHoraInicio, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Hora Fin (HH:MM):"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtHoraFin = new JTextField(15);
        panelFormulario.add(txtHoraFin, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        JButton btnGuardar = new JButton("Guardar Franja");
        // aplicamos el estilo plano que estamos usando en toda la app
        btnGuardar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelFormulario.add(btnGuardar, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        // tabla de solo lectura
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Inicio", "Fin"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaFranjas = new JTable(modeloTabla);
        tablaFranjas.setRowHeight(25);

        // ocultamos el id visualmente para que la tabla sea mas limpia
        tablaFranjas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaFranjas.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaFranjas.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(tablaFranjas), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBorrar = new JButton("Borrar Seleccionada");
        // aplicamos el estilo plano al boton rojo de borrar
        btnBorrar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnBorrar.setBackground(new Color(220, 53, 69));
        btnBorrar.setForeground(Color.WHITE);
        btnBorrar.setFocusPainted(false);
        btnBorrar.setBorderPainted(false);
        btnBorrar.setOpaque(true);
        btnBorrar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnBorrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelInferior.add(btnBorrar);
        add(panelInferior, BorderLayout.SOUTH);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText().trim();
            String strInicio = txtHoraInicio.getText().trim();
            String strFin = txtHoraFin.getText().trim();

            if (nombre.isEmpty() || strInicio.isEmpty() || strFin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Rellena todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // parseamos los string a objetos localtime propios de java 8
                LocalTime horaInicio = LocalTime.parse(strInicio);
                LocalTime horaFin = LocalTime.parse(strFin);

                if (horaInicio.isAfter(horaFin)) {
                    JOptionPane.showMessageDialog(this, "La hora de inicio no puede ser mayor que la de fin.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                dao.save(new TimeSlot(nombre, horaInicio, horaFin));

                txtNombre.setText("");
                txtHoraInicio.setText("");
                txtHoraFin.setText("");
                cargarDatosEnTabla();

            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato de hora inválido. Usa HH:MM (Ej: 08:30).", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar la franja.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBorrar.addActionListener(e -> {
            int fila = tablaFranjas.getSelectedRow();
            if (fila == -1) return;

            Integer id = (Integer) modeloTabla.getValueAt(fila, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Borrar esta franja horaria?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.delete(dao.findById(TimeSlot.class, id));
                    cargarDatosEnTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "No se puede borrar. Hay reservas usando esta franja.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void cargarDatosEnTabla() {
        modeloTabla.setRowCount(0);

        // protegemos la lectura masiva de la bd ante posibles desconexiones
        try {
            List<TimeSlot> lista = dao.findAll(TimeSlot.class);
            for (TimeSlot franja : lista) {
                modeloTabla.addRow(new Object[]{
                        franja.getId(),
                        franja.getName(),
                        franja.getStartTime().toString(),
                        franja.getEndTime().toString()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error al cargar franjas desde el servidor", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }
    }
}