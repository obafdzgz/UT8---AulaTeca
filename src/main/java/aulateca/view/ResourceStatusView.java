package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.ResourceStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResourceStatusView extends JDialog {

    private JTable tablaEstados;
    private DefaultTableModel modeloTabla;
    private JTextField txtNombre;

    private final GenericDAO<ResourceStatus> dao = new GenericDAO<>();

    public ResourceStatusView(Frame parent) {
        super(parent, "Gestión de Estados de Recursos", true);
        // le damos un poco mas de margen a la ventana
        setSize(450, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        // montamos el formulario superior con margenes para que respire
        JPanel panelFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelFormulario.add(new JLabel("Nuevo Estado:"));

        txtNombre = new JTextField(15);
        // le damos un toque mas limpio al borde del input
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panelFormulario.add(txtNombre);

        JButton btnGuardar = new JButton("Guardar");
        // quitamos el estilo de windows para ponerle un diseño web mas minimalista
        btnGuardar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelFormulario.add(btnGuardar);
        add(panelFormulario, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // bloqueamos la edicion directa en la tabla con este override rapido
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre del Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaEstados = new JTable(modeloTabla);
        tablaEstados.setRowHeight(25);

        // ocultamos la columna del id bajando su ancho a cero pixeles
        tablaEstados.getColumnModel().getColumn(0).setMinWidth(0);
        tablaEstados.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaEstados.getColumnModel().getColumn(0).setWidth(0);

        panelCentral.add(new JScrollPane(tablaEstados), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 20));

        JButton btnBorrar = new JButton("Borrar Seleccionado");
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
            // forzamos mayusculas para mantener coherencia en la base de datos
            String nombre = txtNombre.getText().trim().toUpperCase();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                dao.save(new ResourceStatus(nombre));
                txtNombre.setText("");
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBorrar.addActionListener(e -> {
            int fila = tablaEstados.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un estado para borrar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // rescatamos el id oculto de la fila que han clicado
            Integer id = (Integer) modeloTabla.getValueAt(fila, 0);
            if (JOptionPane.showConfirmDialog(this, "¿Borrar este estado?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.delete(dao.findById(ResourceStatus.class, id));
                    cargarDatosEnTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: Estado en uso por algún recurso.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void cargarDatosEnTabla() {
        modeloTabla.setRowCount(0);

        // envolvemos la lectura masiva en un bloque de seguridad
        try {
            List<ResourceStatus> lista = dao.findAll(ResourceStatus.class);
            for (ResourceStatus estado : lista) {
                modeloTabla.addRow(new Object[]{estado.getId(), estado.getName()});
            }
        } catch (Exception ex) {
            // avisamos al usuario si la base de datos no responde al arrancar la pantalla
            JOptionPane.showMessageDialog(this, "error al cargar los estados desde el servidor", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }
    }
}