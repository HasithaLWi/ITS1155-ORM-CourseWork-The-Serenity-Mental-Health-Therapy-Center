package lk.ijse.theserenitymentalhealththerapycenter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.util.HibernateUtil;

public class AppInitializer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialize Hibernate on startup
        System.out.println("Initializing Hibernate SessionFactory...");
        HibernateUtil.getSessionFactory();
        System.out.println("Hibernate initialized successfully!");

        // Load Login screen
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/lk/ijse/theserenitymentalhealththerapycenter/view/ReceptionistDashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource(
                "/lk/ijse/theserenitymentalhealththerapycenter/view/calm-clarity.css").toExternalForm());

        stage.setTitle("Serenity Mental Health Therapy Center - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        // Shutdown Hibernate when app closes
        HibernateUtil.shutdown();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
