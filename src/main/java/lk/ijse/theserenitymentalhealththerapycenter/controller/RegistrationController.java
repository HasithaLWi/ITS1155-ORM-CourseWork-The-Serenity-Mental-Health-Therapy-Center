package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.EmailService;
import lk.ijse.theserenitymentalhealththerapycenter.util.OtpManager;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.io.IOException;
import java.util.List;

public class RegistrationController {

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private PasswordField txtConfirmPassword;
    @FXML
    private TextField txtFullName;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField textAuthentication;

    @FXML
    private Label lblUsernameError;
    @FXML
    private Label lblPasswordError;
    @FXML
    private Label lblConfirmPasswordError;
    @FXML
    private Label lblFullNameError;
    @FXML
    private Label lblEmailError;
    @FXML
    private Label lblMessage;
    @FXML
    private Label lblAuthenticationInfo;
    @FXML
    private Label lblAuthenticationError;

    private final UserBO userService = (UserBO) BOFactory.getInstance().getBO(BOFactory.BOType.USER);

    private static final String VALID_STYLE =
            "-fx-background-color: #FFFFFF; -fx-text-fill: #2D3436; -fx-prompt-text-fill: #95A5A6; "
                    + "-fx-border-color: #7AB88F; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 11 16; -fx-font-size: 13px;";
    private static final String INVALID_STYLE =
            "-fx-background-color: #FFFFFF; -fx-text-fill: #2D3436; -fx-prompt-text-fill: #95A5A6; "
                    + "-fx-border-color: #C47171; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 11 16; -fx-font-size: 13px;";
    private static final String DEFAULT_STYLE =
            "-fx-background-color: #FFFFFF; -fx-text-fill: #2D3436; -fx-prompt-text-fill: #95A5A6; "
                    + "-fx-border-color: #E5DFD8; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 11 16; -fx-font-size: 13px;";

    @FXML
    public void initialize() {

        txtUsername.textProperty().addListener((o, a, b) -> validateUsername());
        txtPassword.textProperty().addListener((o, a, b) -> {
            validatePassword();
            if (!txtConfirmPassword.getText().isEmpty()) validateConfirmPassword();
        });
        txtConfirmPassword.textProperty().addListener((o, a, b) -> validateConfirmPassword());
        txtFullName.textProperty().addListener((o, a, b) -> validateFullName());
        txtEmail.textProperty().addListener((o, a, b) -> validateEmail());
        textAuthentication.textProperty().addListener((o, a, b) -> validateAuthentication());
    }

    private boolean validateUsername() {
        String v = txtUsername.getText().trim();
        if (v.isEmpty()) {
            setErr(txtUsername, lblUsernameError, "Username is required");
            return false;
        }
        if (v.length() < 3) {
            setErr(txtUsername, lblUsernameError, "At least 3 characters");
            return false;
        }
        if (userService.usernameExists(v)) {
            setErr(txtUsername, lblUsernameError, "Username is already taken");
            return false;
        }
        setOk(txtUsername, lblUsernameError);
        return true;
    }

    private boolean validatePassword() {
        String v = txtPassword.getText();
        if (v.isEmpty()) {
            setErr(txtPassword, lblPasswordError, "Password is required");
            return false;
        }
        if (v.length() < 6) {
            setErr(txtPassword, lblPasswordError, "At least 6 characters");
            return false;
        }
        setOk(txtPassword, lblPasswordError);
        return true;
    }

    private boolean validateConfirmPassword() {
        String v = txtConfirmPassword.getText();
        if (v.isEmpty()) {
            setErr(txtConfirmPassword, lblConfirmPasswordError, "Confirm your password");
            return false;
        }
        if (!v.equals(txtPassword.getText())) {
            setErr(txtConfirmPassword, lblConfirmPasswordError, "Passwords do not match");
            return false;
        }
        setOk(txtConfirmPassword, lblConfirmPasswordError);
        return true;
    }

    private boolean validateFullName() {
        String v = txtFullName.getText().trim();
        if (v.isEmpty()) {
            setErr(txtFullName, lblFullNameError, "Full name is required");
            return false;
        }
        if (!ValidationUtil.isValidName(v)) {
            setErr(txtFullName, lblFullNameError, "Invalid name format");
            return false;
        }
        setOk(txtFullName, lblFullNameError);
        return true;
    }

    private boolean validateEmail() {
        String v = txtEmail.getText().trim();
        if (v.isEmpty()) {
            setErr(txtEmail, lblEmailError, "Email is required");
            return false;
        }
        if (!ValidationUtil.isValidEmail(v)) {
            setErr(txtEmail, lblEmailError, "Invalid email format");
            return false;
        }
        if (userService.emailExists(v)) {
            setErr(txtEmail, lblEmailError, "Email already registered");
            return false;
        }
        setOk(txtEmail, lblEmailError);
        return true;
    }

    private boolean validateAuthentication() {
        String v = textAuthentication.getText().trim();
        if (v.isEmpty()) {
            setErr(textAuthentication, lblAuthenticationError, "Authentication code is required");
            return false;
        }
        if (!OtpManager.checkRegistrationCode(v)) {
            setErr(textAuthentication, lblAuthenticationError, "Invalid or expired authentication code");
            return false;
        }
        setOk(textAuthentication, lblAuthenticationError);
        return true;
    }

    @FXML
    void handleSendCodeToAdmin(ActionEvent event) {
        try {
            List<String> adminEmails = userService.getAdminEmail();
            if (adminEmails == null || adminEmails.isEmpty()) {
                AlertUtil.showError("Error", "No administrators found to send the authentication code.");
                return;
            }
            String code = OtpManager.generateRegistrationCode();

            String subject = "Serenity Therapy - Receptionist Registration Request";
            String body = "Dear Administrator,\n\n"
                    + "A receptionist has requested an authentication code to register an account.\n"
                    + "Please provide them with the following code:\n\n"
                    + "Authentication Code: " + code + "\n\n"
                    + "Best regards,\n"
                    + "Serenity Mental Health Therapy Center System";

            for (String email : adminEmails) {
                EmailService.sendEmailAsync(email, subject, body);
            }


            lblAuthenticationInfo.setText("Code sent to Admin.");
            lblAuthenticationInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #7AB88F;");
            lblAuthenticationError.setText("");
        } catch (Exception e) {
            lblAuthenticationError.setText("Failed to send authentication code.");
            e.printStackTrace();
        }
    }

    private void setErr(TextInputControl f, Label l, String m) {
        f.setStyle(INVALID_STYLE);
        l.setText(m);
        l.setStyle("-fx-text-fill: #C47171; -fx-font-size: 10px;");
    }

    private void setOk(TextInputControl f, Label l) {
        f.setStyle(VALID_STYLE);
        l.setText("\u2713");
        l.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 10px;");
    }

    @FXML
    void handleRegister(ActionEvent event) {
        boolean valid = validateUsername() & validatePassword() & validateConfirmPassword()
                & validateFullName() & validateEmail() & validateAuthentication();

        if (!valid) {
            lblMessage.setStyle("-fx-text-fill: #C47171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblMessage.setText("Please fix the errors above.");
            return;
        }


        OtpManager.consumeRegistrationCode(textAuthentication.getText().trim());

        try {
            userService.register(txtUsername.getText().trim(), txtPassword.getText(),
                    txtFullName.getText().trim(), txtEmail.getText().trim(),
                    UserRole.RECEPTIONIST);

            lblMessage.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblMessage.setText("Account created successfully! Redirecting...");
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> navigateToLogin());
            pause.play();
        } catch (RegistrationException e) {
            lblMessage.setStyle("-fx-text-fill: #C47171; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblMessage.setText(e.getMessage());
        }
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
            lblMessage.setText("Failed to load login page.");
            e.printStackTrace();
        }
    }
}
