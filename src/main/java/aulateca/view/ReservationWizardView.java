package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Reservation;
import aulateca.model.Resource;
import aulateca.model.ResourceType;
import aulateca.model.TimeSlot;
import aulateca.model.User;
import aulateca.service.ReservationService;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.DateHighlightPolicy;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.HighlightInformation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReservationWizardView extends JDialog {

    private final User usuarioLogueado;

    // usamos cardlayout para poder cambiar de panel como si fuera un asistente sin abrir ventanas nuevas
    private CardLayout cardLayout;
    private JPanel panelContenedor;

    private JPanel panelPaso1;
    private JPanel panelPaso2;
    private JPanel panelPaso3;

    private ResourceType categoriaSeleccionada;
    private Resource recursoSeleccionado;

    private DatePicker datePickerFecha;
    private JComboBox<TimeSlot> comboFranja;
    private JTextArea txtObservaciones;
    private JButton btnConfirmar;

    // guardamos las franjas y dias ocupados en memoria para pintar el calendario rapido
    private List<Integer> franjasOcupadasIds = new ArrayList<>();
    private Set<LocalDate> diasTotalmenteOcupados = new HashSet<>();

    private final ReservationService reservationService = new ReservationService();
    private final GenericDAO<ResourceType> tipoDAO = new GenericDAO<>();
    private final GenericDAO<Resource> recursoDAO = new GenericDAO<>();
    private final GenericDAO<TimeSlot> franjaDAO = new GenericDAO<>();
    private final GenericDAO<Reservation> reservationDAO = new GenericDAO<>();

    public ReservationWizardView(Frame parent, User usuarioLogueado) {
        super(parent, "Nueva Reserva - Asistente", true);
        this.usuarioLogueado = usuarioLogueado;
        setSize(600, 450);
        setLocationRelativeTo(parent);

        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);

        inicializarPaso1();
        inicializarPaso2();
        inicializarPaso3();

        // asignamos un string clave a cada panel para poder saltar de uno a otro facilmente
        panelContenedor.add(panelPaso1, "PASO_1");
        panelContenedor.add(panelPaso2, "PASO_2");
        panelContenedor.add(panelPaso3, "PASO_3");

        add(panelContenedor);
    }

    private void inicializarPaso1() {
        panelPaso1 = new JPanel(new BorderLayout(10, 10));
        panelPaso1.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("Paso 1: ¿Qué tipo de recurso necesitas?", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelPaso1.add(titulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(0, 2, 15, 15));

        // blindamos la lectura de tipos de recurso por si la base de datos falla
        List<ResourceType> categorias = new ArrayList<>();
        try {
            categorias = tipoDAO.findAll(ResourceType.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al cargar las categorias", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }

        for (ResourceType tipo : categorias) {
            JButton btnCategoria = new JButton(tipo.getName());

            // aplicamos el diseño web rompiendo la UI de windows
            btnCategoria.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            btnCategoria.setFont(new Font("Arial", Font.BOLD, 16));
            btnCategoria.setBackground(new Color(240, 240, 240));
            btnCategoria.setForeground(new Color(50, 50, 50));
            btnCategoria.setFocusPainted(false);
            btnCategoria.setBorderPainted(false);
            btnCategoria.setOpaque(true);
            btnCategoria.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            btnCategoria.setCursor(new Cursor(Cursor.HAND_CURSOR));

            btnCategoria.addActionListener(e -> {
                categoriaSeleccionada = tipo;
                cargarRecursosEnPaso2();
                // le decimos al cardlayout que traiga el panel 2 al frente
                cardLayout.show(panelContenedor, "PASO_2");
            });

            panelBotones.add(btnCategoria);
        }

        panelPaso1.add(new JScrollPane(panelBotones), BorderLayout.CENTER);
    }

    private void inicializarPaso2() {
        panelPaso2 = new JPanel(new BorderLayout(10, 10));
        panelPaso2.setBorder(new EmptyBorder(20, 20, 20, 20));
    }

    private void cargarRecursosEnPaso2() {
        // vaciamos el panel por si vuelven hacia atras y eligen otra categoria distinta
        panelPaso2.removeAll();

        JLabel titulo = new JLabel("Paso 2: Elige un " + categoriaSeleccionada.getName() + " concreto", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        panelPaso2.add(titulo, BorderLayout.NORTH);

        JPanel panelBotones = new JPanel(new GridLayout(0, 1, 10, 10));

        // capturamos errores al volcar los recursos especificos
        List<Resource> todosRecursos = new ArrayList<>();
        try {
            todosRecursos = recursoDAO.findAll(Resource.class);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al volcar los recursos", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }

        boolean hayRecursos = false;

        for (Resource r : todosRecursos) {
            // solo pintamos botones para los recursos que cuadren con la categoria del paso 1
            if (r.getType().getId().equals(categoriaSeleccionada.getId())) {
                hayRecursos = true;

                String textoBoton = r.getName() + " (" + r.getStatus().getName() + ")";
                JButton btnRecurso = new JButton(textoBoton);

                btnRecurso.setUI(new javax.swing.plaf.basic.BasicButtonUI());
                btnRecurso.setFont(new Font("Arial", Font.BOLD, 14));
                btnRecurso.setFocusPainted(false);
                btnRecurso.setBorderPainted(false);
                btnRecurso.setOpaque(true);
                btnRecurso.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                btnRecurso.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // bloqueamos el click si el recurso esta roto o en mantenimiento
                if (!r.getStatus().getName().equalsIgnoreCase("OPERATIVO")) {
                    btnRecurso.setEnabled(false);
                    btnRecurso.setBackground(new Color(230, 230, 230));
                    btnRecurso.setForeground(Color.GRAY);
                    btnRecurso.setToolTipText("Este recurso no está operativo actualmente.");
                } else {
                    btnRecurso.setBackground(new Color(225, 245, 254));
                    btnRecurso.setForeground(new Color(30, 30, 30));
                }

                btnRecurso.addActionListener(e -> {
                    recursoSeleccionado = r;
                    actualizarTituloPaso3();

                    // calculamos los dias totalmente llenos antes de ir al paso 3 para pintar el calendario de rojo
                    calcularDiasTotalmenteOcupados();
                    aplicarPoliticasCalendario();

                    // nos saltamos los fines de semana y los dias rojos buscando el primer hueco libre real
                    LocalDate fechaSugerida = LocalDate.now();
                    while (fechaSugerida.getDayOfWeek() == DayOfWeek.SATURDAY || fechaSugerida.getDayOfWeek() == DayOfWeek.SUNDAY || diasTotalmenteOcupados.contains(fechaSugerida)) {
                        fechaSugerida = fechaSugerida.plusDays(1);
                    }
                    datePickerFecha.setDate(fechaSugerida);

                    actualizarDisponibilidadFranjas();
                    cardLayout.show(panelContenedor, "PASO_3");
                });

                panelBotones.add(btnRecurso);
            }
        }

        if (!hayRecursos) {
            panelBotones.add(new JLabel("No hay recursos registrados en esta categoría.", SwingConstants.CENTER));
        }

        panelPaso2.add(new JScrollPane(panelBotones), BorderLayout.CENTER);

        JButton btnVolver = new JButton("<< Volver a Categorías");
        btnVolver.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnVolver.setBackground(new Color(108, 117, 125));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setOpaque(true);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnVolver.addActionListener(e -> cardLayout.show(panelContenedor, "PASO_1"));
        panelPaso2.add(btnVolver, BorderLayout.SOUTH);

        // forzamos el repintado porque el contenido del panel es muy dinamico
        panelPaso2.revalidate();
        panelPaso2.repaint();
    }

    private void inicializarPaso3() {
        panelPaso3 = new JPanel(new BorderLayout(10, 10));
        panelPaso3.setBorder(new EmptyBorder(20, 20, 20, 20));

        panelPaso3.add(new JLabel("Paso 3: Detalles de la Reserva", SwingConstants.CENTER), BorderLayout.NORTH);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Fecha de reserva:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        datePickerFecha = new DatePicker();

        // metemos un listener para que si hacen trampa y clican un dia rojo el sistema les regañe
        datePickerFecha.addDateChangeListener(event -> {
            LocalDate fechaElegida = datePickerFecha.getDate();

            if (fechaElegida != null && diasTotalmenteOcupados.contains(fechaElegida)) {
                // metemos el dialog en el hilo de swing para que no se congele el calendario al abrirse
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "El día " + fechaElegida + " está totalmente lleno.\nElige otra fecha.", "Día Ocupado", JOptionPane.WARNING_MESSAGE);
                    datePickerFecha.clear();
                });
            } else {
                actualizarDisponibilidadFranjas();
            }
        });

        panelFormulario.add(datePickerFecha, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Franja Horaria:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        comboFranja = new JComboBox<>();

        // protegemos la extraccion de las franjas horarias base
        try {
            List<TimeSlot> franjas = franjaDAO.findAll(TimeSlot.class);
            for (TimeSlot f : franjas) comboFranja.addItem(f);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error al cargar horas", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }

        configurarRenderizadoFranja(comboFranja);

        comboFranja.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                TimeSlot franjaSel = (TimeSlot) comboFranja.getSelectedItem();
                // evitamos que seleccionen franjas ocupadas seteando el indice a menos uno para resetearlo
                if (franjaSel != null && franjasOcupadasIds.contains(franjaSel.getId())) {
                    SwingUtilities.invokeLater(() -> comboFranja.setSelectedIndex(-1));
                }
                comprobarBotonConfirmar();
            }
        });

        panelFormulario.add(comboFranja, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtObservaciones = new JTextArea(3, 20);
        panelFormulario.add(new JScrollPane(txtObservaciones), gbc);

        panelPaso3.add(panelFormulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnVolver = new JButton("<< Volver a Recursos");
        btnVolver.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnVolver.setBackground(new Color(108, 117, 125));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setOpaque(true);
        btnVolver.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnVolver.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVolver.addActionListener(e -> cardLayout.show(panelContenedor, "PASO_2"));

        btnConfirmar = new JButton("Confirmar Reserva");
        btnConfirmar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnConfirmar.setBackground(new Color(40, 167, 69));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFont(new Font("Arial", Font.BOLD, 14));
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setBorderPainted(false);
        btnConfirmar.setOpaque(true);
        btnConfirmar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> procesarReserva());

        panelBotones.add(btnVolver);
        panelBotones.add(btnConfirmar);
        panelPaso3.add(panelBotones, BorderLayout.SOUTH);
    }

    private void actualizarTituloPaso3() {
        BorderLayout layout = (BorderLayout) panelPaso3.getLayout();
        Component norte = layout.getLayoutComponent(BorderLayout.NORTH);
        if (norte instanceof JLabel) {
            ((JLabel) norte).setText("Paso 3: Reservando " + recursoSeleccionado.getName());
            ((JLabel) norte).setFont(new Font("Arial", Font.BOLD, 18));
        }
    }

    private void calcularDiasTotalmenteOcupados() {
        diasTotalmenteOcupados.clear();
        int totalFranjas = comboFranja.getItemCount();
        Map<LocalDate, Integer> conteoPorDia = new HashMap<>();

        // recuperamos todo para ver si algun dia se le han acabado las franjas
        try {
            List<Reservation> todasLasReservas = reservationDAO.findAll(Reservation.class);
            for (Reservation r : todasLasReservas) {
                if (r.getResource().getId().equals(recursoSeleccionado.getId())) {
                    LocalDate fecha = r.getDate();
                    // sumamos uno al contador de ese dia en el mapa
                    conteoPorDia.put(fecha, conteoPorDia.getOrDefault(fecha, 0) + 1);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "error analizando saturacion del calendario", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }

        // si el contador de ese dia llega al maximo de franjas posibles lo marcamos como totalmente bloqueado
        for (Map.Entry<LocalDate, Integer> entry : conteoPorDia.entrySet()) {
            if (entry.getValue() >= totalFranjas) {
                diasTotalmenteOcupados.add(entry.getKey());
            }
        }
    }

    private void aplicarPoliticasCalendario() {
        // capamos los dias anteriores a hoy y los fines de semana
        datePickerFecha.getSettings().setVetoPolicy(new DateVetoPolicy() {
            @Override
            public boolean isDateAllowed(LocalDate date) {
                if (date.isBefore(LocalDate.now())) return false;
                if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) return false;
                return true;
            }
        });

        // cruzamos con el set que acabamos de calcular para que esos dias salgan pintados de rojo
        datePickerFecha.getSettings().setHighlightPolicy(new DateHighlightPolicy() {
            @Override
            public HighlightInformation getHighlightInformationOrNull(LocalDate date) {
                if (diasTotalmenteOcupados.contains(date)) {
                    return new HighlightInformation(new Color(220, 53, 69), Color.WHITE, "Día completo");
                }
                return null;
            }
        });
    }

    private void actualizarDisponibilidadFranjas() {
        franjasOcupadasIds.clear();
        LocalDate fechaSeleccionada = datePickerFecha.getDate();

        if (fechaSeleccionada != null && recursoSeleccionado != null) {
            // metemos la consulta en bloque para que si falla no se cuelgue al cambiar de dia
            try {
                List<Reservation> todasLasReservas = reservationDAO.findAll(Reservation.class);

                for (Reservation r : todasLasReservas) {
                    // apuntamos que franjas concretas ya no estan libres para esta combinacion de dia y recurso
                    if (r.getDate().equals(fechaSeleccionada) && r.getResource().getId().equals(recursoSeleccionado.getId())) {
                        franjasOcupadasIds.add(r.getTimeSlot().getId());
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "error analizando horas libres", "fallo", JOptionPane.ERROR_MESSAGE);
            }
        }

        // disparamos el repintado del combobox para que el renderizador aplique los colores grises
        comboFranja.repaint();
        comprobarBotonConfirmar();
    }

    private void comprobarBotonConfirmar() {
        TimeSlot franjaSel = (TimeSlot) comboFranja.getSelectedItem();

        if (franjaSel == null || datePickerFecha.getDate() == null) {
            btnConfirmar.setEnabled(false);
            return;
        }

        // desactivamos el boton maestro si detectamos que intentan colar una franja marcada
        if (franjasOcupadasIds.contains(franjaSel.getId())) {
            btnConfirmar.setEnabled(false);
            btnConfirmar.setToolTipText("Esta franja ya está reservada.");
        } else {
            btnConfirmar.setEnabled(true);
            btnConfirmar.setToolTipText(null);
        }
    }

    private void configurarRenderizadoFranja(JComboBox<TimeSlot> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof TimeSlot) {
                    TimeSlot ts = (TimeSlot) value;
                    String textoBase = ts.getName() + " [" + ts.getStartTime() + " - " + ts.getEndTime() + "]";

                    // pintamos la letra de gris tenue y le metemos el string extra si su id esta en la lista negra
                    if (franjasOcupadasIds.contains(ts.getId())) {
                        setText(textoBase + " (OCUPADO)");
                        setForeground(Color.LIGHT_GRAY);
                        if (isSelected) setForeground(Color.GRAY);
                    } else {
                        setText(textoBase);
                        if (!isSelected) setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        });
    }

    private void procesarReserva() {
        if (datePickerFecha.getDate() == null || comboFranja.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Por favor, elige una fecha y una franja horaria.", "Faltan datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate fecha = datePickerFecha.getDate();
            TimeSlot franjaSel = (TimeSlot) comboFranja.getSelectedItem();

            Reservation nuevaReserva = new Reservation(usuarioLogueado, recursoSeleccionado, franjaSel, fecha);
            nuevaReserva.setObservations(txtObservaciones.getText().trim());

            // delegamos la inyeccion a la capa de servicios para que ejecute las comprobaciones de negocio finales
            reservationService.registrarReserva(nuevaReserva);

            JOptionPane.showMessageDialog(this, "¡Reserva realizada con éxito para el " + fecha + "!");
            this.dispose();

        } catch (IllegalArgumentException ex) {
            // atrapamos el excepcion si el servicio dice que las reglas fallan
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reserva Denegada", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error crítico al guardar la reserva.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}