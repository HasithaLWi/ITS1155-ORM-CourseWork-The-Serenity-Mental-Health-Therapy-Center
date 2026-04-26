package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import lk.ijse.theserenitymentalhealththerapycenter.bo.UserService;
import lk.ijse.theserenitymentalhealththerapycenter.entity.User;
import lk.ijse.theserenitymentalhealththerapycenter.exception.RegistrationException;
import lk.ijse.theserenitymentalhealththerapycenter.util.ValidationUtil;

import java.io.IOException;

public class RegistrationController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<String> cmbSecurityQuestion;
    @FXML private TextField txtSecurityAnswer;

    @FXML private Label lblUsernameError;
    @FXML private Label lblPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label lblFullNameError;
    @FXML private Label lblEmailError;
    @FXML private Label lblSecurityQuestionError;
    @FXML private Label lblSecurityAnswerError;
    @FXML private Label lblMessage;

    private final UserService userService = new UserService();

    private static final String VALID_STYLE =
            "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #5A7A9A; "
            + "-fx-border-color: #4CAF50; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;";
    private static final String INVALID_STYLE =
            "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #5A7A9A; "
            + "-fx-border-color: #E07070; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;";
    private static final String DEFAULT_STYLE =
            "-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #FFFFFF; -fx-prompt-text-fill: #5A7A9A; "
            + "-fx-border-color: rgba(255,255,255,0.15); -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 13px;";

    @FXML
    public void initialize() {
        cmbSecurityQuestion.setItems(FXCollections.observableArrayList(
                "What is your pet's name?",
                "What city were you born in?",
                "What is your mother's maiden name?",
                "What was the name of your first school?",
                "What is your favorite book?"
        ));
        cmbSecurityQuestion.setStyle(DEFAULT_STYLE);

        txtUsername.textProperty().addListener((o, a, b) -> validateUsername());
        txtPassword.textProperty().addListener((o, a, b) -> { validatePassword(); if (!txtConfirmPassword.getText().isEmpty()) validateConfirmPassword(); });
        txtConfirmPassword.textProperty().addListener((o, a, b) -> validateConfirmPassword());
        txtFullName.textProperty().addListener((o, a, b) -> validateFullName());
        txtEmail.textProperty().addListener((o, a, b) -> validateEmail());
        txtSecurityAnswer.textProperty().addListener((o, a, b) -> validateSecurityAnswer());
        cmbSecurityQuestion.valueProperty().addListener((o, a, b) -> validateSecurityQuestion());
    }

    private boolean validateUsername() {
        String v = txtUsername.getText().trim();
        if (v.isEmpty()) { setErr(txtUsername, lblUsernameError, "Username is required"); return false; }
        if (v.length() < 3) { setErr(txtUsername, lblUsernameError, "At least 3 characters"); return false; }
        if (userService.usernameExists(v)) { setErr(txtUsername, lblUsernameError, "Username is already taken"); return false; }
        setOk(txtUsername, lblUsernameError); return true;
    }

    private boolean validatePassword() {
        String v = txtPassword.getText();
        if (v.isEmpty()) { setErr(txtPassword, lblPasswordError, "Password is required"); return false; }
        if (v.length() < 6) { setErr(txtPassword, lblPasswordError, "At least 6 characters"); return false; }
        setOk(txtPassword, lblPasswordError); return true;
    }

    private boolean validateConfirmPassword() {
        String v = txtConfirmPassword.getText();
        if (v.isEmpty()) { setErr(txtConfirmPassword, lblConfirmPasswordError, "Confirm your password"); return false; }
        if (!v.equals(txtPassword.getText())) { setErr(txtConfirmPassword, lblConfirmPasswordError, "Passwords do not match"); return false; }
        setOk(txtConfirmPassword, lblConfirmPasswordError); return true;
    }

    private boolean validateFullName() {
        String v = txtFullName.getText().trim();
        if (v.isEmpty()) { setErr(txtFullName, lblFullNameError, "Full name is required"); return false; }
        if (!ValidationUtil.isValidName(v)) { setErr(txtFullName, lblFullNameError, "Invalid name format"); return false; }
        setOk(txtFullName, lblFullNameError); return true;
    }

    private boolean validateEmail() {
        String v = txtEmail.getText().trim();
        if (v.isEmpty()) { setErr(txtEmail, lblEmailError, "Email is required"); return false; }
        if (!ValidationUtil.isValidEmail(v)) { setErr(txtEmail, lblEmailError, "Invalid email format"); return false; }
        if (userService.emailExists(v)) { setErr(txtEmail, lblEmailError, "Email already registered"); return false; }
        setOk(txtEmail, lblEmailError); return true;
    }

    private boolean validateSecurityQuestion() {
        if (cmbSecurityQuestion.getValue() == null) {
            lblSecurityQuestionError.setText("Select a security question");
            lblSecurityQuestionError.setStyle("-fx-text-fill: #E07070; -fx-font-size: 10px;");
            cmbSecurityQuestion.setStyle(INVALID_STYLE);
            return false;
        }
        lblSecurityQuestionError.setText("");
        cmbSecurityQuestion.setStyle(VALID_STYLE);
        return true;
    }

    private boolean validateSecurityAnswer() {
        String v = txtSecurityAnswer.getText().trim();
        if (v.isEmpty()) { setErr(txtSecurityAnswer, lblSecurityAnswerError, "Security answer is required"); return false; }
        if (v.length() < 2) { setErr(txtSecurityAnswer, lblSecurityAnswerError, "At least 2 characters"); return false; }
        setOk(txtSecurityAnswer, lblSecurityAnswerError); return true;
    }

    private void setErr(TextInputControl f, Label l, String m) { f.setStyle(INVALID_STYLE); l.setText(m); l.setStyle("-fx-text-fill: #E07070; -fx-font-size: 10px;"); }
    private void setOk(TextInputControl f, Label l) { f.setStyle(VALID_STYLE); l.setText("\u2713"); l.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 10px;"); }

    @FXML
    void handleRegister(ActionEvent event) {
        boolean valid = validateUsername() & validatePassword() & validateConfirmPassword()
                & validateFullName() & validateEmail() & validateSecurityQuestion() & validateSecurityAnswer();

        if (!valid) {
            lblMessage.setStyle("-fx-text-fill: #E07070; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblMessage.setText("Please fix the errors above.");
            return;
        }

        try {
            userService.register(txtUsername.getText().trim(), txtPassword.getText(),
                    txtFullName.getText().trim(), txtEmail.getText().trim(),
                    User.Role.RECEPTIONIST, cmbSecurityQuestion.getValue(), txtSecurityAnswer.getText().trim());

            lblMessage.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblMessage.setText("Account created successfully! Redirecting...");
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(e -> navigateToLogin());
            pause.play();
        } catch (RegistrationException e) {
            lblMessage.setStyle("-fx-text-fill: #E07070; -fx-font-size: 12px; -fx-font-weight: bold;");
            lblMessage.setText(e.getMessage());
        }
    }

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
        } catch (IOException e) { lblMessage.setText("Failed to load login page."); e.printStackTrace(); }
    }
}
