package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Reservation;
import aulateca.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationView extends JDialog {

    private JTable tablaReservas;
    private DefaultTableModel modeloTabla;
    private final User usuarioLogueado;
    private final GenericDAO<Reservation> reservationDAO = new GenericDAO<>();

    public ReservationView(Frame parent, User usuarioLogueado) {
        super(parent, "Gestión de Reservas", true);
        this.usuarioLogueado = usuarioLogueado;
        setSize(800, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        // montamos el panel superior con el boton que abre el wizard de nueva reserva
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnNuevaReserva = new JButton("+ Iniciar Nueva Reserva");

        // aplicamos el estilo plano desvinculando la ui del sistema
        btnNuevaReserva.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnNuevaReserva.setFont(new Font("Arial", Font.BOLD, 14));
        btnNuevaReserva.setBackground(new Color(0, 123, 255));
        btnNuevaReserva.setForeground(Color.WHITE);
        btnNuevaReserva.setFocusPainted(false);
        btnNuevaReserva.setBorderPainted(false);
        btnNuevaReserva.setOpaque(true);
        btnNuevaReserva.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnNuevaReserva.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnNuevaReserva.addActionListener(e -> {
            ReservationWizardView wizard = new ReservationWizardView((Frame) this.getParent(), usuarioLogueado);
            wizard.setVisible(true);
            // refrescamos la tabla automaticamente cuando cierran el asistente
            cargarDatosEnTabla();
        });

        panelSuperior.add(btnNuevaReserva);
        add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout(5, 5));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // añadimos un titulo descriptivo para que quede claro que estamos viendo
        JLabel lblTituloTabla = new JLabel("Mis Próximas Reservas");
        lblTituloTabla.setFont(new Font("Arial", Font.BOLD, 16));
        lblTituloTabla.setForeground(new Color(50, 50, 50));
        panelCentral.add(lblTituloTabla, BorderLayout.NORTH);

        // omitimos la columna de usuario porque el alumno ya sabe quien es y bloqueamos la edicion
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Recurso", "Fecha", "Franja", "Observaciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaReservas = new JTable(modeloTabla);
        // le damos un poco mas de altura a las filas para que no se vea el texto ahogado
        tablaReservas.setRowHeight(25);

        // encogemos la columna del id a cero pixeles para ocultarla pero poder usarla por debajo
        tablaReservas.getColumnModel().getColumn(0).setMinWidth(0);
        tablaReservas.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaReservas.getColumnModel().getColumn(0).setWidth(0);

        panelCentral.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        JButton btnCancelarReserva = new JButton("Cancelar Reserva Seleccionada");

        // aplicamos el mismo diseño plano para el boton rojo de borrar
        btnCancelarReserva.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnCancelarReserva.setBackground(new Color(220, 53, 69));
        btnCancelarReserva.setForeground(Color.WHITE);
        btnCancelarReserva.setFont(new Font("Arial", Font.BOLD, 13));
        btnCancelarReserva.setFocusPainted(false);
        btnCancelarReserva.setBorderPainted(false);
        btnCancelarReserva.setOpaque(true);
        btnCancelarReserva.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCancelarReserva.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnCancelarReserva.addActionListener(e -> {
            int fila = tablaReservas.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona una reserva para cancelar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // rescatamos el id de la fila seleccionada usando la columna invisible que preparamos antes
            Integer id = (Integer) modeloTabla.getValueAt(fila, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Seguro que quieres cancelar esta reserva?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    reservationDAO.delete(reservationDAO.findById(Reservation.class, id));
                    cargarDatosEnTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al cancelar la reserva.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panelInferior.add(btnCancelarReserva);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarDatosEnTabla() {
        modeloTabla.setRowCount(0);
        LocalDate hoy = LocalDate.now();

        // metemos la consulta en un bloque seguro para evitar cierres inesperados como pide la rubrica
        try {
            List<Reservation> reservas = reservationDAO.findAll(Reservation.class);

            for (Reservation r : reservas) {
                // filtramos para que solo salgan las del usuario activo que sean de hoy en adelante
                if (r.getUser().getId().equals(usuarioLogueado.getId()) && !r.getDate().isBefore(hoy)) {

                    modeloTabla.addRow(new Object[]{
                            r.getId(),
                            r.getResource().getName(),
                            r.getDate().toString(),
                            r.getTimeSlot().getName(),
                            r.getObservations()
                    });
                }
            }
        } catch (Exception ex) {
            // capturamos el fallo de persistencia y avisamos de forma amigable
            JOptionPane.showMessageDialog(this, "error al cargar las reservas desde el servidor", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }
    }
}