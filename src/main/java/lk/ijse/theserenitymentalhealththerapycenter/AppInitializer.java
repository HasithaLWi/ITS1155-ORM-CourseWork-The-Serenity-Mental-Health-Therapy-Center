package lk.ijse.theserenitymentalhealththerapycenter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;

public class AppInitializer extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        System.out.println("Initializing Hibernate SessionFactory...");
        FactoryConfiguration.getInstance();
        System.out.println("Hibernate initialized successfully!");



        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add(getClass().getResource(
                "/lk/ijse/theserenitymentalhealththerapycenter/style/calm-clarity.css").toExternalForm());

        stage.setTitle("Serenity Mental Health Therapy Center - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Stopping application resources...");
        lk.ijse.theserenitymentalhealththerapycenter.util.EmailService.shutdown();
        FactoryConfiguration.getInstance().close();
        System.out.println("Application resources cleaned up successfully.");
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
