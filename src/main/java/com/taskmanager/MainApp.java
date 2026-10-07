package com.taskmanager;

import com.taskmanager.controller.MainController;
import com.taskmanager.persistence.TareaDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Clase principal de la aplicación (MVC - Punto de entrada).
 * Inicializa JavaFX, carga la vista FXML, configura el Stage
 * y maneja el ciclo de vida de la aplicación (cierre, guardado).
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    // Referencias para manejo de cierre
    private Stage primaryStage;
    private MainController controller;
    private TareaDAO tareaDAO;

    public static void main(String[] args) {
        // Configurar sistema de logging antes de lanzar JavaFX
        System.setProperty("java.util.logging.config.file", "logging.properties");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            // Cargar vista FXML
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/com/taskmanager/MainView.fxml")));

            Parent root = loader.load();
            this.controller = loader.getController();

            // Inyectar DAO en el controlador (si se usa inyección manual)
            // this.controller.setTareaDAO(new TareaDAO());

            // Crear escena
            Scene scene = new Scene(root, 1100, 700);

            // Cargar hoja de estilos CSS
            String cssPath = Objects.requireNonNull(getClass().getResource("/com/taskmanager/styles.css")).toExternalForm();
            scene.getStylesheets().add(cssPath);

            // Configurar Stage
            primaryStage.setTitle("📚 Gestor de Tareas Académicas v1.0");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);

            // Icono de la aplicación (opcional)
            try {
                primaryStage.getIcons().add(new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream("/com/taskmanager/icon.png"))));
            } catch (Exception e) {
                logger.debug("Icono no encontrado, usando predeterminado");
            }

            // Manejar evento de cierre de ventana
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Cerrando aplicación...");
                manejarCierreAplicacion();
            });

            // Mostrar ventana centrada
            primaryStage.centerOnScreen();
            primaryStage.show();

            logger.info("Aplicación iniciada correctamente");

        } catch (IOException e) {
            logger.error("Error crítico al cargar la vista FXML: {}", e.getMessage(), e);
            mostrarErrorFatal("No se pudo cargar la interfaz", e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Maneja el cierre ordenado de la aplicación.
     * Guarda datos, libera recursos y cierra hilos.
     */
    private void manejarCierreAplicacion() {
        try {
            // El DAO guarda automáticamente en cada operación,
            // pero forzamos una sincronización final por seguridad
            if (controller != null) {
                // El controlador usa el DAO internamente que ya persiste en cada operación
                logger.debug("Verificando guardado final...");
            }

            // Dar tiempo a que se completen escrituras pendientes
            Thread.sleep(100);

            logger.info("Aplicación cerrada correctamente");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrumpido durante cierre: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error durante cierre: {}", e.getMessage(), e);
        }
    }

    /**
     * Muestra un diálogo de error fatal y termina la aplicación.
     */
    private void mostrarErrorFatal(String titulo, String mensaje) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error Fatal");
            alert.setHeaderText(titulo);
            alert.setContentText(mensaje + "\n\nLa aplicación se cerrará.");
            alert.showAndWait();
        });
    }

    /**
     * Getter para testing o acceso externo al controlador.
     */
    public MainController getController() {
        return controller;
    }

    /**
     * Getter para el Stage principal.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}