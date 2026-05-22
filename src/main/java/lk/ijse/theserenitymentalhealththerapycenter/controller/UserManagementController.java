package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.UserBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.UserDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.UserRole;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.UserTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserManagementController implements Initializable {

    @FXML private TextField txtUsername;
    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<UserRole> cmbUserRole;
    @FXML private TextField txtSearchUser;

    @FXML private TableView<UserTM> tblUsers;
    @FXML private TableColumn<UserTM, String> colUserId;
    @FXML private TableColumn<UserTM, String> colUsername;
    @FXML private TableColumn<UserTM, String> colFullName;
    @FXML private TableColumn<UserTM, String> colEmail;
    @FXML private TableColumn<UserTM, String> colRole;

    private final UserBO userBO = (UserBO) BOFactory.getInstance().getBO(BOFactory.BOType.USER);
    private FilteredList<UserTM> filteredUsers;
    private UserTM selectedUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbUserRole.setItems(FXCollections.observableArrayList(UserRole.values()));
        cmbUserRole.setValue(UserRole.RECEPTIONIST);

        setupTable();
        loadData();
        setupSearch();

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedUser = newVal;
                populateForm(newVal);
            }
        });
    }

    private void setupTable() {
        colUserId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colFullName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colRole.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRole()));
    }

    private void loadData() {
        try {
            List<UserDTO> dtos = userBO.getAllUsers();
            List<UserTM> tms = dtos.stream().map(dto -> new UserTM(
                    dto.getStringId(),
                    dto.getUsername(),
                    dto.getFullName(),
                    dto.getEmail(),
                    dto.getRole() != null ? dto.getRole().name() : ""
            )).toList();
            filteredUsers = new FilteredList<>(FXCollections.observableArrayList(tms), p -> true);
            tblUsers.setItems(filteredUsers);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void setupSearch() {
        txtSearchUser.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredUsers != null) {
                filteredUsers.setPredicate(u -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (u.getUsername() != null && u.getUsername().toLowerCase().contains(lower))
                            || (u.getFullName() != null && u.getFullName().toLowerCase().contains(lower))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(lower));
                });
            }
        });
    }

    private void populateForm(UserTM u) {
        txtUsername.setText(u.getUsername());
        txtFullName.setText(u.getFullName());
        txtEmail.setText(u.getEmail());
        txtPassword.clear(); // Never populate password
        if (u.getRole() != null && !u.getRole().isEmpty()) {
            cmbUserRole.setValue(UserRole.valueOf(u.getRole()));
        }
    }

    @FXML
    void handleSaveUser(ActionEvent event) {
        try {
            String password = txtPassword.getText();
            if (password == null || password.trim().isEmpty()) {
                AlertUtil.showWarning("Warning", "Password is required for new users.");
                return;
            }
            userBO.register(
                    txtUsername.getText(),
                    password,
                    txtFullName.getText(),
                    txtEmail.getText(),
                    cmbUserRole.getValue()
            );
            AlertUtil.showInfo("Success", "User saved successfully.");
            handleClearUser(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleUpdateUser(ActionEvent event) {
        if (selectedUser == null) {
            AlertUtil.showWarning("Warning", "Please select a user to update.");
            return;
        }
        try {
            UserDTO dto = new UserDTO();
            // Parse the Long ID from the formatted String ID (e.g., "U001" -> 1)
            String rawId = selectedUser.getId();
            if (rawId != null && rawId.startsWith("U")) {
                dto.setId(Long.parseLong(rawId.substring(1)));
            }
            dto.setUsername(txtUsername.getText());
            dto.setFullName(txtFullName.getText());
            dto.setEmail(txtEmail.getText());
            dto.setRole(cmbUserRole.getValue());

            String password = txtPassword.getText();
            if (password != null && !password.trim().isEmpty()) {

                userBO.updateUserWithPassword(dto, password);
            } else {

                userBO.updateUser(dto);
            }
            AlertUtil.showInfo("Success", "User updated successfully.");
            handleClearUser(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleDeleteUser(ActionEvent event) {
        UserTM u = tblUsers.getSelectionModel().getSelectedItem();
        if (u == null) {
            AlertUtil.showWarning("Warning", "Please select a user to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete user \"" + u.getUsername() + "\"?")) {
            try {
                // Parse the Long ID from the formatted String ID
                String rawId = u.getId();
                long id = 0;
                if (rawId != null && rawId.startsWith("U")) {
                    id = Long.parseLong(rawId.substring(1));
                }
                userBO.deleteUser(id);
                AlertUtil.showInfo("Deleted", "User deleted.");
                handleClearUser(event);
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleClearUser(ActionEvent event) {
        txtUsername.clear();
        txtFullName.clear();
        txtEmail.clear();
        txtPassword.clear();
        cmbUserRole.setValue(UserRole.RECEPTIONIST);
        selectedUser = null;
        tblUsers.getSelectionModel().clearSelection();
    }
}
