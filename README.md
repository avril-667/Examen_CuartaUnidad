# Gestor de Tareas Académicas - JavaFX

Aplicación de escritorio completa para la gestión de tareas académicas, desarrollada con **Java 17**, **JavaFX 17**, arquitectura **MVC** y persistencia en **JSON** (Jackson).

## 🚀 Características

- **CRUD Completo**: Crear, leer, actualizar, eliminar tareas
- **Persistencia Automática**: Guarda en `tareas.json` al cerrar/cada operación
- **Filtrado Avanzado**: Por texto, materia, estado, prioridad
- **Validación Robusta**: Campos obligatorios, fechas, alertas informativas
- **UI Moderna**: Tema personalizado CSS, colores por prioridad, filas vencidas destacadas
- **Arquitectura MVC**: Separación clara Modelo-Vista-Controlador
- **Thread-Safe**: DAO con ReadWriteLock para concurrencia

## 📋 Modelo de Datos (Tarea)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | String | UUID único (auto-generado) |
| `titulo` | String | Título de la tarea * |
| `descripcion` | String | Detalles opcionales |
| `materia` | String | Asignatura * |
| `fechaEntrega` | LocalDate | Fecha límite * |
| `prioridad` | String | Alta / Media / Baja * |
| `estado` | String | Pendiente / En Proceso / Completada * |

* = Campo obligatorio

## ⚠️ REQUISITO IMPORTANTE: Java 17+

**Esta aplicación REQUIERE Java 17 (JDK 17) o superior.**

- Java 8 **NO es compatible** (JavaFX 17+ requiere Java 11+)
- Verifique su versión: `java -version` debe mostrar `17.x.x` o superior
- Si tiene Java 8, instale JDK 17+ desde: https://adoptium.net/ o https://www.oracle.com/java/technologies/downloads/

## 🛠️ Requisitos

- **Java 17+** (JDK) - **OBLIGATORIO**
- **Gradle 8+** (incluido wrapper `gradlew` / `gradlew.bat`)

## ▶️ Ejecutar la Aplicación

### Opción 1: Con Gradle Wrapper (recomendado)

```bash
# Linux/macOS
./gradlew run

# Windows PowerShell/CMD
.\gradlew.bat run

# Solo compilar (genera JAR en build/libs/)
./gradlew build

# Ejecutar tests
./gradlew test

# Crear distribución portable en dist/
./gradlew createDistribution
```

### Opción 2: Desde IDE (IntelliJ IDEA / Eclipse / VS Code)

1. **Importar** como proyecto Gradle (File → Open → build.gradle)
2. **Configurar JDK 17** en Project Structure → SDK
3. **Ejecutar** `MainApp.main()`
4. **VM Options** (si el IDE no detecta módulos automáticamente):
   ```
   --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml
   ```
   Donde `PATH_TO_FX` es la ruta al SDK de JavaFX (ej: `/home/user/javafx-sdk-17/lib`)

### Opción 3: Java directo (JAR precompilado)

```bash
# Requiere JavaFX SDK 17 descargado por separado
java --module-path /ruta/javafx-sdk-17/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar build/libs/taskmanager-1.0.0.jar
```

## 📁 Estructura del Proyecto

```
src/main/java/com/taskmanager/
├── MainApp.java                 # Punto de entrada (Application)
├── module-info.java             # Definición de módulo JPMS
├── model/
│   └── Tarea.java               # Modelo con JavaFX Properties
├── controller/
│   └── MainController.java      # Lógica de presentación
└── persistence/
    └── TareaDAO.java            # Capa de datos (JSON/Jackson)

src/main/resources/com/taskmanager/
├── MainView.fxml                # Vista declarativa
├── styles.css                   # Tema visual personalizado
└── logging.properties           # Configuración de logs
```

## 🎯 Uso de la Aplicación

1. **Agregar tarea**: Rellene el formulario lateral → Click "➕ Agregar Tarea"
2. **Editar tarea**: Click en fila de la tabla → Modifique campos → Click "✏️ Editar" → "Guardar Cambios"
3. **Eliminar tarea**: Seleccione fila → Click "🗑️ Eliminar" → Confirme
4. **Limpiar formulario**: Click "🧹 Limpiar Campos" (deselecciona fila)
5. **Filtrar**: Use la barra superior (texto, materia, estado, prioridad)
6. **Persistencia**: Los datos se guardan automáticamente en `tareas.json`

## ⌨️ Atajos de Teclado

| Tecla | Acción |
|-------|--------|
| `Enter` en búsqueda | Aplicar filtro de texto |
| `Doble clic` en fila | Entrar en modo edición |
| `Tab` | Navegar entre campos |
| `Esc` en diálogo | Cancelar/cerrar |

## 🔧 Tecnologías Utilizadas

- **Java 17** - LTS, records, pattern matching, sealed classes
- **JavaFX 17** - UI declarativa (FXML) + controles modernos
- **Jackson 2.15** - Serialización JSON con soporte `LocalDate`
- **SLF4J + Logback** - Logging estructurado
- **Gradle 8** - Build automatizado con plugin JavaFX

## 📦 Distribución

Ejecutar `./gradlew createDistribution` genera una carpeta `dist/` portable:
```
dist/
├── run.bat          # Ejecutable Windows
├── run.sh           # Ejecutable Linux/macOS
└── lib/             # Todas las dependencias (JARs)
```

## 📝 Licencia

Proyecto educativo - Uso libre para aprendizaje y referencia.

---

**Desarrollado como ejemplo de arquitectura MVC en JavaFX** 🎓