/**
 * Definición del módulo Java para la aplicación TaskManager.
 * Exporta los paquetes necesarios y declara dependencias de módulos JavaFX.
 */
module com.taskmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;
    requires ch.qos.logback.classic;

    // Abrir paquetes para reflexión (FXML, Jackson, JavaFX)
    opens com.taskmanager to javafx.fxml, com.fasterxml.jackson.databind;
    opens com.taskmanager.controller to javafx.fxml;
    opens com.taskmanager.model to com.fasterxml.jackson.databind;
    opens com.taskmanager.persistence to com.fasterxml.jackson.databind;

    // Exportar API pública si fuera una biblioteca
    exports com.taskmanager;
    exports com.taskmanager.model;
    exports com.taskmanager.persistence;
    exports com.taskmanager.controller;
}