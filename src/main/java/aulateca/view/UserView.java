package aulateca.view;

import aulateca.dao.UserDAO;
import aulateca.model.Role;
import aulateca.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserView extends JDialog {

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JComboBox<Role> comboRol;

    private final UserDAO dao = new UserDAO();
    private Integer idUsuarioSeleccionado = null;

    public UserView(Frame parent) {
        super(parent, "Gestión de Usuarios", true);
        setSize(650, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
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

        gbc.gridx = 1; gbc.gridy = 4;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnGuardar = new JButton("Crear Nuevo");
        JButton btnActualizar = new JButton("Actualizar");

        // aplicamos el estilo de boton web plano
        aplicarEstiloBoton(btnGuardar, new Color(40, 167, 69));
        aplicarEstiloBoton(btnActualizar, new Color(253, 126, 20));
        btnActualizar.setEnabled(false);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(new String[]{"ID", "Usuario", "Nombre Completo", "Rol"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setRowHeight(25);
        // ocultamos visualmente la columna id para limpiar la tabla
        tablaUsuarios.getColumnModel().getColumn(0).setMinWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaUsuarios.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnBorrar = new JButton("Borrar Seleccionado");
        JButton btnLimpiar = new JButton("Limpiar");

        aplicarEstiloBoton(btnBorrar, new Color(220, 53, 69));
        aplicarEstiloBoton(btnLimpiar, new Color(108, 117, 125));

        panelInferior.add(btnLimpiar);
        panelInferior.add(btnBorrar);
        add(panelInferior, BorderLayout.SOUTH);

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
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "error al guardar usuario", "error", JOptionPane.ERROR_MESSAGE);
            }
        });

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
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "error al actualizar", "error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBorrar.addActionListener(e -> {
            if (idUsuarioSeleccionado == null) return;
            if (JOptionPane.showConfirmDialog(this, "¿borrar usuario?", "confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.delete(dao.findById(User.class, idUsuarioSeleccionado));
                    limpiarFormulario(btnGuardar, btnActualizar);
                    cargarDatosEnTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "no se puede borrar", "error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnLimpiar.addActionListener(e -> limpiarFormulario(btnGuardar, btnActualizar));
    }

    private void aplicarEstiloBoton(JButton btn, Color bg) {
        // metodo auxiliar para estandarizar el look plano en toda la aplicacion
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void limpiarFormulario(JButton g, JButton a) {
        txtUsername.setText(""); txtPassword.setText(""); txtFullName.setText("");
        comboRol.setSelectedIndex(0);
        idUsuarioSeleccionado = null;
        tablaUsuarios.clearSelection();
        g.setEnabled(true); a.setEnabled(false);
    }

    private void cargarDatosEnTabla() {
        modeloTabla.setRowCount(0);
        // bloque de seguridad para que el programa no colapse si la bd falla
        try {
            List<User> lista = dao.findAll(User.class);
            for (User u : lista) {
                modeloTabla.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole().toString()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error al cargar la lista", "error", JOptionPane.ERROR_MESSAGE);
        }
    }
}