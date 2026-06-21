package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.enumaration.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.exception.LoginException;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.util.SessionContext;

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

    private final UserBO userService = (UserBO) BOFactory.getInstance().getBO(BOFactory.BOType.USER);
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {

        userService.createUserForFirstTime();


        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());
        txtPasswordVisible.setVisible(false);
        txtPasswordVisible.setManaged(false);


    }

    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        try {
            UserDTO user = userService.login(username, password);
            SessionContext.setCurrentUser(user);
            lblError.setText("");
            lblError.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 11px;");
            lblError.setText("Login successful! Loading dashboard...");

            // Route based on role
            String fxmlPath;
            String title;
            if (user.getRole() == UserRole.ADMIN) {
                fxmlPath = "/lk/ijse/theserenitymentalhealththerapycenter/view/AdminDashboard.fxml";
                title = "Serenity - Admin Dashboard";
            } else {
                fxmlPath = "/lk/ijse/theserenitymentalhealththerapycenter/view/ReceptionistDashboard.fxml";
                title = "Serenity - Receptionist Dashboard";
            }


            String finalTitle = title;
            String finalFxmlPath = fxmlPath;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(finalFxmlPath));
                Parent root = loader.load();
                Stage stage = (Stage) txtUsername.getScene().getWindow();
                Scene scene = new Scene(root, 1280, 720);
                stage.setScene(scene);
                stage.setTitle(finalTitle);
                stage.centerOnScreen();
            } catch (IOException ex) {
                lblError.setStyle("-fx-text-fill: #C47171; -fx-font-size: 11px;");
                lblError.setText("Failed to load dashboard.");
                ex.printStackTrace();
            }


        } catch (LoginException e) {
            lblError.setStyle("-fx-text-fill: #C47171; -fx-font-size: 11px;");
            lblError.setText(e.getMessage());
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
            btnTogglePassword.setText("\uD83D\uDC41\uFE0F\u200D\uD83D\uDDE8\uFE0F");
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            btnTogglePassword.setText("\uD83D\uDC41");
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

            Scene scene = new Scene(root, 1280, 720);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();


        } catch (IOException e) {
            lblError.setText("Failed to load page.");
            e.printStackTrace();
        }
    }

}
