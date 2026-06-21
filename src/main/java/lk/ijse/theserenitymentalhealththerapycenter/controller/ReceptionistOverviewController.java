package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.*;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PatientTM;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.TherapySessionTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ReceptionistOverviewController implements Initializable {

    @FXML
    private Label lblTotalPatients;

    @FXML
    private Label lblTodaySessions;

    @FXML
    private TableView<TherapySessionTM> tblTodaySessions;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayDate;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayTime;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayPatient;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayPhone;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayTherapist;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayProgram;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayStatus;

    @FXML
    private TableView<PatientTM> tblUnscheduledPatients;

    @FXML
    private TableColumn<PatientTM, String> colUnscheduledId;

    @FXML
    private TableColumn<PatientTM, String> colUnscheduledName;

    @FXML
    private TableColumn<PatientTM, String> colUnscheduledPhone;

    @FXML
    private TableColumn<PatientTM, String> colUnscheduledEmail;

    @FXML
    private TableColumn<PatientTM, String> colUnscheduledDate;

    private final PatientBO patientService = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BOType.PATIENT);
    private final TherapySessionBO sessionService = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTables();
        loadStats();
        loadSessions();
        loadUnscheduledPatients();
    }

    private void setupTables() {
        colTodayDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""
        ));
        colTodayTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""
        ));
        colTodayPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colTodayPhone.setCellValueFactory(new PropertyValueFactory<>("patientPhone"));
        colTodayTherapist.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colTodayProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colTodayStatus.setCellValueFactory(new PropertyValueFactory<>("status"));


        colUnscheduledId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUnscheduledName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        colUnscheduledPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colUnscheduledEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUnscheduledDate.setCellValueFactory(new PropertyValueFactory<>("registeredDate"));
    }

    private void loadStats() {
        try {
            long patientCount = patientService.getPatientCount();
            lblTotalPatients.setText(String.valueOf(patientCount));
        } catch (Exception e) {
            System.err.println("Error loading receptionist stats: " + e.getMessage());
        }
    }

    private void loadSessions() {
        try {
            List<TherapySessionDTO> sessions = sessionService.getScheduledSessionsSortedByDate();
            List<TherapySessionTM> tms = sessions.stream().map(this::toTM).toList();

            tblTodaySessions.setItems(FXCollections.observableArrayList(tms));
            lblTodaySessions.setText(String.valueOf(tms.size()));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load scheduled sessions: " + e.getMessage());
        }
    }

    private void loadUnscheduledPatients() {
        try {
            List<PatientDTO> patients = patientService.getPatientsWithNoScheduledSessions();
            List<PatientTM> tms = patients.stream().map(this::toTM).toList();

            tblUnscheduledPatients.setItems(FXCollections.observableArrayList(tms));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load unscheduled patients: " + e.getMessage());
        }
    }

    private TherapySessionTM toTM(TherapySessionDTO d) {
        return new TherapySessionTM(
                d.getId() == 0 ? "" : "S" + String.format("%03d", d.getId()),
                d.getSequenceNumber(),
                d.getSessionDate(),
                d.getSessionTime(),
                d.getStatus() != null ? d.getStatus().name() : "",
                d.getPaymentStatus() != null ? d.getPaymentStatus().name() : "",
                d.getPatientName(),
                d.getTherapistName(),
                d.getProgramName(),
                d.getPatientPhone()
        );
    }

    private PatientTM toTM(PatientDTO d) {
        PatientTM tm = new PatientTM();
        tm.setId(d.getStringId());
        tm.setFirstName(d.getFirstName());
        tm.setLastName(d.getLastName());
        tm.setEmail(d.getEmail());
        tm.setPhone(d.getPhone());
        tm.setAddress(d.getAddress());
        tm.setRegisteredDate(d.getRegisteredDate() != null
                ? d.getRegisteredDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        return tm;
    }
}
