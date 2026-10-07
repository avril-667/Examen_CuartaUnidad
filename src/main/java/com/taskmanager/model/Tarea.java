package com.taskmanager.model;

import javafx.beans.property.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de datos que representa una Tarea académica.
 * Utiliza JavaFX Properties para facilitar el binding con la UI (TableView, Formularios).
 * Implementa Serializable para permitir persistencia mediante serialización de objetos.
 */
public class Tarea implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Propiedades JavaFX para data binding
    private final StringProperty id;
    private final StringProperty titulo;
    private final StringProperty descripcion;
    private final StringProperty materia;
    private final ObjectProperty<LocalDate> fechaEntrega;
    private final StringProperty prioridad;
    private final StringProperty estado;

    // Constantes para valores válidos
    public static final String PRIORIDAD_ALTA = "Alta";
    public static final String PRIORIDAD_MEDIA = "Media";
    public static final String PRIORIDAD_BAJA = "Baja";
    public static final String ESTADO_PENDIENTE = "Pendiente";
    public static final String ESTADO_EN_PROCESO = "En Proceso";
    public static final String ESTADO_COMPLETADA = "Completada";

    /**
     * Constructor completo para crear una nueva tarea.
     * Genera un ID único automáticamente.
     */
    public Tarea(String titulo, String descripcion, String materia,
                 LocalDate fechaEntrega, String prioridad, String estado) {
        this.id = new SimpleStringProperty(UUID.randomUUID().toString());
        this.titulo = new SimpleStringProperty(titulo);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.materia = new SimpleStringProperty(materia);
        this.fechaEntrega = new SimpleObjectProperty<>(fechaEntrega);
        this.prioridad = new SimpleStringProperty(prioridad);
        this.estado = new SimpleStringProperty(estado);
    }

    /**
     * Constructor por defecto necesario para deserialización (Jackson/Gson).
     * Inicializa todas las propiedades con valores vacíos/nulos.
     */
    public Tarea() {
        this.id = new SimpleStringProperty();
        this.titulo = new SimpleStringProperty();
        this.descripcion = new SimpleStringProperty();
        this.materia = new SimpleStringProperty();
        this.fechaEntrega = new SimpleObjectProperty<>();
        this.prioridad = new SimpleStringProperty();
        this.estado = new SimpleStringProperty();
    }

    // ==================== GETTERS DE PROPIEDADES (para binding) ====================

    public StringProperty idProperty() { return id; }
    public StringProperty tituloProperty() { return titulo; }
    public StringProperty descripcionProperty() { return descripcion; }
    public StringProperty materiaProperty() { return materia; }
    public ObjectProperty<LocalDate> fechaEntregaProperty() { return fechaEntrega; }
    public StringProperty prioridadProperty() { return prioridad; }
    public StringProperty estadoProperty() { return estado; }

    // ==================== GETTERS DE VALORES ====================

    public String getId() { return id.get(); }
    public String getTitulo() { return titulo.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public String getMateria() { return materia.get(); }
    public LocalDate getFechaEntrega() { return fechaEntrega.get(); }
    public String getPrioridad() { return prioridad.get(); }
    public String getEstado() { return estado.get(); }

    // ==================== SETTERS ====================

    public void setId(String id) { this.id.set(id); }
    public void setTitulo(String titulo) { this.titulo.set(titulo); }
    public void setDescripcion(String descripcion) { this.descripcion.set(descripcion); }
    public void setMateria(String materia) { this.materia.set(materia); }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega.set(fechaEntrega); }
    public void setPrioridad(String prioridad) { this.prioridad.set(prioridad); }
    public void setEstado(String estado) { this.estado.set(estado); }

    // ==================== MÉTODOS DE UTILIDAD ====================

    /**
     * Verifica si la tarea está vencida (fecha de entrega anterior a hoy y no completada).
     */
    public boolean estaVencida() {
        return fechaEntrega.get() != null
                && fechaEntrega.get().isBefore(LocalDate.now())
                && !ESTADO_COMPLETADA.equals(estado.get());
    }

    /**
     * Crea una copia profunda de la tarea (útil para edición sin confirmar).
     */
    public Tarea clonar() {
        Tarea copia = new Tarea();
        copia.setId(this.getId());
        copia.setTitulo(this.getTitulo());
        copia.setDescripcion(this.getDescripcion());
        copia.setMateria(this.getMateria());
        copia.setFechaEntrega(this.getFechaEntrega());
        copia.setPrioridad(this.getPrioridad());
        copia.setEstado(this.getEstado());
        return copia;
    }

    /**
     * Actualiza los valores de esta tarea con los de otra (para guardar cambios).
     */
    public void actualizarDesde(Tarea otra) {
        this.titulo.set(otra.getTitulo());
        this.descripcion.set(otra.getDescripcion());
        this.materia.set(otra.getMateria());
        this.fechaEntrega.set(otra.getFechaEntrega());
        this.prioridad.set(otra.getPrioridad());
        this.estado.set(otra.getEstado());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        return Objects.equals(id.get(), tarea.id.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id.get());
    }

    @Override
    public String toString() {
        return "Tarea{" +
                "id='" + id.get() + '\'' +
                ", titulo='" + titulo.get() + '\'' +
                ", materia='" + materia.get() + '\'' +
                ", fechaEntrega=" + fechaEntrega.get() +
                ", prioridad='" + prioridad.get() + '\'' +
                ", estado='" + estado.get() + '\'' +
                '}';
    }
}