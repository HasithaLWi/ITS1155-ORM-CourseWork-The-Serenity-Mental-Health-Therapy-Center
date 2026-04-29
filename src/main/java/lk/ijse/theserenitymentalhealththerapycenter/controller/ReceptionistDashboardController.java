package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReceptionistDashboardController implements Initializable {

    // ===== Sidebar =====
    @FXML private Button btnDashboard;
    @FXML private Button btnRegisterPatient;
    @FXML private Button btnPatientList;
    @FXML private Button btnScheduleSession;
    @FXML private Button btnPayments;

    // ===== Header =====
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;
    @FXML private Label lblCurrentDate;
    @FXML private Label lblReceptionistName;

    // ===== Dynamic Content Area =====
    @FXML private StackPane contentArea;

    private Button activeNavButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        activeNavButton = btnDashboard;
        loadSubPage("ReceptionistOverview.fxml");
    }

    // ===== Navigation =====
    @FXML
    void showDashboard(ActionEvent event) {
        setActivePage("Dashboard", "Welcome back", btnDashboard, "ReceptionistOverview.fxml");
    }

    @FXML
    void showRegisterPatient(ActionEvent event) {
        setActivePage("Register Patient", "Add a new patient record", btnRegisterPatient, "PatientRegistration.fxml");
    }

    @FXML
    void showPatientList(ActionEvent event) {
        setActivePage("Patient List", "View all patients", btnPatientList, "PatientList.fxml");
    }

    @FXML
    void showScheduleSession(ActionEvent event) {
        setActivePage("Schedule Session", "Book a therapy session", btnScheduleSession, "SessionManagement.fxml");
    }

    @FXML
    void showPayments(ActionEvent event) {
        setActivePage("Payments", "Process payments", btnPayments, "PaymentManagement.fxml");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/lk/ijse/theserenitymentalhealththerapycenter/view/Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            Scene scene = new Scene(root, 600, 500);
            stage.setScene(scene);
            stage.setTitle("Serenity - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertUtil.showError("Error", "Failed to load login page.");
            e.printStackTrace();
        }
    }

    // ===== Helper Methods =====
    private void setActivePage(String title, String subtitle, Button navButton, String fxmlFile) {
        lblPageTitle.setText(title);
        lblPageSubtitle.setText(subtitle);
        setActiveNavButton(navButton);
        loadSubPage(fxmlFile);
    }

    private void setActiveNavButton(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-btn-active");
            if (!activeNavButton.getStyleClass().contains("nav-btn")) {
                activeNavButton.getStyleClass().add("nav-btn");
            }
        }
        activeNavButton = button;
        button.getStyleClass().remove("nav-btn");
        if (!button.getStyleClass().contains("nav-btn-active")) {
            button.getStyleClass().add("nav-btn-active");
        }
    }

    private void loadSubPage(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/lk/ijse/theserenitymentalhealththerapycenter/view/" + fxmlFileName));
            Node page = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);
        } catch (IOException e) {
            contentArea.getChildren().clear();
            Label errorLabel = new Label("Failed to load: " + fxmlFileName);
            errorLabel.setStyle("-fx-text-fill: #C47171; -fx-font-size: 14px;");
            contentArea.getChildren().add(errorLabel);
            e.printStackTrace();
        }
    }
}
