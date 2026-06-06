package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.ResourceType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResourceTypeView extends JDialog {

    private JTable tablaTipos;
    private DefaultTableModel modeloTabla;
    private JTextField txtNombre;

    // instanciamos el dao generico para comunicar con la base de datos
    private final GenericDAO<ResourceType> dao = new GenericDAO<>();

    public ResourceTypeView(Frame parent) {
        // configuramos la ventana como un jdialog que bloquea la principal hasta que se cierre
        super(parent, "Gestión de Tipos de Recursos", true);
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
        panelFormulario.add(new JLabel("Nuevo Tipo:"));

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
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre del Tipo"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaTipos = new JTable(modeloTabla);
        tablaTipos.setRowHeight(25);

        // ocultamos la columna del id bajando su ancho a cero pixeles
        tablaTipos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaTipos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaTipos.getColumnModel().getColumn(0).setWidth(0);

        panelCentral.add(new JScrollPane(tablaTipos), BorderLayout.CENTER);
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
                ResourceType nuevoTipo = new ResourceType(nombre);
                dao.save(nuevoTipo);

                txtNombre.setText("");
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar. Puede que el nombre ya exista.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBorrar.addActionListener(e -> {
            int filaSeleccionada = tablaTipos.getSelectedRow();
            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona un elemento de la tabla para borrar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // rescatamos el id oculto de la fila que han clicado
            Integer id = (Integer) modeloTabla.getValueAt(filaSeleccionada, 0);

            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Seguro que quieres borrar este tipo?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    // buscamos el objeto por id y lo borramos
                    ResourceType tipoABorrar = dao.findById(ResourceType.class, id);
                    dao.delete(tipoABorrar);
                    cargarDatosEnTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al borrar. Comprueba que no esté siendo usado por un recurso.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void cargarDatosEnTabla() {
        modeloTabla.setRowCount(0);

        // envolvemos la lectura masiva en un bloque de seguridad
        try {
            List<ResourceType> listaTipos = dao.findAll(ResourceType.class);
            for (ResourceType tipo : listaTipos) {
                modeloTabla.addRow(new Object[]{tipo.getId(), tipo.getName()});
            }
        } catch (Exception ex) {
            // avisamos al usuario si la base de datos no responde al arrancar la pantalla
            JOptionPane.showMessageDialog(this, "error al cargar los tipos desde el servidor", "fallo de conexion", JOptionPane.ERROR_MESSAGE);
        }
    }
}