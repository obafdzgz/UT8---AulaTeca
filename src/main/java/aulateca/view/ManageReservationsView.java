package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Reservation;
import aulateca.model.Resource;
import com.github.lgooddatepicker.components.DatePicker;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private JComboBox<Resource> comboFiltroRecurso;
    private JTextField txtBuscador;

    private List<Reservation> todasLasReservas = new ArrayList<>();
    private final GenericDAO<Reservation> reservationDAO = new GenericDAO<>();
    private final GenericDAO<Resource> recursoDAO = new GenericDAO<>();

    public ManageReservationsView(Frame parent) {
        super(parent, "Administración General de Reservas", true);
        // ventana un poco más alta para evitar superposición
        setSize(1050, 680);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        try {
            todasLasReservas = reservationDAO.findAll(Reservation.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al volcar los datos del servidor", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }

        inicializarComponentes();
        cargarDatosEnTabla(todasLasReservas);
    }

    private void inicializarComponentes() {
        // --- 1. ZONA SUPERIOR: Filtros ---
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros y Búsqueda Rápida"));

        panelFiltros.add(new JLabel("Buscador:"));
        txtBuscador = new JTextField(15);
        txtBuscador.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        txtBuscador.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        panelFiltros.add(txtBuscador);

        panelFiltros.add(new JLabel("Fecha:"));
        datePickerFiltro = new DatePicker();
        datePickerFiltro.addDateChangeListener(e -> aplicarFiltros());
        panelFiltros.add(datePickerFiltro);

        panelFiltros.add(new JLabel("Recurso:"));
        comboFiltroRecurso = new JComboBox<>();
        comboFiltroRecurso.addItem(null);
        try {
            for (Resource r : recursoDAO.findAll(Resource.class)) comboFiltroRecurso.addItem(r);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al cargar recursos", "aviso", JOptionPane.WARNING_MESSAGE);
        }
        configurarRenderizadoRecurso(comboFiltroRecurso);
        comboFiltroRecurso.addItemListener(e -> aplicarFiltros());
        panelFiltros.add(comboFiltroRecurso);

        JButton btnLimpiar = new JButton("Limpiar Filtros");
        btnLimpiar.addActionListener(e -> {
            datePickerFiltro.clear();
            comboFiltroRecurso.setSelectedItem(null);
            txtBuscador.setText("");
        });
        panelFiltros.add(btnLimpiar);

        add(panelFiltros, BorderLayout.NORTH);

        // --- 2. ZONA CENTRAL: Tabla ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Usuario", "Recurso", "Fecha", "Franja", "Observaciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setRowHeight(25);

        tablaReservas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaReservas.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaReservas.getColumnModel().getColumn(0).setWidth(0);

        panelCentral.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // --- 3. ZONA INFERIOR: Botón con margen especial ---
        // el EmptyBorder superior (20) evita que el boton choque con la tabla
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

        JButton btnCancelarReserva = new JButton("Cancelar Reserva (Modo Admin)");
        aplicarEstiloBotonRojo(btnCancelarReserva);

        btnCancelarReserva.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona una reserva para eliminarla.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Integer id = (Integer) modeloTabla.getValueAt(fila, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Seguro que quieres forzar la cancelación?", "Confirmación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    reservationDAO.delete(reservationDAO.findById(Reservation.class, id));
                    todasLasReservas = reservationDAO.findAll(Reservation.class);
                    aplicarFiltros();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al cancelar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelInferior.add(btnCancelarReserva);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void aplicarEstiloBotonRojo(JButton btn) {
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(new Color(220, 53, 69));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void aplicarFiltros() {
        LocalDate fechaFiltro = datePickerFiltro.getDate();
        Resource recursoFiltro = (Resource) comboFiltroRecurso.getSelectedItem();
        String textoBusqueda = txtBuscador.getText().trim().toLowerCase();

        List<Reservation> filtradas = todasLasReservas.stream()
                .filter(r -> fechaFiltro == null || r.getDate().equals(fechaFiltro))
                .filter(r -> recursoFiltro == null || r.getResource().getId().equals(recursoFiltro.getId()))
                .filter(r -> textoBusqueda.isEmpty() ||
                        r.getUser().getUsername().toLowerCase().contains(textoBusqueda) ||
                        (r.getUser().getFullName() != null && r.getUser().getFullName().toLowerCase().contains(textoBusqueda)) ||
                        r.getResource().getName().toLowerCase().contains(textoBusqueda) ||
                        (r.getObservations() != null && r.getObservations().toLowerCase().contains(textoBusqueda)))
                .collect(Collectors.toList());

        cargarDatosEnTabla(filtradas);
    }

    private void cargarDatosEnTabla(List<Reservation> reservas) {
        modeloTabla.setRowCount(0);
        for (Reservation r : reservas) {
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