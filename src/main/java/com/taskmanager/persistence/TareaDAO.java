package com.taskmanager.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.model.Tarea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Capa de acceso a datos (DAO) para la entidad Tarea.
 * Implementa persistencia en archivo JSON usando Jackson.
 * Thread-safe mediante ReadWriteLock para operaciones concurrentes.
 */
public class TareaDAO {

    private static final Logger logger = LoggerFactory.getLogger(TareaDAO.class);
    private static final String ARCHIVO_DATOS = "tareas.json";

    private final ObjectMapper objectMapper;
    private final File archivoDatos;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private List<Tarea> cacheTareas;

    /**
     * Constructor que inicializa el ObjectMapper con soporte para LocalDate
     * y carga los datos desde el archivo.
     */
    public TareaDAO() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.archivoDatos = new File(ARCHIVO_DATOS);
        this.cacheTareas = new ArrayList<>();
        cargar();
    }

    /**
     * Constructor que permite especificar una ruta personalizada para el archivo de datos.
     * Útil para testing o configuraciones personalizadas.
     */
    public TareaDAO(String rutaArchivo) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.archivoDatos = new File(rutaArchivo);
        this.cacheTareas = new ArrayList<>();
        cargar();
    }

    // ==================== OPERACIONES CRUD ====================

    /**
     * Obtiene todas las tareas almacenadas.
     * Retorna una copia defensiva para evitar modificaciones externas no controladas.
     */
    public List<Tarea> obtenerTodas() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(cacheTareas);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Busca una tarea por su ID único.
     */
    public Optional<Tarea> buscarPorId(String id) {
        lock.readLock().lock();
        try {
            return cacheTareas.stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Agrega una nueva tarea a la lista y persiste los cambios.
     * @return true si se agregó correctamente, false si ya existía una tarea con el mismo ID
     */
    public boolean agregar(Tarea tarea) {
        lock.writeLock().lock();
        try {
            if (cacheTareas.stream().anyMatch(t -> t.getId().equals(tarea.getId()))) {
                logger.warn("Intento de agregar tarea con ID duplicado: {}", tarea.getId());
                return false;
            }
            cacheTareas.add(tarea);
            guardar();
            logger.info("Tarea agregada: {}", tarea.getId());
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Actualiza una tarea existente.
     * @return true si se actualizó, false si no se encontró la tarea
     */
    public boolean actualizar(Tarea tarea) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < cacheTareas.size(); i++) {
                if (cacheTareas.get(i).getId().equals(tarea.getId())) {
                    cacheTareas.set(i, tarea);
                    guardar();
                    logger.info("Tarea actualizada: {}", tarea.getId());
                    return true;
                }
            }
            logger.warn("Intento de actualizar tarea inexistente: {}", tarea.getId());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Elimina una tarea por su ID.
     * @return true si se eliminó, false si no se encontró
     */
    public boolean eliminar(String id) {
        lock.writeLock().lock();
        try {
            boolean removed = cacheTareas.removeIf(t -> t.getId().equals(id));
            if (removed) {
                guardar();
                logger.info("Tarea eliminada: {}", id);
            } else {
                logger.warn("Intento de eliminar tarea inexistente: {}", id);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Elimina todas las tareas (limpieza completa).
     */
    public void eliminarTodas() {
        lock.writeLock().lock();
        try {
            cacheTareas.clear();
            guardar();
            logger.info("Todas las tareas eliminadas");
        } finally {
            lock.writeLock().unlock();
        }
    }

    // ==================== OPERACIONES DE BÚSQUEDA/FILTRADO ====================

    /**
     * Busca tareas que coincidan con el texto en título o descripción (case-insensitive).
     */
    public List<Tarea> buscarPorTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return obtenerTodas();
        }
        String busqueda = texto.toLowerCase().trim();
        lock.readLock().lock();
        try {
            return cacheTareas.stream()
                    .filter(t -> t.getTitulo().toLowerCase().contains(busqueda)
                            || t.getDescripcion().toLowerCase().contains(busqueda))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Filtra tareas por materia exacta.
     */
    public List<Tarea> filtrarPorMateria(String materia) {
        if (materia == null || materia.trim().isEmpty()) {
            return obtenerTodas();
        }
        lock.readLock().lock();
        try {
            return cacheTareas.stream()
                    .filter(t -> materia.equalsIgnoreCase(t.getMateria()))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Filtra tareas por estado exacto.
     */
    public List<Tarea> filtrarPorEstado(String estado) {
        if (estado == null || estado.trim().isEmpty()) {
            return obtenerTodas();
        }
        lock.readLock().lock();
        try {
            return cacheTareas.stream()
                    .filter(t -> estado.equalsIgnoreCase(t.getEstado()))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Filtra tareas por prioridad exacta.
     */
    public List<Tarea> filtrarPorPrioridad(String prioridad) {
        if (prioridad == null || prioridad.trim().isEmpty()) {
            return obtenerTodas();
        }
        lock.readLock().lock();
        try {
            return cacheTareas.stream()
                    .filter(t -> prioridad.equalsIgnoreCase(t.getPrioridad()))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Obtiene la lista de materias únicas registradas (para poblar ComboBox de filtro).
     */
    public List<String> obtenerMateriasUnicas() {
        lock.readLock().lock();
        try {
            return cacheTareas.stream()
                    .map(Tarea::getMateria)
                    .filter(m -> m != null && !m.trim().isEmpty())
                    .distinct()
                    .sorted()
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    // ==================== PERSISTENCIA ====================

    /**
     * Guarda la lista actual de tareas en el archivo JSON.
     * Lanza RuntimeException si falla la escritura (para que el llamador decida cómo manejarlo).
     */
    private void guardar() {
        try {
            // Escribir en archivo temporal y luego renombrar para atomicidad
            File tempFile = File.createTempFile("tareas", ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile, cacheTareas);

            // En Windows, delete() puede fallar si el archivo está en uso, así que usamos renameTo
            if (archivoDatos.exists()) {
                archivoDatos.delete();
            }
            if (!tempFile.renameTo(archivoDatos)) {
                // Fallback: copiar y borrar
                java.nio.file.Files.copy(tempFile.toPath(), archivoDatos.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                tempFile.delete();
            }
            logger.debug("Datos guardados en {}", archivoDatos.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Error guardando tareas: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo guardar el archivo de tareas", e);
        }
    }

    /**
     * Carga las tareas desde el archivo JSON.
     * Si el archivo no existe o está corrupto, inicializa una lista vacía.
     */
    private void cargar() {
        lock.writeLock().lock();
        try {
            if (!archivoDatos.exists()) {
                logger.info("Archivo de datos no existe, iniciando con lista vacía: {}", archivoDatos.getAbsolutePath());
                cacheTareas = new ArrayList<>();
                return;
            }

            try {
                List<Tarea> cargadas = objectMapper.readValue(archivoDatos, new TypeReference<List<Tarea>>() {});
                cacheTareas = cargadas != null ? cargadas : new ArrayList<>();
                logger.info("Cargadas {} tareas desde {}", cacheTareas.size(), archivoDatos.getAbsolutePath());
            } catch (IOException e) {
                logger.error("Error leyendo archivo de tareas (posible corrupción): {}", e.getMessage(), e);
                // Backup del archivo corrupto y empezar limpio
                File backup = new File(archivoDatos.getAbsolutePath() + ".corrupt." + System.currentTimeMillis());
                archivoDatos.renameTo(backup);
                logger.warn("Archivo corrupto movido a: {}", backup.getAbsolutePath());
                cacheTareas = new ArrayList<>();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Fuerza la recarga desde disco (útil si el archivo fue modificado externamente).
     */
    public void recargar() {
        cargar();
    }

    /**
     * Obtiene la ruta del archivo de datos para información/debugging.
     */
    public String getRutaArchivo() {
        return archivoDatos.getAbsolutePath();
    }

    /**
     * Verifica si hay datos guardados.
     */
    public boolean tieneDatos() {
        lock.readLock().lock();
        try {
            return !cacheTareas.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }
}