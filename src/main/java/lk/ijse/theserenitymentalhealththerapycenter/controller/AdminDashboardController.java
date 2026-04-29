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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    // ===== Sidebar Buttons =====
    @FXML private Button btnDashboard;
    @FXML private Button btnTherapists;
    @FXML private Button btnPrograms;
    @FXML private Button btnPatients;
    @FXML private Button btnScheduling;
    @FXML private Button btnPayments;
    @FXML private Button btnReports;

    // ===== Header =====
    @FXML private Label lblPageTitle;
    @FXML private Label lblPageSubtitle;
    @FXML private Label lblCurrentDate;
    @FXML private Label lblAdminName;

    // ===== Dynamic Content Area =====
    @FXML private StackPane contentArea;

    private Button activeNavButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        activeNavButton = btnDashboard;
        loadSubPage("AdminDashboardOverview.fxml");
    }

    // ===== Navigation Handlers =====
    @FXML
    void showDashboard(ActionEvent event) {
        setActivePage("Dashboard Overview", "Welcome back, Administrator", btnDashboard, "AdminDashboardOverview.fxml");
    }

    @FXML
    void showTherapistManagement(ActionEvent event) {
        setActivePage("Therapist Management", "Manage therapist records", btnTherapists, "TherapistManagement.fxml");
    }

    @FXML
    void showProgramManagement(ActionEvent event) {
        setActivePage("Therapy Programs", "Manage therapy programs", btnPrograms, "ProgramManagement.fxml");
    }

    @FXML
    void showPatientOverview(ActionEvent event) {
        setActivePage("Patient Overview", "View all patient records", btnPatients, "AdminPatientOverview.fxml");
    }

    @FXML
    void showScheduling(ActionEvent event) {
        setActivePage("Session Scheduling", "Manage therapy sessions", btnScheduling, "SessionManagement.fxml");
    }

    @FXML
    void showPayments(ActionEvent event) {
        setActivePage("Payments & Invoices", "Manage payment records", btnPayments, "PaymentManagement.fxml");
    }

    @FXML
    void showReports(ActionEvent event) {
        setActivePage("Reports & Analytics", "Generate and view reports", btnReports, "Reports.fxml");
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
