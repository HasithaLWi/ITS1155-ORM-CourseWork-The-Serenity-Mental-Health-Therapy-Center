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
import lk.ijse.theserenitymentalhealththerapycenter.dao.GenericDAO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    // ===== Stats Cards =====
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTotalTherapists;
    @FXML private Label lblTotalPrograms;
    @FXML private Label lblMonthlyRevenue;

    // ===== Therapist Table =====
    @FXML private TextField txtSearchTherapist;
    @FXML private TableView<Therapist> therapistTable;
    @FXML private TableColumn<Therapist, Long> colTherapistId;
    @FXML private TableColumn<Therapist, String> colTherapistName;
    @FXML private TableColumn<Therapist, String> colTherapistSpecialty;
    @FXML private TableColumn<Therapist, String> colTherapistStatus;
    @FXML private TableColumn<Therapist, Void> colTherapistActions;

    // ===== Program Table =====
    @FXML private TableView<TherapyProgram> programTable;
    @FXML private TableColumn<TherapyProgram, Long> colProgramId;
    @FXML private TableColumn<TherapyProgram, String> colProgramName;
    @FXML private TableColumn<TherapyProgram, String> colDuration;
    @FXML private TableColumn<TherapyProgram, BigDecimal> colFee;
    @FXML private TableColumn<TherapyProgram, Void> colProgramActions;

    // ===== DAOs =====
    private final GenericDAO<Therapist> therapistDAO = new GenericDAO<>(Therapist.class);
    private final GenericDAO<TherapyProgram> programDAO = new GenericDAO<>(TherapyProgram.class);
    private final GenericDAO<Patient> patientDAO = new GenericDAO<>(Patient.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set current date
        lblCurrentDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));

        // Setup table columns
        setupTherapistTable();
        setupProgramTable();

        // Load data
        loadDashboardData();
    }

    private void setupTherapistTable() {
        colTherapistId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colTherapistName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colTherapistSpecialty.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSpecialty()));
        colTherapistStatus.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getStatus() != null ? data.getValue().getStatus().name() : ""));
    }

    private void setupProgramTable() {
        colProgramId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colProgramName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colDuration.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));
        colFee.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getFee()));
    }

    private void loadDashboardData() {
        try {
            // Load stats
            List<Therapist> therapists = therapistDAO.getAll();
            List<TherapyProgram> programs = programDAO.getAll();
            long patientCount = patientDAO.count();

            lblTotalPatients.setText(String.valueOf(patientCount));
            lblTotalTherapists.setText(String.valueOf(therapists.size()));
            lblTotalPrograms.setText(String.valueOf(programs.size()));

            // Load tables
            therapistTable.setItems(FXCollections.observableArrayList(therapists));
            programTable.setItems(FXCollections.observableArrayList(programs));
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }

    // ===== Navigation Handlers =====
    @FXML void showDashboard(ActionEvent event) {
        lblPageTitle.setText("Dashboard Overview");
        lblPageSubtitle.setText("Welcome back, Administrator");
    }

    @FXML void showTherapistManagement(ActionEvent event) {
        lblPageTitle.setText("Therapist Management");
        lblPageSubtitle.setText("Manage therapist records");
    }

    @FXML void showProgramManagement(ActionEvent event) {
        lblPageTitle.setText("Therapy Programs");
        lblPageSubtitle.setText("Manage therapy programs");
    }

    @FXML void showPatientOverview(ActionEvent event) {
        lblPageTitle.setText("Patient Overview");
        lblPageSubtitle.setText("View all patient records");
    }

    @FXML void showScheduling(ActionEvent event) {
        lblPageTitle.setText("Session Scheduling");
        lblPageSubtitle.setText("Manage therapy sessions");
    }

    @FXML void showPayments(ActionEvent event) {
        lblPageTitle.setText("Payments & Invoices");
        lblPageSubtitle.setText("Manage payment records");
    }

    @FXML void showReports(ActionEvent event) {
        lblPageTitle.setText("Reports & Analytics");
        lblPageSubtitle.setText("Generate and view reports");
    }

    @FXML
    void showAddTherapistDialog(ActionEvent event) {
        AlertUtil.showInfo("Add Therapist", "Therapist form will be implemented here.");
    }

    @FXML
    void showAddProgramDialog(ActionEvent event) {
        AlertUtil.showInfo("Add Program", "Program form will be implemented here.");
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
