package aulateca.view;

import aulateca.dao.GenericDAO;
import aulateca.model.Resource;
import aulateca.model.ResourceStatus;
import aulateca.model.ResourceType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResourceView extends JDialog {

    private JTable tablaRecursos;
    private DefaultTableModel modeloTabla;

    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JComboBox<ResourceType> comboTipo;
    private JComboBox<ResourceStatus> comboEstado;

    private JButton btnGuardar;
    private JButton btnActualizar;

    // instanciamos tres daos distintos porque esta pantalla tiene que cruzar datos de tres tablas
    private final GenericDAO<Resource> recursoDAO = new GenericDAO<>();
    private final GenericDAO<ResourceType> tipoDAO = new GenericDAO<>();
    private final GenericDAO<ResourceStatus> estadoDAO = new GenericDAO<>();

    // variable centinela para saber que fila han seleccionado y actualizar la correcta
    private Integer idRecursoSeleccionado = null;

    public ResourceView(Frame parent) {
        super(parent, "Gestión de Recursos", true);
        setSize(750, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDesplegables();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Recurso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelFormulario.add(new JLabel("Nombre (Ej: Aula 204):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;

        txtNombre = new JTextField(20);
        // suavizamos el borde del input para que quede mas web
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panelFormulario.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelFormulario.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;

        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        panelFormulario.add(new JScrollPane(txtDescripcion), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelFormulario.add(new JLabel("Tipo de Recurso:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        comboTipo = new JComboBox<>();
        configurarRenderizadoComboTipo(comboTipo);
        panelFormulario.add(comboTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panelFormulario.add(new JLabel("Estado Actual:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        comboEstado = new JComboBox<>();
        configurarRenderizadoComboEstado(comboEstado);
        panelFormulario.add(comboEstado, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        btnGuardar = new JButton("Crear Nuevo");
        // aplicamos diseño plano web a todos los botones operativos
        btnGuardar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnGuardar.setBackground(new Color(40, 167, 69));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        btnGuardar.setOpaque(true);
        btnGuardar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnActualizar = new JButton("Actualizar Seleccionado");
        btnActualizar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnActualizar.setBackground(new Color(253, 126, 20));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setOpaque(true);
        btnActualizar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.setEnabled(false);

        panelBotones.add(btnGuardar);
        panelBotones.add(btnActualizar);
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.NORTH);

        // creamos la tabla bloqueando la edicion in situ
        modeloTabla = new DefaultTableModel(new String[]{"ID", "Nombre", "Descripción", "Tipo", "Estado"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaRecursos = new JTable(modeloTabla);
        tablaRecursos.setRowHeight(25);

        // escondemos visualmente el id pero lo dejamos en el modelo para operaciones crud
        tablaRecursos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaRecursos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaRecursos.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(tablaRecursos), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton btnLimpiar = new JButton("Limpiar Formulario");
        btnLimpiar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLimpiar.setBackground(new Color(108, 117, 125));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);
        btnLimpiar.setBorderPainted(false);
        btnLimpiar.setOpaque(true);
        btnLimpiar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnLimpiar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnBorrar = new JButton("Borrar Seleccionado");
        btnBorrar.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnBorrar.setBackground(new Color(220, 53, 69));
        btnBorrar.setForeground(Color.WHITE);
        btnBorrar.setFocusPainted(false);
        btnBorrar.setBorderPainted(false);
        btnBorrar.setOpaque(true);
        btnBorrar.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnBorrar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelInferior.add(btnLimpiar);
        panelInferior.add(btnBorrar);
        add(panelInferior, BorderLayout.SOUTH);

        // cargamos los datos de la fila clicada al formulario de arriba para editar
        tablaRecursos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaRecursos.getSelectedRow() != -1) {
                int fila = tablaRecursos.getSelectedRow();
                // usamos la columna invisible que habiamos preparado
                idRecursoSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);

                try {
                    Resource recurso = recursoDAO.findById(Resource.class, idRecursoSeleccionado);
                    txtNombre.setText(recurso.getName());
                    txtDescripcion.setText(recurso.getDescription());

                    seleccionarItemPorId(comboTipo, recurso.getType().getId());
                    seleccionarItemPorId(comboEstado, recurso.getStatus().getId());

                    btnActualizar.setEnabled(true);
                    btnGuardar.setEnabled(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "error al volcar datos del recurso", "error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnGuardar.addActionListener(e -> {
            if (!validarFormulario()) return;

            try {
                ResourceType tipoSelec = (ResourceType) comboTipo.getSelectedItem();
                ResourceStatus estadoSelec = (ResourceStatus) comboEstado.getSelectedItem();

                Resource nuevoRecurso = new Resource(txtNombre.getText().trim(), tipoSelec, estadoSelec);
                nuevoRecurso.setDescription(txtDescripcion.getText().trim());

                recursoDAO.save(nuevoRecurso);
                limpiarFormulario();
                cargarDatosEnTabla();
                JOptionPane.showMessageDialog(this, "Recurso creado correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el recurso.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnActualizar.addActionListener(e -> {
            if (idRecursoSeleccionado == null || !validarFormulario()) return;

            try {
                Resource recurso = recursoDAO.findById(Resource.class, idRecursoSeleccionado);
                recurso.setName(txtNombre.getText().trim());
                recurso.setDescription(txtDescripcion.getText().trim());
                recurso.setType((ResourceType) comboTipo.getSelectedItem());
                recurso.setStatus((ResourceStatus) comboEstado.getSelectedItem());

                recursoDAO.update(recurso);
                limpiarFormulario();
                cargarDatosEnTabla();
                JOptionPane.showMessageDialog(this, "Recurso actualizado.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBorrar.addActionListener(e -> {
            if (idRecursoSeleccionado == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un recurso de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "¿Borrar este recurso?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    recursoDAO.delete(recursoDAO.findById(Resource.class, idRecursoSeleccionado));
                    limpiarFormulario();
                    cargarDatosEnTabla();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error. El recurso tiene reservas asociadas.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private boolean validarFormulario() {
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (comboTipo.getSelectedItem() == null || comboEstado.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debes crear primero Tipos y Estados en el sistema.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        if (comboTipo.getItemCount() > 0) comboTipo.setSelectedIndex(0);
        if (comboEstado.getItemCount() > 0) comboEstado.setSelectedIndex(0);

        idRecursoSeleccionado = null;
        tablaRecursos.clearSelection();
        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
    }

    private void cargarDesplegables() {
        // blindamos la extraccion de datos para los selectores
        try {
            List<ResourceType> tipos = tipoDAO.findAll(ResourceType.class);
            for (ResourceType t : tipos) comboTipo.addItem(t);

            List<ResourceStatus> estados = estadoDAO.findAll(ResourceStatus.class);
            for (ResourceStatus e : estados) comboEstado.addItem(e);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error al cargar configuracion de recursos", "fallo bd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosEnTabla() {
        modeloTabla.setRowCount(0);

        try {
            List<Resource> recursos = recursoDAO.findAll(Resource.class);
            for (Resource r : recursos) {
                modeloTabla.addRow(new Object[]{
                        r.getId(),
                        r.getName(),
                        r.getDescription(),
                        r.getType().getName(),
                        r.getStatus().getName()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "error al volcar tabla de recursos", "fallo bd", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarRenderizadoComboTipo(JComboBox<ResourceType> combo) {
        // renderizamos el combobox para inyectar solo el nombre de la entidad
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ResourceType) setText(((ResourceType) value).getName());
                return this;
            }
        });
    }

    private void configurarRenderizadoComboEstado(JComboBox<ResourceStatus> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ResourceStatus) setText(((ResourceStatus) value).getName());
                return this;
            }
        });
    }

    private void seleccionarItemPorId(JComboBox combo, Integer idDeseado) {
        // bucle para emparejar el id seleccionado en la tabla con el objeto exacto dentro del combo
        for (int i = 0; i < combo.getItemCount(); i++) {
            Object item = combo.getItemAt(i);
            if (item instanceof ResourceType && ((ResourceType) item).getId().equals(idDeseado)) {
                combo.setSelectedIndex(i); return;
            }
            if (item instanceof ResourceStatus && ((ResourceStatus) item).getId().equals(idDeseado)) {
                combo.setSelectedIndex(i); return;
            }
        }
    }
}