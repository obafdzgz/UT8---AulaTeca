package aulateca.view;

import aulateca.model.User;
import aulateca.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private final UserService userService = new UserService();

    public LoginView() {
        setTitle("Aulateca - Inicio de Sesión");
        // ampliamos un poco la ventana para que los elementos respiren mejor
        setSize(450, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // creamos un panel base con fondo blanco para dar una estetica limpia y profesional
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(Color.WHITE);

        // --- cabecera corporativa ---
        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(Color.WHITE);
        panelCabecera.setBorder(new EmptyBorder(20, 0, 10, 0));

        JLabel lblTitulo = new JLabel("AULATECA");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(33, 37, 41));
        panelCabecera.add(lblTitulo);

        // --- formulario central ---
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblUser = new JLabel("Usuario:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        txtUsername = new JTextField(15);
        // personalizamos el borde del campo de texto para que sea mas suave y moderno
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        panelFormulario.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panelFormulario.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        // usamos el campo de password para ocultar los caracteres introducidos
        txtPassword = new JPasswordField(15);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        panelFormulario.add(txtPassword, gbc);

        // --- zona inferior del boton ---
        JPanel panelBoton = new JPanel();
        panelBoton.setBackground(Color.WHITE);
        panelBoton.setBorder(new EmptyBorder(10, 0, 30, 0));

        btnLogin = new JButton("Iniciar Sesión");

        // desvinculamos el boton del sistema operativo para aplicarle un color solido
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLogin.setBackground(new Color(13, 110, 253));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // quitamos bordes y efectos por defecto para conseguir un look and feel plano
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(true);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBoton.add(btnLogin);

        // ensamblamos las tres partes en el panel principal
        panelPrincipal.add(panelCabecera, BorderLayout.NORTH);
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);
        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        add(panelPrincipal);

        // usamos una expresion lambda para compactar el action listener
        btnLogin.addActionListener(e -> procesarLogin());
    }

    private void procesarLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try {
            User userLogueado = userService.login(username, password);

            JOptionPane.showMessageDialog(this, "¡Bienvenido, " + userLogueado.getUsername() + "!",
                    "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // levantamos el panel principal inyectando el objeto del usuario actual
            MainView main = new MainView(userLogueado);
            main.setVisible(true);

            // destruimos la ventana de login para que desaparezca y libere recursos
            this.dispose();

        } catch (IllegalArgumentException ex) {
            // atrapamos cualquier excepcion
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
        }
    }
}