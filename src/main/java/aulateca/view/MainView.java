package aulateca.view;

import aulateca.model.User;
import aulateca.model.Role;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainView extends JFrame {

    // guardamos el usuario que acaba de entrar para saber que permisos tiene
    private final User usuarioLogueado;

    public MainView(User user) {
        this.usuarioLogueado = user;

        setTitle("Aulateca - Gestor de Reservas de Espacios y Recursos");
        // le damos un tamano generoso para que las tarjetas del centro no se vean apretadas
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        // montamos la barra superior clasica por si prefieren navegar por menus
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuSistema = new JMenu("Sistema");
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        JMenuItem itemSalir = new JMenuItem("Salir");
        menuSistema.add(itemCerrarSesion);
        menuSistema.add(new JSeparator());
        menuSistema.add(itemSalir);

        JMenu menuGestion = new JMenu("Gestión");
        JMenuItem itemUsuarios = new JMenuItem("Usuarios");
        JMenuItem itemTiposRecurso = new JMenuItem("Tipos de Recursos");
        JMenuItem itemRecursos = new JMenuItem("Recursos Concretos");
        JMenuItem itemEstados = new JMenuItem("Estados de Recursos");
        JMenuItem itemFranjas = new JMenuItem("Franjas Horarias");
        JMenuItem itemGestionarReservas = new JMenuItem("Gestionar Reservas (Admin)");

        menuGestion.add(itemUsuarios);
        menuGestion.add(itemTiposRecurso);
        menuGestion.add(itemRecursos);
        menuGestion.add(itemEstados);
        menuGestion.add(itemFranjas);
        menuGestion.add(new JSeparator());
        menuGestion.add(itemGestionarReservas);

        JMenu menuReservas = new JMenu("Reservas");
        JMenuItem itemNuevaReserva = new JMenuItem("Nueva Reserva");
        JMenuItem itemConsultarDisponibilidad = new JMenuItem("Consultar Disponibilidad");
        JMenuItem itemHistorial = new JMenuItem("Mi Historial");

        menuReservas.add(itemNuevaReserva);
        menuReservas.add(itemConsultarDisponibilidad);
        menuReservas.add(itemHistorial);

        barraMenu.add(menuSistema);
        barraMenu.add(menuGestion);
        barraMenu.add(menuReservas);
        setJMenuBar(barraMenu);

        // usamos un color claro de fondo para darle un toque mas moderno tipo pagina web
        JPanel panelCentral = new JPanel(new BorderLayout(20, 20));
        panelCentral.setBorder(new EmptyBorder(30, 40, 30, 40));
        panelCentral.setBackground(new Color(248, 249, 250));

        JPanel panelCabecera = new JPanel(new GridLayout(2, 1));
        panelCabecera.setOpaque(false);

        JLabel lblBienvenida = new JLabel("¡Hola, " + usuarioLogueado.getFullName() + "!", JLabel.LEFT);
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBienvenida.setForeground(new Color(33, 37, 41));

        JLabel lblSubtitulo = new JLabel("Bienvenido al panel de control de Aulateca. ¿Qué necesitas hacer hoy?", JLabel.LEFT);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitulo.setForeground(new Color(108, 117, 125));

        panelCabecera.add(lblBienvenida);
        panelCabecera.add(lblSubtitulo);
        panelCentral.add(panelCabecera, BorderLayout.NORTH);

        // creamos una cuadricula perfecta para que todos los botones tengan exactamente el mismo tamano
        JPanel panelTarjetas = new JPanel(new GridLayout(2, 2, 20, 20));
        panelTarjetas.setOpaque(false);

        JButton btnCrearReserva = crearBotonDashboard("Nueva Reserva", "Asegura un aula, carrito o laboratorio de forma rápida.", new Color(13, 110, 253));
        btnCrearReserva.addActionListener(e -> abrirModuloReservas("Nueva Reserva"));
        panelTarjetas.add(btnCrearReserva);

        JButton btnDisponibilidad = crearBotonDashboard("Ver Horarios", "Consulta qué espacios están libres hoy u otros días.", new Color(25, 135, 84));
        btnDisponibilidad.addActionListener(e -> abrirModuloReservas("Consultar Disponibilidad"));
        panelTarjetas.add(btnDisponibilidad);

        JButton btnMisReservas = crearBotonDashboard("Mis Reservas", "Revisa tus próximas reservas o consulta tu historial pasado.", new Color(253, 126, 20));
        btnMisReservas.addActionListener(e -> abrirModuloReservas("Historial"));
        panelTarjetas.add(btnMisReservas);

        // metemos la logica para que el panel principal cambie segun el rol del que inicie sesion
        if (usuarioLogueado.getRole() != Role.ALUMNO) {
            // si es profe o admin le pintamos la tarjeta de acceso a la gestion masiva
            JButton btnAdmin = crearBotonDashboard(" Gestión General", "Panel avanzado para supervisar el centro.", new Color(108, 117, 125));
            btnAdmin.addActionListener(e -> {
                ManageReservationsView panelAdminReservas = new ManageReservationsView(this);
                panelAdminReservas.setVisible(true);
            });
            panelTarjetas.add(btnAdmin);
        } else {
            // rellenamos el hueco vacio de los alumnos con un mensaje util para que la cuadricula no quede coja
            JPanel panelVacioDecorativo = new JPanel(new BorderLayout());
            panelVacioDecorativo.setOpaque(false);
            JLabel infoExtra = new JLabel("<html><div style='text-align:center; color:#6c757d;'><i>Recuerda devolver siempre los<br>materiales en su estado original.</i></div></html>", JLabel.CENTER);
            panelVacioDecorativo.add(infoExtra, BorderLayout.CENTER);
            panelTarjetas.add(panelVacioDecorativo);
        }

        panelCentral.add(panelTarjetas, BorderLayout.CENTER);

        JPanel panelPie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelPie.setOpaque(false);
        JButton btnCerrarSesionRapido = new JButton("Cerrar Sesión");
        btnCerrarSesionRapido.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrarSesionRapido.setBackground(new Color(220, 53, 69));
        btnCerrarSesionRapido.setForeground(Color.WHITE);
        btnCerrarSesionRapido.setFocusPainted(false);
        btnCerrarSesionRapido.addActionListener(e -> cerrarSesion());
        panelPie.add(btnCerrarSesionRapido);

        panelCentral.add(panelPie, BorderLayout.SOUTH);

        JPanel barraEstado = new JPanel(new BorderLayout());
        barraEstado.setBorder(BorderFactory.createEtchedBorder());
        barraEstado.setPreferredSize(new Dimension(this.getWidth(), 25));

        JLabel lblRol = new JLabel("  Sesión iniciada como: " + usuarioLogueado.getRole().toString());
        lblRol.setFont(new Font("Arial", Font.ITALIC, 12));
        barraEstado.add(lblRol, BorderLayout.WEST);

        add(panelCentral, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);

        // capamos el acceso al menu superior de mantenimiento si el usuario es solo un alumno
        if (usuarioLogueado.getRole() == Role.ALUMNO) {
            menuGestion.setVisible(false);
        }

        itemSalir.addActionListener(e -> System.exit(0));
        itemCerrarSesion.addActionListener(e -> cerrarSesion());

        itemUsuarios.addActionListener(e -> abrirVentanaCRUD("Usuarios"));
        itemTiposRecurso.addActionListener(e -> abrirVentanaCRUD("Tipos de Recursos"));
        itemRecursos.addActionListener(e -> abrirVentanaCRUD("Recursos Concretos"));
        itemEstados.addActionListener(e -> abrirVentanaCRUD("Estados de Recursos"));
        itemFranjas.addActionListener(e -> abrirVentanaCRUD("Franjas Horarias"));
        itemGestionarReservas.addActionListener(e -> {
            ManageReservationsView panelAdmin = new ManageReservationsView(this);
            panelAdmin.setVisible(true);
        });

        itemNuevaReserva.addActionListener(e -> abrirModuloReservas("Nueva Reserva"));
        itemConsultarDisponibilidad.addActionListener(e -> abrirModuloReservas("Consultar Disponibilidad"));
        itemHistorial.addActionListener(e -> abrirModuloReservas("Historial"));
    }

    private JButton crearBotonDashboard(String titulo, String descripcion, Color colorBase) {
        // este es el truco para que los botones de java parezcan tarjetas web inyectando codigo html
        String htmlText = "<html><div style='text-align: center; padding: 10px;'>"
                + "<h2 style='margin-bottom: 5px; font-family: Segoe UI;'>" + titulo + "</h2>"
                + "<p style='font-family: Segoe UI; font-size: 11px; margin-top: 0;'>" + descripcion + "</p>"
                + "</div></html>";

        JButton boton = new JButton(htmlText);
        boton.setBackground(colorBase);
        boton.setForeground(Color.WHITE);

        // le quitamos los bordes feos que trae swing por defecto para que se vea totalmente plano
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder());
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    private void cerrarSesion() {
        // matamos esta ventana al cerrar sesion para no dejar procesos colgando en memoria
        this.dispose();
        new LoginView().setVisible(true);
    }

    // centralizamos la apertura de las pantallas de mantenimiento en un solo metodo con ifs
    private void abrirVentanaCRUD(String nombreModulo) {
        if (nombreModulo.equals("Tipos de Recursos")) {
            new ResourceTypeView(this).setVisible(true);
        } else if (nombreModulo.equals("Usuarios")) {
            new UserView(this).setVisible(true);
        } else if (nombreModulo.equals("Estados de Recursos")) {
            new ResourceStatusView(this).setVisible(true);
        } else if (nombreModulo.equals("Franjas Horarias")) {
            new TimeSlotView(this).setVisible(true);
        } else if (nombreModulo.equals("Recursos Concretos")) {
            new ResourceView(this).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Abriendo el panel: " + nombreModulo, nombreModulo, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // hacemos lo mismo para enrutar las ventanas del bloque de reservas segun el string
    private void abrirModuloReservas(String nombreModulo) {
        if (nombreModulo.equals("Nueva Reserva")) {
            ReservationView ventanaReservas = new ReservationView(this, usuarioLogueado);
            ventanaReservas.setVisible(true);
        } else if (nombreModulo.equals("Consultar Disponibilidad")) {
            AvailabilityView ventanaDisponibilidad = new AvailabilityView(this);
            ventanaDisponibilidad.setVisible(true);
        } else if (nombreModulo.equals("Historial")) {
            HistoryView ventanaHistorial = new HistoryView(this, usuarioLogueado);
            ventanaHistorial.setVisible(true);
        }
    }
}