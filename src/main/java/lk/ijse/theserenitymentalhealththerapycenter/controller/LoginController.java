package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.LoginException;
import lk.ijse.theserenitymentalhealththerapycenter.bo.UserService;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtPasswordVisible;

    @FXML
    private Button btnTogglePassword;

    @FXML
    private Label lblError;

    @FXML
    private HBox passwordContainer;

    private final UserService userService = new UserService();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        // Sync visible and hidden password fields
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);


        userService.createUserForFirstTime();
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        try {
            User user = userService.login(username, password);
            lblError.setText("");
            lblError.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 11px;");
            lblError.setText("Login successful! Loading dashboard...");

            // Route based on role
            String fxmlPath;
            String title;
            if (user.getRole() == User.Role.ADMIN) {
                fxmlPath = "/lk/ijse/theserenitymentalhealththerapycenter/view/AdminDashboard.fxml";
                title = "Serenity - Admin Dashboard";
            } else {
                fxmlPath = "/lk/ijse/theserenitymentalhealththerapycenter/view/ReceptionistDashboard.fxml";
                title = "Serenity - Receptionist Dashboard";
            }

            // Fade out transition
            String finalTitle = title;
            String finalFxmlPath = fxmlPath;
            FadeTransition fadeOut = new FadeTransition(Duration.millis(400), txtUsername.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(finalFxmlPath));
                    Parent root = loader.load();
                    Stage stage = (Stage) txtUsername.getScene().getWindow();
                    Scene scene = new Scene(root, 1280, 800);
                    stage.setScene(scene);
                    stage.setTitle(finalTitle);
                    stage.centerOnScreen();
                } catch (IOException ex) {
                    lblError.setStyle("-fx-text-fill: #C47171; -fx-font-size: 11px;");
                    lblError.setText("Failed to load dashboard.");
                    ex.printStackTrace();
                }
            });
            fadeOut.play();

        } catch (LoginException e) {
            lblError.setStyle("-fx-text-fill: #C47171; -fx-font-size: 11px;");
            lblError.setText(e.getMessage());

            // Shake animation for error
            shakeNode(lblError);
        }
    }

    @FXML
    void handleTogglePassword(ActionEvent event) {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            btnTogglePassword.setText("🙈");
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            btnTogglePassword.setText("👁");
        }
    }

    @FXML
    void handleForgotPassword(ActionEvent event) {
        navigateTo("/lk/ijse/theserenitymentalhealththerapycenter/view/ForgotPassword.fxml",
                "Serenity - Reset Password");
    }

    @FXML
    void handleCreateAccount(ActionEvent event) {
        navigateTo("/lk/ijse/theserenitymentalhealththerapycenter/view/Registration.fxml",
                "Serenity - Create Account");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            // Fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), txtUsername.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                Scene scene = new Scene(root, 1280, 800);
                stage.setScene(scene);
                stage.setTitle(title);
                stage.centerOnScreen();

                // Fade in
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (IOException e) {
            lblError.setText("Failed to load page.");
            e.printStackTrace();
        }
    }

    private void shakeNode(javafx.scene.Node node) {
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> node.setTranslateX(0));
        shake.play();
    }
}
