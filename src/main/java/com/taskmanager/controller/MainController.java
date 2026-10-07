package com.taskmanager.controller;

import com.taskmanager.model.Tarea;
import com.taskmanager.persistence.TareaDAO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador principal de la aplicación (MVC - Controller).
 * Maneja la lógica de presentación, validaciones, eventos de UI y coordinación
 * entre la Vista (FXML) y el Modelo (Tarea + TareaDAO).
 */
public class MainController implements Initializable {

    // ==================== COMPONENTES FXML - TABLA ====================

    @FXML private TableView<Tarea> tablaTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colMateria;
    @FXML private TableColumn<Tarea, LocalDate> colFechaEntrega;
    @FXML private TableColumn<Tarea, String> colPrioridad;
    @FXML private TableColumn<Tarea, String> colEstado;

    // ==================== COMPONENTES FXML - FORMULARIO ====================

    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtMateria;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private ComboBox<String> cbPrioridad;
    @FXML private ComboBox<String> cbEstado;

    // ==================== COMPONENTES FXML - BOTONES ====================

    @FXML private Button btnAgregar;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    // ==================== COMPONENTES FXML - BÚSQUEDA/FILTROS ====================

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroMateria;
    @FXML private ComboBox<String> cbFiltroEstado;
    @FXML private ComboBox<String> cbFiltroPrioridad;
    @FXML private Button btnLimpiarFiltros;

    // ==================== ESTADO INTERNO ====================

    private final TareaDAO tareaDAO = new TareaDAO();
    private final ObservableList<Tarea> listaTareas = FXCollections.observableArrayList();
    private FilteredList<Tarea> tareasFiltradas;
    private Tarea tareaSeleccionada = null;
    private boolean modoEdicion = false;

    // Formateador de fecha para la tabla
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ==================== INICIALIZACIÓN ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTabla();
        configurarFormulario();
        configurarFiltros();
        configurarEventos();
        cargarDatosIniciales();
    }

    // ==================== CONFIGURACIÓN DE LA TABLA ====================

    private void configurarTabla() {
        // Configurar columnas con PropertyValueFactory para binding automático
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materia"));
        colFechaEntrega.setCellValueFactory(new PropertyValueFactory<>("fechaEntrega"));
        colPrioridad.setCellValueFactory(new PropertyValueFactory<>("prioridad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Formatear fecha en la tabla
        colFechaEntrega.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setText(empty || fecha == null ? "" : fecha.format(FORMATO_FECHA));
            }
        });

        // Colorear filas según prioridad y estado
        tablaTareas.setRowFactory(tv -> new TableRow<Tarea>() {
            @Override
            protected void updateItem(Tarea tarea, boolean empty) {
                super.updateItem(tarea, empty);
                if (empty || tarea == null) {
                    setStyle("");
                } else {
                    StringBuilder style = new StringBuilder();

                    // Colores por prioridad
                    switch (tarea.getPrioridad()) {
                        case Tarea.PRIORIDAD_ALTA -> style.append("-fx-background-color: #ffe0e0; ");
                        case Tarea.PRIORIDAD_MEDIA -> style.append("-fx-background-color: #fff3e0; ");
                        case Tarea.PRIORIDAD_BAJA -> style.append("-fx-background-color: #e8f5e9; ");
                    }

                    // Tachado para completadas
                    if (Tarea.ESTADO_COMPLETADA.equals(tarea.getEstado())) {
                        style.append("-fx-text-fill: #9e9e9e; ");
                    }

                    // Resaltar vencidas
                    if (tarea.estaVencida()) {
                        style.append("-fx-font-weight: bold; -fx-text-fill: #c62828; ");
                    }

                    setStyle(style.toString());
                }
            }
        });

        // Listener de selección: poblar formulario al seleccionar fila
        tablaTareas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tareaSeleccionada = newSel;
                poblarFormulario(newSel);
                actualizarBotonesParaEdicion(true);
            }
        });

        // Configurar lista filtrada y ordenada
        tareasFiltradas = new FilteredList<>(listaTareas, p -> true);
        SortedList<Tarea> tareasOrdenadas = new SortedList<>(tareasFiltradas);
        tareasOrdenadas.comparatorProperty().bind(tablaTareas.comparatorProperty());
        tablaTareas.setItems(tareasOrdenadas);
    }

    // ==================== CONFIGURACIÓN DEL FORMULARIO ====================

    private void configurarFormulario() {
        // Poblar ComboBoxes con valores válidos
        cbPrioridad.setItems(FXCollections.observableArrayList(
                Tarea.PRIORIDAD_ALTA, Tarea.PRIORIDAD_MEDIA, Tarea.PRIORIDAD_BAJA));
        cbPrioridad.setValue(Tarea.PRIORIDAD_MEDIA); // Valor por defecto

        cbEstado.setItems(FXCollections.observableArrayList(
                Tarea.ESTADO_PENDIENTE, Tarea.ESTADO_EN_PROCESO, Tarea.ESTADO_COMPLETADA));
        cbEstado.setValue(Tarea.ESTADO_PENDIENTE); // Valor por defecto

        // Configurar DatePicker con formato español
        dpFechaEntrega.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(FORMATO_FECHA) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                try {
                    return LocalDate.parse(string, FORMATO_FECHA);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        // Deshabilitar edición inicial del ID (no editable por usuario)
        // El ID se genera automáticamente
    }

    // ==================== CONFIGURACIÓN DE FILTROS ====================

    private void configurarFiltros() {
        // ComboBox de filtro de materias (se poblará dinámicamente)
        cbFiltroMateria.setItems(FXCollections.observableArrayList());
        cbFiltroMateria.setPromptText("Todas las materias");

        cbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", Tarea.ESTADO_PENDIENTE, Tarea.ESTADO_EN_PROCESO, Tarea.ESTADO_COMPLETADA));
        cbFiltroEstado.setValue("Todos");

        cbFiltroPrioridad.setItems(FXCollections.observableArrayList(
                "Todas", Tarea.PRIORIDAD_ALTA, Tarea.PRIORIDAD_MEDIA, Tarea.PRIORIDAD_BAJA));
        cbFiltroPrioridad.setValue("Todas");

        // Listeners para filtros en tiempo real
        txtBuscar.textProperty().addListener((obs, old, val) -> aplicarFiltros());
        cbFiltroMateria.valueProperty().addListener((obs, old, val) -> aplicarFiltros());
        cbFiltroEstado.valueProperty().addListener((obs, old, val) -> aplicarFiltros());
        cbFiltroPrioridad.valueProperty().addListener((obs, old, val) -> aplicarFiltros());
    }

    // ==================== CONFIGURACIÓN DE EVENTOS ====================

    private void configurarEventos() {
        btnAgregar.setOnAction(e -> manejarAgregar());
        btnEditar.setOnAction(e -> manejarEditar());
        btnEliminar.setOnAction(e -> manejarEliminar());
        btnLimpiar.setOnAction(e -> manejarLimpiar());
        btnLimpiarFiltros.setOnAction(e -> limpiarFiltros());

        // Enter en campo de búsqueda aplica filtro
        txtBuscar.setOnAction(e -> aplicarFiltros());

        // Doble clic en tabla entra en modo edición
        tablaTareas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tareaSeleccionada != null) {
                modoEdicion = true;
                actualizarBotonesParaEdicion(true);
                txtTitulo.requestFocus();
            }
        });
    }

    // ==================== CARGA INICIAL ====================

    private void cargarDatosIniciales() {
        List<Tarea> tareas = tareaDAO.obtenerTodas();
        listaTareas.setAll(tareas);
        actualizarFiltroMaterias();
        logger.info("Aplicación iniciada con {} tareas", tareas.size());
    }

    // ==================== MANEJADORES CRUD ====================

    @FXML
    private void manejarAgregar() {
        if (!validarFormulario()) return;

        Tarea nueva = crearTareaDesdeFormulario();
        if (tareaDAO.agregar(nueva)) {
            listaTareas.add(nueva);
            actualizarFiltroMaterias();
            manejarLimpiar();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarea registrada correctamente");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agregar la tarea (ID duplicado)");
        }
    }

    @FXML
    private void manejarEditar() {
        if (tareaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Seleccione una tarea para editar");
            return;
        }

        if (!validarFormulario()) return;

        // Si no está en modo edición, activar modo edición
        if (!modoEdicion) {
            modoEdicion = true;
            actualizarBotonesParaEdicion(true);
            txtTitulo.requestFocus();
            return;
        }

        // Modo edición: guardar cambios
        Tarea actualizada = crearTareaDesdeFormulario();
        actualizada.setId(tareaSeleccionada.getId()); // Mantener el mismo ID

        if (tareaDAO.actualizar(actualizada)) {
            // Actualizar en la lista observable (mantiene la referencia en la tabla)
            tareaSeleccionada.actualizarDesde(actualizada);
            tablaTareas.refresh(); // Refrescar para que se vean cambios en celdas
            actualizarFiltroMaterias();
            manejarLimpiar();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarea actualizada correctamente");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la tarea");
        }
    }

    @FXML
    private void manejarEliminar() {
        if (tareaSeleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Seleccione una tarea para eliminar");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar tarea?");
        confirmacion.setContentText("¿Está seguro de eliminar la tarea: \"" + tareaSeleccionada.getTitulo() + "\"?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (tareaDAO.eliminar(tareaSeleccionada.getId())) {
                listaTareas.remove(tareaSeleccionada);
                actualizarFiltroMaterias();
                manejarLimpiar();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tarea eliminada correctamente");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la tarea");
            }
        }
    }

    @FXML
    private void manejarLimpiar() {
        limpiarFormulario();
        tablaTareas.getSelectionModel().clearSelection();
        tareaSeleccionada = null;
        modoEdicion = false;
        actualizarBotonesParaEdicion(false);
    }

    // ==================== FILTRADO ====================

    private void aplicarFiltros() {
        String textoBusqueda = txtBuscar.getText();
        String materiaFiltro = cbFiltroMateria.getValue();
        String estadoFiltro = cbFiltroEstado.getValue();
        String prioridadFiltro = cbFiltroPrioridad.getValue();

        Predicate<Tarea> filtro = tarea -> {
            // Filtro por texto (título o descripción)
            if (textoBusqueda != null && !textoBusqueda.trim().isEmpty()) {
                String busqueda = textoBusqueda.toLowerCase().trim();
                boolean coincideTitulo = tarea.getTitulo().toLowerCase().contains(busqueda);
                boolean coincideDesc = tarea.getDescripcion() != null
                        && tarea.getDescripcion().toLowerCase().contains(busqueda);
                if (!coincideTitulo && !coincideDesc) return false;
            }

            // Filtro por materia
            if (materiaFiltro != null && !materiaFiltro.equals("Todas las materias")
                    && !materiaFiltro.isEmpty()) {
                if (!materiaFiltro.equalsIgnoreCase(tarea.getMateria())) return false;
            }

            // Filtro por estado
            if (estadoFiltro != null && !estadoFiltro.equals("Todos")) {
                if (!estadoFiltro.equalsIgnoreCase(tarea.getEstado())) return false;
            }

            // Filtro por prioridad
            if (prioridadFiltro != null && !prioridadFiltro.equals("Todas")) {
                if (!prioridadFiltro.equalsIgnoreCase(tarea.getPrioridad())) return false;
            }

            return true;
        };

        tareasFiltradas.setPredicate(filtro);
    }

    private void limpiarFiltros() {
        txtBuscar.clear();
        cbFiltroMateria.setValue("Todas las materias");
        cbFiltroEstado.setValue("Todos");
        cbFiltroPrioridad.setValue("Todas");
    }

    private void actualizarFiltroMaterias() {
        List<String> materias = tareaDAO.obtenerMateriasUnicas();
        ObservableList<String> items = FXCollections.observableArrayList("Todas las materias");
        items.addAll(materias);
        cbFiltroMateria.setItems(items);

        // Mantener selección actual si sigue existiendo
        String actual = cbFiltroMateria.getValue();
        if (actual != null && !items.contains(actual)) {
            cbFiltroMateria.setValue("Todas las materias");
        }
    }

    // ==================== UTILIDADES DE FORMULARIO ====================

    private void poblarFormulario(Tarea tarea) {
        txtTitulo.setText(tarea.getTitulo());
        txtDescripcion.setText(tarea.getDescripcion());
        txtMateria.setText(tarea.getMateria());
        dpFechaEntrega.setValue(tarea.getFechaEntrega());
        cbPrioridad.setValue(tarea.getPrioridad());
        cbEstado.setValue(tarea.getEstado());
    }

    private void limpiarFormulario() {
        txtTitulo.clear();
        txtDescripcion.clear();
        txtMateria.clear();
        dpFechaEntrega.setValue(null);
        cbPrioridad.setValue(Tarea.PRIORIDAD_MEDIA);
        cbEstado.setValue(Tarea.ESTADO_PENDIENTE);
    }

    private Tarea crearTareaDesdeFormulario() {
        return new Tarea(
                txtTitulo.getText().trim(),
                txtDescripcion.getText().trim(),
                txtMateria.getText().trim(),
                dpFechaEntrega.getValue(),
                cbPrioridad.getValue(),
                cbEstado.getValue()
        );
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (txtTitulo.getText() == null || txtTitulo.getText().trim().isEmpty()) {
            errores.append("• El título es obligatorio\n");
        }

        if (txtMateria.getText() == null || txtMateria.getText().trim().isEmpty()) {
            errores.append("• La materia es obligatoria\n");
        }

        if (dpFechaEntrega.getValue() == null) {
            errores.append("• La fecha de entrega es obligatoria\n");
        } else if (dpFechaEntrega.getValue().isBefore(LocalDate.now())
                && !Tarea.ESTADO_COMPLETADA.equals(cbEstado.getValue())) {
            // Advertencia, no error: permitir fecha pasada pero avisar
        }

        if (cbPrioridad.getValue() == null) {
            errores.append("• Seleccione una prioridad\n");
        }

        if (cbEstado.getValue() == null) {
            errores.append("• Seleccione un estado\n");
        }

        if (!errores.isEmpty()) {
            mostrarAlerta(Alert.AlertType.ERROR, "Validación fallida",
                    "Por corrija los siguientes errores:\n\n" + errores);
            return false;
        }

        return true;
    }

    private void actualizarBotonesParaEdicion(boolean enEdicion) {
        btnAgregar.setDisable(enEdicion);
        btnEditar.setText(enEdicion ? "Guardar Cambios" : "Editar");
        btnEliminar.setDisable(!enEdicion);

        // En modo edición, el botón limpiar funciona como "Cancelar"
        btnLimpiar.setText(enEdicion ? "Cancelar" : "Limpiar Campos");
    }

    // ==================== UTILIDADES ====================

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // Logger usando SLF4J
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
}