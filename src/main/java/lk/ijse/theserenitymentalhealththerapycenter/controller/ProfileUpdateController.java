package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.SessionContext;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileUpdateController implements Initializable {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtUsername;
    @FXML private TextField txtRole;
    
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private PasswordField txtCurrentPassword;
    
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOFactory.BOType.USER);
    private UserDTO currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadProfileData();
    }

    private void loadProfileData() {
        currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            txtFullName.setText(currentUser.getFullName());
            txtEmail.setText(currentUser.getEmail());
            txtUsername.setText(currentUser.getUsername());
            txtRole.setText(currentUser.getRole() != null ? currentUser.getRole().name() : "");
            
            // Clear passwords
            txtNewPassword.clear();
            txtConfirmPassword.clear();
            txtCurrentPassword.clear();
        } else {
            AlertUtil.showError("Session Error", "No active user session found. Please log in again.");
        }
    }

    @FXML
    void handleResetForm(ActionEvent event) {
        loadProfileData();
    }

    @FXML
    void handleSaveChanges(ActionEvent event) {
        if (currentUser == null) {
            AlertUtil.showError("Error", "No logged-in user found.");
            return;
        }

        String currentPassword = txtCurrentPassword.getText();
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            AlertUtil.showWarning("Verification Required", "You must enter your current password to authorize profile changes.");
            return;
        }

        String fullName = txtFullName.getText();
        String email = txtEmail.getText();
        String username = txtUsername.getText();
        
        if (fullName == null || fullName.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Full Name cannot be empty.");
            return;
        }
        if (username == null || username.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Username cannot be empty.");
            return;
        }

        String newPassword = txtNewPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                AlertUtil.showWarning("Validation Error", "New password must be at least 6 characters.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                AlertUtil.showWarning("Validation Error", "New password and confirmation password do not match.");
                return;
            }
        }

        try {
            UserDTO updatedDto = new UserDTO();
            updatedDto.setId(currentUser.getId());
            updatedDto.setRole(currentUser.getRole());
            updatedDto.setFullName(fullName.trim());
            updatedDto.setEmail(email != null ? email.trim() : "");
            updatedDto.setUsername(username.trim());

            userBO.updateProfile(updatedDto, currentPassword, newPassword);

            SessionContext.setCurrentUser(updatedDto);

            Scene scene = txtUsername.getScene();
            if (scene != null) {
                Label adminLabel = (Label) scene.lookup("#lblAdminName");
                if (adminLabel != null) {
                    adminLabel.setText(updatedDto.getFullName());
                }
                Label recepLabel = (Label) scene.lookup("#lblReceptionistName");
                if (recepLabel != null) {
                    recepLabel.setText(updatedDto.getFullName());
                }
            }

            AlertUtil.showInfo("Success", "Profile updated successfully!");

            loadProfileData();

        } catch (Exception e) {
            AlertUtil.showError("Update Failed", e.getMessage());
        }
    }
}
