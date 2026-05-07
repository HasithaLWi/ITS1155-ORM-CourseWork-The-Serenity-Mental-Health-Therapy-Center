package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapistBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
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
    @FXML private TableColumn<TherapySession, Integer> colSequenceNumber;
    @FXML private TableColumn<TherapySession, String> colPaymentStatus;

    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();
    private final PatientBOImpl patientService = new PatientBOImpl();
    private final TherapistBOImpl therapistService = new TherapistBOImpl();
    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
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
        colSequenceNumber.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSequenceNumber()));
        
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
            
        colPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPaymentStatus() != null ? d.getValue().getPaymentStatus().name() : ""));
            
        // Status Badges
        colPaymentStatus.setCellFactory(column -> new TableCell<TherapySession, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("badge-paid", "badge-pending");
                } else {
                    setText(item);
                    if ("PAID".equals(item)) {
                        getStyleClass().add("badge-paid");
                        getStyleClass().remove("badge-pending");
                    } else if ("PENDING".equals(item)) {
                        getStyleClass().add("badge-pending");
                        getStyleClass().remove("badge-paid");
                    }
                }
            }
        });
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
        cmbSessionStatus.setValue(s.getStatus() != null ? s.getStatus() : TherapySession.SessionStatus.SCHEDULED);
        txtSessionNotes.setText(s.getNotes());
    }

    @FXML void handleScheduleSession(ActionEvent event) {
        if (selectedSession == null) {
            AlertUtil.showWarning("Warning", "Select a pre-generated UNSCHEDULED session to schedule.");
            return;
        }
        
        if (selectedSession.getPaymentStatus() == TherapySession.PaymentStatus.PENDING) {
            // Ideally we'd pop up a pay-as-you-go dialog here, but for now we warn them.
            // AlertUtil.showWarning("Payment Required", "This session is PENDING. Please collect payment before scheduling.");
            // Or we allow it but they have to collect later. We will allow it with a warning.
            AlertUtil.showWarning("Warning", "Session scheduled, but payment is still PENDING.");
        }
        
        try {
            selectedSession.setPatient(cmbSessionPatient.getValue());
            selectedSession.setTherapist(cmbSessionTherapist.getValue());
            selectedSession.setProgram(cmbSessionProgram.getValue());
            selectedSession.setSessionDate(dpSessionDate.getValue());
            selectedSession.setSessionTime(parseTime());
            selectedSession.setStatus(cmbSessionStatus.getValue());
            selectedSession.setNotes(txtSessionNotes.getText());
            sessionService.scheduleSession(selectedSession);
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
    
    @FXML void handleCompleteSession(ActionEvent event) {
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) { AlertUtil.showWarning("Warning", "Select a session to complete."); return; }
        if (s.getStatus() == TherapySession.SessionStatus.COMPLETED) {
            AlertUtil.showWarning("Info", "Session is already completed."); return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Mark session as COMPLETED?")) {
            try { 
                TherapySession nextSession = sessionService.completeSession(s); 
                AlertUtil.showInfo("Success", "Session completed.");
                if (nextSession != null) {
                    if (AlertUtil.showConfirmation("Next Session", "Would you like to schedule the next session (" + nextSession.getSequenceNumber() + ") now?")) {
                        selectedSession = nextSession;
                        populateForm(nextSession);
                    }
                }
                loadData(); 
            }
            catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }
    
    @FXML void handleCancelSession(ActionEvent event) {
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) { AlertUtil.showWarning("Warning", "Select a session to cancel/reschedule."); return; }
        if (AlertUtil.showConfirmation("Confirm", "Cancel and return session to UNSCHEDULED status?")) {
            try { 
                sessionService.cancelAndReschedule(s); 
                AlertUtil.showInfo("Success", "Session cancelled and returned to unscheduled pool.");
                handleClearSession(event); 
                loadData(); 
            }
            catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }

    @FXML void handleDeleteSession(ActionEvent event) {
        // We probably shouldn't allow raw deletion since they are pre-generated, but keeping for admin
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) { AlertUtil.showWarning("Warning", "Select a session to delete."); return; }
        if (AlertUtil.showConfirmation("Confirm", "Delete this session completely?")) {
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
