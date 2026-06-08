package aulateca.view;

import aulateca.dao.UserDAO;
import aulateca.model.Role;
import aulateca.model.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// ventana para la gestion de los usuarios
public class UserView extends JDialog {

    // elementos visuales de la pantalla
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JComboBox<Role> comboRol;

    // caja de texto para buscar usuarios
    private JTextField txtBuscador;

    // lista para guardar los datos y que el buscador vaya mas rapido
    private List<User> todosLosUsuarios = new ArrayList<>();

    // conexion con la base de datos y control del usuario seleccionado
    private final UserDAO dao = new UserDAO();
    private Integer idUsuarioSeleccionado = null;

    // configuracion principal de la ventana
    public UserView(Frame parent) {
        super(parent, "Gestión de Usuarios", true);
        setSize(700, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarUsuariosDesdeDB();
    }

    // dibuja y coloca todos los botones, textos y paneles
    private void inicializarComponentes() {

        // zona de arriba: formulario para crear o editar
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Usuario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        txtUsername = new JTextField(15);
        panelFormulario.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtPassword = new JPasswordField(15);
        panelFormulario.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Nombre Completo:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtFullName = new JTextField(15);
        panelFormulario.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panelFormulario.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        comboRol = new JComboBox<>(Role.values());
        panelFormulario.add(comboRol, gbc);

        // botones propios del formulario
        gbc.gridx = 1; gbc.gridy = 4;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnGuardar = new JButton("Crear Nuevo");
        JButton btnActualizar = new JButton("Actualizar");

        aplicarEstiloBoton(btnGuardar, new Color(40, 167, 69));
        aplicarEstiloBoton(btnActualizar, new Color(253, 126, 20));
        btnActualizar.setEnabled(false);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        // zona central: buscador y la tabla con los datos
        JPanel panelCentral = new JPanel(new BorderLayout(5, 5));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel panelBuscador = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBuscador.add(new JLabel("🔍 Buscar Usuario:"));
        txtBuscador = new JTextField(25);
        txtBuscador.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // hace que la lista se filtre sola al ir tecleando
        txtBuscador.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            @Override
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });
        panelBuscador.add(txtBuscador);
        panelCentral.add(panelBuscador, BorderLayout.NORTH);

        // creacion de la tabla para que no se pueda escribir en ella directamente
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Usuario", "Nombre Completo", "Rol"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setRowHeight(25);

        // esconde la primera columna del ID para que quede mas limpio
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setWidth(0);

        panelCentral.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // zona de abajo: botones secundarios
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        JButton btnBorrar = new JButton("Borrar Seleccionado");
        JButton btnLimpiar = new JButton("Limpiar Formulario");

        aplicarEstiloBoton(btnBorrar, new Color(220, 53, 69));
        aplicarEstiloBoton(btnLimpiar, new Color(108, 117, 125));

        panelInferior.add(btnLimpiar);
        panelInferior.add(btnBorrar);
        add(panelInferior, BorderLayout.SOUTH);

        // al pinchar en una fila de la tabla, los datos suben al formulario
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                int fila = tablaUsuarios.getSelectedRow();
                idUsuarioSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
                txtUsername.setText((String) modeloTabla.getValueAt(fila, 1));
                txtFullName.setText((String) modeloTabla.getValueAt(fila, 2));
                comboRol.setSelectedItem(Role.valueOf((String) modeloTabla.getValueAt(fila, 3)));
                txtPassword.setText("");
                btnActualizar.setEnabled(true);
                btnGuardar.setEnabled(false);
            }
        });

        // bloque de codigo para guardar un usuario nuevo
        btnGuardar.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "usuario y contraseña son obligatorios", "aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                User nuevo = new User(username, password, (Role) comboRol.getSelectedItem());
                nuevo.setFullName(txtFullName.getText().trim());
                dao.save(nuevo);
                limpiarFormulario(btnGuardar, btnActualizar);
                cargarUsuariosDesdeDB();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "error al guardar usuario", "error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // bloque de codigo para modificar un usuario existente
        btnActualizar.addActionListener(e -> {
            if (idUsuarioSeleccionado == null) return;
            try {
                User u = dao.findById(User.class, idUsuarioSeleccionado);
                u.setUsername(txtUsername.getText().trim());
                u.setFullName(txtFullName.getText().trim());
                u.setRole((Role) comboRol.getSelectedItem());
                String p = new String(txtPassword.getPassword()).trim();
                if (!p.isEmpty()) u.setPassword(p);
                dao.update(u);
                limpiarFormulario(btnGuardar, btnActualizar);
                cargarUsuariosDesdeDB();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "error al actualizar", "error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // bloque de codigo para eliminar un usuario manejando la integridad referencial
        btnBorrar.addActionListener(e -> {
            if (idUsuarioSeleccionado == null) return;
            if (JOptionPane.showConfirmDialog(this, "¿borrar usuario?", "confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.delete(dao.findById(User.class, idUsuarioSeleccionado));
                    limpiarFormulario(btnGuardar, btnActualizar);
                    cargarUsuariosDesdeDB();
                    JOptionPane.showMessageDialog(this, "Usuario borrado con éxito.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "No se puede borrar este usuario porque tiene reservas asociadas en el historial.\n\nPor favor, ve a la Gestión de Reservas y elimina sus reservas primero.",
                            "Aviso de Integridad",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // accion para vaciar todo
        btnLimpiar.addActionListener(e -> {
            limpiarFormulario(btnGuardar, btnActualizar);
            txtBuscador.setText("");
        });
    }

    // metodo para poner los botones de colores sin el estilo de windows
    private void aplicarEstiloBoton(JButton btn, Color bg) {
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // borra los textos y prepara el formulario para uno nuevo
    private void limpiarFormulario(JButton g, JButton a) {
        txtUsername.setText(""); txtPassword.setText(""); txtFullName.setText("");
        comboRol.setSelectedIndex(0);
        idUsuarioSeleccionado = null;
        tablaUsuarios.clearSelection();
        g.setEnabled(true); a.setEnabled(false);
    }

    // lee la base de datos y guarda los datos en memoria
    private void cargarUsuariosDesdeDB() {
        try {
            todosLosUsuarios = dao.findAll(User.class);
            aplicarFiltros();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error al cargar la lista", "error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // busca las coincidencias entre el texto escrito y la lista de usuarios
    private void aplicarFiltros() {
        String textoBusqueda = txtBuscador.getText().trim().toLowerCase();

        List<User> filtrados = todosLosUsuarios.stream()
                .filter(u -> textoBusqueda.isEmpty() ||
                        u.getUsername().toLowerCase().contains(textoBusqueda) ||
                        (u.getFullName() != null && u.getFullName().toLowerCase().contains(textoBusqueda)) ||
                        u.getRole().name().toLowerCase().contains(textoBusqueda))
                .collect(Collectors.toList());

        pintarTabla(filtrados);
    }

    // dibuja los resultados de la busqueda en la tabla visual
    private void pintarTabla(List<User> lista) {
        modeloTabla.setRowCount(0);
        for (User u : lista) {
            modeloTabla.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole().toString()});
        }
    }
}