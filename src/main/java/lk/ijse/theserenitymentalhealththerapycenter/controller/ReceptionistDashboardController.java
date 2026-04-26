package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.bo.PatientService;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // ===== Stats =====
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTodaySessions;

    // ===== Patient Table =====
    @FXML private TextField txtSearchPatient;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, Long> colPatientId;
    @FXML private TableColumn<Patient, String> colPatientName;
    @FXML private TableColumn<Patient, String> colPatientEmail;
    @FXML private TableColumn<Patient, String> colPatientPhone;
    @FXML private TableColumn<Patient, String> colPatientDate;

    private final PatientService patientService = new PatientService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));

        setupPatientTable();
        loadPatientData();
    }

    private void setupPatientTable() {
        colPatientId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colPatientName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colPatientEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colPatientPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colPatientDate.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getRegisteredDate() != null
                        ? data.getValue().getRegisteredDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        : ""));
    }

    private void loadPatientData() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            patientTable.setItems(FXCollections.observableArrayList(patients));
            lblTotalPatients.setText(String.valueOf(patients.size()));
        } catch (Exception e) {
            System.err.println("Error loading patients: " + e.getMessage());
        }
    }

    // ===== Navigation =====
    @FXML void showDashboard(ActionEvent event) {
        lblPageTitle.setText("Dashboard");
        lblPageSubtitle.setText("Welcome back");
    }

    @FXML void showRegisterPatient(ActionEvent event) {
        lblPageTitle.setText("Register Patient");
        lblPageSubtitle.setText("Add a new patient record");
    }

    @FXML void showPatientList(ActionEvent event) {
        lblPageTitle.setText("Patient List");
        lblPageSubtitle.setText("View all patients");
    }

    @FXML void showScheduleSession(ActionEvent event) {
        lblPageTitle.setText("Schedule Session");
        lblPageSubtitle.setText("Book a therapy session");
    }

    @FXML void showPayments(ActionEvent event) {
        lblPageTitle.setText("Payments");
        lblPageSubtitle.setText("Process payments");
    }

    @FXML void handleAddPatient(ActionEvent event) {
        AlertUtil.showInfo("Register Patient", "Patient registration form will be implemented here.");
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
}
