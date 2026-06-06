package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Reservation;
import aulateca.model.User;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateHighlightPolicy;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.HighlightInformation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HistoryView extends JDialog {

    private final User usuarioLogueado;

    private DatePicker datePickerFecha;
    private JTable tablaHistorial;
    private DefaultTableModel modeloTabla;

    // guardamos las reservas en memoria para no machacar la base de datos con consultas repetidas
    private final List<Reservation> misReservas = new ArrayList<>();
    // separamos las fechas en un set para que el calendario las lea mas rapido al pintarse
    private final Set<LocalDate> misFechasConReserva = new HashSet<>();

    private final GenericDAO<Reservation> reservationDAO = new GenericDAO<>();

    public HistoryView(Frame parent, User usuarioLogueado) {
        super(parent, "Mi Historial de Reservas", true);
        this.usuarioLogueado = usuarioLogueado;
        setSize(750, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // volcamos la informacion al construir la ventana
        cargarMisDatos();

        // montamos la interfaz grafica
        inicializarComponentes();

        // por defecto mostramos las proximas reservas del usuario
        mostrarProximas5();
    }

    private void cargarMisDatos() {
        misReservas.clear();
        misFechasConReserva.clear();

        // metemos la consulta en un bloque try catch por si se cae la base de datos
        try {
            List<Reservation> todasLasReservas = reservationDAO.findAll(Reservation.class);
            for (Reservation r : todasLasReservas) {
                // filtramos para quedarnos unicamente con las reservas vinculadas a la sesion actual
                if (r.getUser().getId().equals(usuarioLogueado.getId())) {
                    misReservas.add(r);
                    misFechasConReserva.add(r.getDate());
                }
            }
        } catch (Exception ex) {
            // avisamos al usuario para que no se quede esperando si hay un fallo critico
            JOptionPane.showMessageDialog(this, "no se pudo conectar con la base de datos", "error critico", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void inicializarComponentes() {
        // montamos el panel superior con el buscador y los botones rapidos
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Filtrar mi historial"));

        panelSuperior.add(new JLabel("Buscar por fecha concreta:"));
        datePickerFecha = new DatePicker();
        configurarCalendario();
        panelSuperior.add(datePickerFecha);

        JButton btnUltimas5 = new JButton("Ver mis últimas 5 completadas");
        btnUltimas5.setBackground(new Color(108, 117, 125));
        btnUltimas5.setForeground(Color.WHITE);

        JButton btnProximas5 = new JButton("Ver mis 5 próximas");
        btnProximas5.setBackground(new Color(0, 123, 255));
        btnProximas5.setForeground(Color.WHITE);

        // ponemos esto a modo de separador visual simple
        panelSuperior.add(new JLabel("   |   "));
        panelSuperior.add(btnUltimas5);
        panelSuperior.add(btnProximas5);

        add(panelSuperior, BorderLayout.NORTH);

        // configuramos el modelo de la tabla bloqueando la edicion de las celdas
        modeloTabla = new DefaultTableModel(new String[]{"Estado", "Fecha", "Franja", "Recurso", "Observaciones"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaHistorial = new JTable(modeloTabla);
        tablaHistorial.setRowHeight(25);
        add(new JScrollPane(tablaHistorial), BorderLayout.CENTER);

        // lanzamos la busqueda especifica cuando eligen un dia concreto en el componente
        datePickerFecha.addDateChangeListener(event -> {
            LocalDate fechaElegida = datePickerFecha.getDate();
            if (fechaElegida != null) {
                mostrarPorFecha(fechaElegida);
            }
        });

        btnUltimas5.addActionListener(e -> {
            // borramos la seleccion del calendario para que no confunda con la tabla
            datePickerFecha.clear();
            mostrarUltimas5();
        });

        btnProximas5.addActionListener(e -> {
            datePickerFecha.clear();
            mostrarProximas5();
        });
    }

    // ajustamos las politicas del calendario para que solo se puedan clickear dias con reserva
    private void configurarCalendario() {
        // capamos la seleccion de cualquier fecha que no este registrada en nuestro set
        datePickerFecha.getSettings().setVetoPolicy(new DateVetoPolicy() {
            @Override
            public boolean isDateAllowed(LocalDate date) {
                return misFechasConReserva.contains(date);
            }
        });

        // resaltamos con color los dias que coinciden con las fechas almacenadas
        datePickerFecha.getSettings().setHighlightPolicy(new DateHighlightPolicy() {
            @Override
            public HighlightInformation getHighlightInformationOrNull(LocalDate date) {
                if (misFechasConReserva.contains(date)) {
                    return new HighlightInformation(new Color(0, 123, 255), Color.WHITE, "¡Tienes reservas este día!");
                }
                // si devolvemos null la libreria aplica el color gris por defecto
                return null;
            }
        });
    }

    private void mostrarProximas5() {
        LocalDate hoy = LocalDate.now();
        // tiramos de streams para filtrar y ordenar la lista entera en pocas lineas
        List<Reservation> futuras = misReservas.stream()
                // descartamos el historico viejo
                .filter(r -> !r.getDate().isBefore(hoy))
                // ordenamos de forma ascendente segun la fecha
                .sorted(Comparator.comparing(Reservation::getDate))
                // truncamos la lista a 5 elementos maximo
                .limit(5)
                .collect(Collectors.toList());

        llenarTabla(futuras);
    }

    private void mostrarUltimas5() {
        LocalDate hoy = LocalDate.now();
        List<Reservation> pasadas = misReservas.stream()
                .filter(r -> r.getDate().isBefore(hoy))
                // le damos la vuelta al orden para ver lo ultimo que se hizo primero
                .sorted(Comparator.comparing(Reservation::getDate).reversed())
                .limit(5)
                .collect(Collectors.toList());

        llenarTabla(pasadas);
    }

    private void mostrarPorFecha(LocalDate fecha) {
        List<Reservation> deEsteDia = misReservas.stream()
                .filter(r -> r.getDate().equals(fecha))
                .collect(Collectors.toList());

        llenarTabla(deEsteDia);
    }

    private void llenarTabla(List<Reservation> lista) {
        modeloTabla.setRowCount(0);
        LocalDate hoy = LocalDate.now();

        for (Reservation r : lista) {
            // calculamos el estado con un operador ternario para poner un texto amigable
            String estadoVisual = r.getDate().isBefore(hoy) ? "✅ COMPLETADA" : "⏳ PENDIENTE";

            modeloTabla.addRow(new Object[]{
                    estadoVisual,
                    r.getDate().toString(),
                    r.getTimeSlot().getName(),
                    r.getResource().getName(),
                    r.getObservations()
            });
        }
    }
}