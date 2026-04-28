package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.bo.UserService;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PasswordResetException;

import java.io.IOException;

public class ForgotPasswordController {

    // Step 1 - Identity
    @FXML private VBox step1Pane;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private Label lblStep1Error;

    // Step 2 - Security Question
    @FXML private VBox step2Pane;
    @FXML private Label lblSecurityQuestion;
    @FXML private TextField txtSecurityAnswer;
    @FXML private Label lblStep2Error;

    // Step 3 - New Password
    @FXML private VBox step3Pane;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmNewPassword;
    @FXML private Label lblStep3Error;

    // Step indicators
    @FXML private Label step1Indicator;
    @FXML private Label step2Indicator;
    @FXML private Label step3Indicator;

    private final UserService userService = new UserService();
    private User verifiedUser;
    private int currentStep = 1;

    @FXML
    public void initialize() {
        showStep(1);
    }

    private void showStep(int step) {
        currentStep = step;
        step1Pane.setVisible(step == 1); step1Pane.setManaged(step == 1);
        step2Pane.setVisible(step == 2); step2Pane.setManaged(step == 2);
        step3Pane.setVisible(step == 3); step3Pane.setManaged(step == 3);

        String active = "-fx-text-fill: #4A7FA5; -fx-font-size: 28px; -fx-font-weight: bold;";
        String inactive = "-fx-text-fill: #95A5A6; -fx-font-size: 28px;";
        String done = "-fx-text-fill: #7AB88F; -fx-font-size: 28px; -fx-font-weight: bold;";

        step1Indicator.setStyle(step == 1 ? active : (step > 1 ? done : inactive));
        step2Indicator.setStyle(step == 2 ? active : (step > 2 ? done : inactive));
        step3Indicator.setStyle(step == 3 ? active : inactive);

        step1Indicator.setText(step > 1 ? "\u2713" : "1");
        step2Indicator.setText(step > 2 ? "\u2713" : "2");
    }

    @FXML
    void handleVerifyIdentity(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            lblStep1Error.setText("Please enter both username and email.");
            return;
        }

        try {
            verifiedUser = userService.verifyIdentity(username, email);
            // Skip step 2 (security question not yet implemented) and go to password reset
            showStep(3);
        } catch (Exception e) {
            lblStep1Error.setText(e.getMessage());
        }
    }

    @FXML
    void handleVerifyAnswer(ActionEvent event) {
        // Security question not yet implemented — placeholder
        showStep(3);
    }





    @FXML
    void handleResetPassword(ActionEvent event) {
        String newPass = txtNewPassword.getText();
        String confirmPass = txtConfirmNewPassword.getText();

        if (newPass.isEmpty() || newPass.length() < 6) {
            lblStep3Error.setText("Password must be at least 6 characters.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            lblStep3Error.setText("Passwords do not match.");
            return;
        }

        try {
            userService.resetPassword(verifiedUser.getUsername(), newPass);
            lblStep3Error.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 11px;");
            lblStep3Error.setText("Password reset successful! Redirecting to login...");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> navigateToLogin());
            pause.play();
        } catch (PasswordResetException e) {
            lblStep3Error.setText(e.getMessage());
        }
    }

    @FXML
    void handleBackToStep1(ActionEvent event) { showStep(1); }

    @FXML
    void handleBackToLogin(ActionEvent event) { navigateToLogin(); }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), txtUsername.getScene().getRoot());
            fadeOut.setFromValue(1.0); fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                Scene scene = new Scene(root, 1280, 800);
                stage.setScene(scene); stage.setTitle("Serenity - Login"); stage.centerOnScreen();
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0.0); fadeIn.setToValue(1.0); fadeIn.play();
            });
            fadeOut.play();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
