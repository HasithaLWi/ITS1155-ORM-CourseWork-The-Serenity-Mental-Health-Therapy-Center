package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.*;
import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class SessionManagementController implements Initializable {

    @FXML private ComboBox<Patient> cmbSessionPatient;
    @FXML private ComboBox<Therapist> cmbSessionTherapist;
    @FXML private ComboBox<TherapyProgram> cmbSessionProgram;
    @FXML private DatePicker dpSessionDate;
    @FXML private ComboBox<String> cmbSessionTime;
    @FXML private ComboBox<TherapySession.SessionStatus> cmbSessionStatus;
    @FXML private TextArea txtSessionNotes;

    @FXML private TableView<TherapySession> tblSessions;
    @FXML private TableColumn<TherapySession, Long> colSessionId;
    @FXML private TableColumn<TherapySession, String> colSessionDate;
    @FXML private TableColumn<TherapySession, String> colSessionTime;
    @FXML private TableColumn<TherapySession, String> colSessionPatient;
    @FXML private TableColumn<TherapySession, String> colSessionTherapist;
    @FXML private TableColumn<TherapySession, String> colSessionProgram;
    @FXML private TableColumn<TherapySession, String> colSessionStatus;

    private final TherapySessionService sessionService = new TherapySessionService();
    private final PatientService patientService = new PatientService();
    private final TherapistService therapistService = new TherapistService();
    private final TherapyProgramService programService = new TherapyProgramService();
    private TherapySession selectedSession;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadComboBoxes();
        setupTable();
        loadData();
        tblSessions.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { selectedSession = n; populateForm(n); }
        });
    }

    private void loadComboBoxes() {
        try {
            cmbSessionPatient.setItems(FXCollections.observableArrayList(patientService.getAllPatients()));
            cmbSessionTherapist.setItems(FXCollections.observableArrayList(therapistService.getActiveTherapists()));
            cmbSessionProgram.setItems(FXCollections.observableArrayList(programService.getAllPrograms()));
        } catch (Exception e) { System.err.println("Error loading combos: " + e.getMessage()); }

        cmbSessionTime.setItems(FXCollections.observableArrayList(
            "08:00","08:30","09:00","09:30","10:00","10:30","11:00","11:30",
            "12:00","13:00","13:30","14:00","14:30","15:00","15:30","16:00","16:30","17:00"));
        cmbSessionStatus.setItems(FXCollections.observableArrayList(TherapySession.SessionStatus.values()));
        cmbSessionStatus.setValue(TherapySession.SessionStatus.SCHEDULED);

        setComboCellFactory(cmbSessionPatient, Patient::getName);
        setComboCellFactory(cmbSessionTherapist, Therapist::getName);
        setComboCellFactory(cmbSessionProgram, TherapyProgram::getName);
    }

    private <T> void setComboCellFactory(ComboBox<T> combo, java.util.function.Function<T, String> nameFunc) {
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameFunc.apply(item));
            }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameFunc.apply(item));
            }
        });
    }

    private void setupTable() {
        colSessionId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colSessionDate.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""));
        colSessionTime.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
        colSessionPatient.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPatient() != null ? d.getValue().getPatient().getName() : ""));
        colSessionTherapist.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getTherapist() != null ? d.getValue().getTherapist().getName() : ""));
        colSessionProgram.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getProgram() != null ? d.getValue().getProgram().getName() : ""));
        colSessionStatus.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""));
    }

    private void loadData() {
        try { tblSessions.setItems(FXCollections.observableArrayList(sessionService.getAllSessions())); }
        catch (Exception e) { AlertUtil.showError("Error", "Failed to load sessions: " + e.getMessage()); }
    }

    private void populateForm(TherapySession s) {
        cmbSessionPatient.setValue(s.getPatient());
        cmbSessionTherapist.setValue(s.getTherapist());
        cmbSessionProgram.setValue(s.getProgram());
        dpSessionDate.setValue(s.getSessionDate());
        if (s.getSessionTime() != null) cmbSessionTime.setValue(s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        cmbSessionStatus.setValue(s.getStatus());
        txtSessionNotes.setText(s.getNotes());
    }

    @FXML void handleScheduleSession(ActionEvent event) {
        try {
            TherapySession s = new TherapySession();
            s.setPatient(cmbSessionPatient.getValue());
            s.setTherapist(cmbSessionTherapist.getValue());
            s.setProgram(cmbSessionProgram.getValue());
            s.setSessionDate(dpSessionDate.getValue());
            s.setSessionTime(parseTime());
            s.setStatus(cmbSessionStatus.getValue());
            s.setNotes(txtSessionNotes.getText());
            sessionService.scheduleSession(s);
            AlertUtil.showInfo("Success", "Session scheduled.");
            handleClearSession(event);
            loadData();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void handleUpdateSession(ActionEvent event) {
        if (selectedSession == null) { AlertUtil.showWarning("Warning", "Select a session first."); return; }
        try {
            selectedSession.setPatient(cmbSessionPatient.getValue());
            selectedSession.setTherapist(cmbSessionTherapist.getValue());
            selectedSession.setProgram(cmbSessionProgram.getValue());
            selectedSession.setSessionDate(dpSessionDate.getValue());
            selectedSession.setSessionTime(parseTime());
            selectedSession.setStatus(cmbSessionStatus.getValue());
            selectedSession.setNotes(txtSessionNotes.getText());
            sessionService.updateSession(selectedSession);
            AlertUtil.showInfo("Success", "Session updated.");
            handleClearSession(event);
            loadData();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void handleDeleteSession(ActionEvent event) {
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) { AlertUtil.showWarning("Warning", "Select a session to delete."); return; }
        if (AlertUtil.showConfirmation("Confirm", "Delete this session?")) {
            try { sessionService.deleteSession(s); handleClearSession(event); loadData(); }
            catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }

    @FXML void handleClearSession(ActionEvent event) {
        cmbSessionPatient.setValue(null); cmbSessionTherapist.setValue(null);
        cmbSessionProgram.setValue(null); dpSessionDate.setValue(null);
        cmbSessionTime.setValue(null); cmbSessionStatus.setValue(TherapySession.SessionStatus.SCHEDULED);
        txtSessionNotes.clear(); selectedSession = null;
        tblSessions.getSelectionModel().clearSelection();
    }

    private LocalTime parseTime() {
        String t = cmbSessionTime.getValue();
        return (t == null || t.isEmpty()) ? null : LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
