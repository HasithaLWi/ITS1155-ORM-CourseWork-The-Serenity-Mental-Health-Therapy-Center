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
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.exception.PasswordResetException;

import java.io.IOException;

public class ForgotPasswordController {


    @FXML
    private VBox step1Pane;
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtEmail;
    @FXML
    private Label lblStep1Error;


    @FXML
    private VBox step2Pane;
    @FXML
    private Label lblSecurityQuestion;
    @FXML
    private TextField txtSecurityAnswer;
    @FXML
    private Label lblStep2Error;


    @FXML
    private VBox step3Pane;
    @FXML
    private PasswordField txtNewPassword;
    @FXML
    private PasswordField txtConfirmNewPassword;
    @FXML
    private Label lblStep3Error;


    @FXML
    private Label step1Indicator;
    @FXML
    private Label step2Indicator;
    @FXML
    private Label step3Indicator;

    private final UserBO userService = (UserBO) BOFactory.getInstance().getBO(BOFactory.BOType.USER);
    private UserDTO verifiedUser;
    private int currentStep = 1;

    @FXML
    public void initialize() {
        showStep(1);
    }

    private void showStep(int step) {
        currentStep = step;
        step1Pane.setVisible(step == 1);
        step1Pane.setManaged(step == 1);
        step2Pane.setVisible(step == 2);
        step2Pane.setManaged(step == 2);
        step3Pane.setVisible(step == 3);
        step3Pane.setManaged(step == 3);

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


            String otp = lk.ijse.theserenitymentalhealththerapycenter.util.OtpManager.generateOtp(verifiedUser.getUsername());


            String subject = "Serenity Therapy - Password Reset OTP";
            String body = "Dear " + verifiedUser.getFullName() + ",\n\n"
                    + "We received a request to reset your password.\n"
                    + "Please use the following One-Time Password (OTP) to proceed with your password reset:\n\n"
                    + "OTP Code: " + otp + "\n\n"
                    + "This code is valid for 5 minutes. If you did not request a password reset, please ignore this email.\n\n"
                    + "Best regards,\n"
                    + "Serenity Mental Health Therapy Center Team";

            lk.ijse.theserenitymentalhealththerapycenter.util.EmailService.sendEmailAsync(email, subject, body);

            lblStep2Error.setText("");
            txtSecurityAnswer.clear();
            showStep(2);
        } catch (Exception e) {
            lblStep1Error.setText(e.getMessage());
        }
    }

    @FXML
    void handleVerifyAnswer(ActionEvent event) {
        if (verifiedUser == null) {
            lblStep2Error.setText("Session invalid. Please go back and verify identity.");
            return;
        }

        String enteredOtp = txtSecurityAnswer.getText().trim();
        if (enteredOtp.isEmpty()) {
            lblStep2Error.setText("Please enter the OTP.");
            return;
        }

        if (lk.ijse.theserenitymentalhealththerapycenter.util.OtpManager.validateOtp(verifiedUser.getUsername(), enteredOtp)) {
            lblStep3Error.setText("");
            showStep(3);
        } else {
            lblStep2Error.setText("Invalid or expired OTP. Please try again.");
        }
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
    void handleBackToStep1(ActionEvent event) {
        showStep(1);
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();

            Scene scene = new Scene(root, 1280, 720);
            stage.setScene(scene);
            stage.setTitle("Serenity - Login");
            stage.centerOnScreen();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
