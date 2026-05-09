package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapistBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.*;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ComboBoxAutoCompleteUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SessionManagementController implements Initializable {


    @FXML
    private ComboBox<TherapySession> cmbSessionId;
    @FXML
    private TextField txtAllSessionSearch;
    @FXML
    private Button btnSessionPay;

    @FXML
    private ComboBox<Patient> cmbSessionPatient;
    @FXML
    private ComboBox<Therapist> cmbSessionTherapist;
    @FXML
    private ComboBox<TherapyProgram> cmbSessionProgram;
    @FXML
    private DatePicker dpSessionDate;
    @FXML
    private ComboBox<String> cmbSessionTime;
    @FXML
    private ComboBox<TherapySession.SessionStatus> cmbSessionStatus;
    @FXML
    private TextArea txtSessionNotes;

    @FXML
    private HBox hboxSessionActions;

    @FXML
    private VBox vboxPatientSessions;
    @FXML
    private TableView<TherapySession> tblPatientSessions;
    @FXML
    private TableColumn<TherapySession, Long> colPatSessionId;
    @FXML
    private TableColumn<TherapySession, Integer> colPatSequenceNumber;
    @FXML
    private TableColumn<TherapySession, String> colPatSessionDate;
    @FXML
    private TableColumn<TherapySession, String> colPatSessionTime;
    @FXML
    private TableColumn<TherapySession, String> colPatSessionTherapist;
    @FXML
    private TableColumn<TherapySession, String> colPatSessionProgram;
    @FXML
    private TableColumn<TherapySession, String> colPatSessionStatus;
    @FXML
    private TableColumn<TherapySession, String> colPatPaymentStatus;

    @FXML
    private DatePicker dpFilterDate;
    @FXML
    private ComboBox<String> cmbFilterTime;
    @FXML
    private ComboBox<Therapist> cmbFilterTherapist;
    @FXML
    private ComboBox<TherapyProgram> cmbFilterProgram;

    @FXML
    private TableView<TherapySession> tblSessions;
    @FXML
    private TableColumn<TherapySession, Long> colSessionId;
    @FXML
    private TableColumn<TherapySession, String> colSessionDate;
    @FXML
    private TableColumn<TherapySession, String> colSessionTime;
    @FXML
    private TableColumn<TherapySession, String> colSessionPatient;
    @FXML
    private TableColumn<TherapySession, String> colSessionTherapist;
    @FXML
    private TableColumn<TherapySession, String> colSessionProgram;
    @FXML
    private TableColumn<TherapySession, String> colSessionStatus;
    @FXML
    private TableColumn<TherapySession, Integer> colSequenceNumber;
    @FXML
    private TableColumn<TherapySession, String> colPaymentStatus;

    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();
    private final PatientBOImpl patientService = new PatientBOImpl();
    private final TherapistBOImpl therapistService = new TherapistBOImpl();
    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
    private TherapySession selectedSession;


    private List<TherapySession> allSessionsCache; // Cache to hold all sessions for filtering
    private List<Patient> allPatientsCache;
    private List<Therapist> allTherapistsCache;
    private List<TherapyProgram> allProgramsCache;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAllData();
        loadComboBoxes();
        setupTable();
        loadData();
        updateActionButtonsVisibility(false);
        vboxPatientSessions.setVisible(false);
        vboxPatientSessions.setManaged(false);

        setupTblSelection(tblSessions);
        setupTblSelection(tblPatientSessions);

        cmbSessionPatient.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                vboxPatientSessions.setVisible(true);
                vboxPatientSessions.setManaged(true);
                loadPatientSessions(newVal);
            } else {
                vboxPatientSessions.setVisible(false);
                vboxPatientSessions.setManaged(false);
                tblPatientSessions.setItems(FXCollections.observableArrayList());
            }
        });

        txtAllSessionSearch.textProperty().addListener((obs, oldVal, newVal) -> filterAllSessions());
        dpFilterDate.valueProperty().addListener((obs, o, n) -> filterAllSessions());
        cmbFilterTime.valueProperty().addListener((obs, o, n) -> filterAllSessions());
        cmbFilterTherapist.valueProperty().addListener((obs, o, n) -> filterAllSessions());
        cmbFilterProgram.valueProperty().addListener((obs, o, n) -> filterAllSessions());

    }

    private void setupTblSelection(TableView<TherapySession> tblPatientSessions) {
        tblPatientSessions.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedSession = n;
                populateForm(n);
                updateActionButtonsVisibility(true);

                cmbSessionId.setMouseTransparent(true);
                cmbSessionPatient.setMouseTransparent(true);
                cmbSessionProgram.setMouseTransparent(true);
                setDbFilteredData(n);

            }
        });
    }

    private void loadAllData(){
        allSessionsCache = sessionService.getAllSessions();
        allPatientsCache = patientService.getAllPatients();
        allTherapistsCache = therapistService.getAllTherapists();
        allProgramsCache = programService.getAllPrograms();
    }

    private void setDbFilteredData(TherapySession session){
        TherapyProgram program = session.getProgram();

        List<Therapist> therapists = new ArrayList<>();
        if (program != null) {
            Set<Therapist> assignedTherapists = program.getTherapists();
            if (assignedTherapists != null) {
                therapists.addAll(assignedTherapists);
            }
        }
        if (!therapists.isEmpty()) {
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionTherapist, therapists,
                    Therapist::getName, t -> t.getId() + " " + t.getName());

        }
    }

    private void loadComboBoxes() {
        try {
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionPatient, allPatientsCache,
                    Patient::getName, p -> p.getId() + " " + p.getName());
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionTherapist, allTherapistsCache,
                    Therapist::getName, t -> t.getId() + " " + t.getName());
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionProgram, allProgramsCache,
                    TherapyProgram::getName, p -> p.getId() + " " + p.getName());
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionId, allSessionsCache,
                    s -> s.getId() == null ? "" : String.valueOf(s.getId()), s -> s.getId() != null ? String.valueOf(s.getId()) : ""); // why error - because getId() can be null for new sessions, so we return empty string in that case
        } catch (Exception e) {
            System.err.println("Error loading combos: " + e.getMessage());
        }

        cmbSessionTime.setItems(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"));
        cmbFilterTime.setItems(FXCollections.observableArrayList(cmbSessionTime.getItems()));

        cmbSessionStatus.setItems(FXCollections.observableArrayList(TherapySession.SessionStatus.values()));
        cmbSessionStatus.setValue(TherapySession.SessionStatus.SCHEDULED);

        try {
            cmbFilterTherapist.setItems(FXCollections.observableArrayList(therapistService.getActiveTherapists()));
            cmbFilterProgram.setItems(FXCollections.observableArrayList(programService.getAllPrograms()));
        } catch (Exception e) {
        }
        setComboCellFactory(cmbFilterTherapist, Therapist::getName);
        setComboCellFactory(cmbFilterProgram, TherapyProgram::getName);
    }

    private <T> void setComboCellFactory(ComboBox<T> combo, Function<T, String> nameFunc) {
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameFunc.apply(item));
            }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
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

        // Setup Patient Sessions Table
        colPatSessionId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colPatSequenceNumber.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSequenceNumber()));
        colPatSessionDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""));
        colPatSessionTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
        colPatSessionTherapist.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTherapist() != null ? d.getValue().getTherapist().getName() : ""));
        colPatSessionProgram.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getProgram() != null ? d.getValue().getProgram().getName() : ""));
        colPatSessionStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""));
        colPatPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPaymentStatus() != null ? d.getValue().getPaymentStatus().name() : ""));

        // Status Badges
        Callback<TableColumn<TherapySession, String>, TableCell<TherapySession, String>> cellFactory = column -> new TableCell<TherapySession, String>() {
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
        };

        colPaymentStatus.setCellFactory(cellFactory);
        colPatPaymentStatus.setCellFactory(cellFactory);
    }

    private void loadData() {
        try {
            tblSessions.setItems(FXCollections.observableArrayList(sessionService.getAllSessions()));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load sessions: " + e.getMessage());
        }
    }

    private void loadPatientSessions(Patient patient) {
        try {
            List<TherapySession> all = sessionService.getAllSessions();
            List<TherapySession> filtered = all.stream()
                    .filter(s -> s.getPatient() != null && s.getPatient().getId().equals(patient.getId()))
                    .collect(Collectors.toList());
            tblPatientSessions.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            System.err.println("Error loading patient sessions: " + e.getMessage());
        }
    }

    private void filterAllSessions() {
        try {
            List<TherapySession> all = sessionService.getAllSessions();

            LocalDate fDate = dpFilterDate.getValue();
            String fTime = cmbFilterTime.getValue();
            Therapist fTherapist = cmbFilterTherapist.getValue();
            TherapyProgram fProgram = cmbFilterProgram.getValue();
            String searchBar = txtAllSessionSearch.getText().trim();

            List<TherapySession> filtered = all.stream()
                    .filter(s -> fDate == null || fDate.equals(s.getSessionDate()))
                    .filter(s -> fTime == null || fTime.equals(s.getSessionTime() != null ? s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null))
                    .filter(s -> fTherapist == null || (s.getTherapist() != null && fTherapist.getId().equals(s.getTherapist().getId())))
                    .filter(s -> fProgram == null || (s.getProgram() != null && fProgram.getId().equals(s.getProgram().getId())))
                    .filter(s -> searchBar.isEmpty() ||
                            (s.getPatient() != null && s.getPatient().getName().toLowerCase().contains(searchBar.toLowerCase())) ||
                            (s.getTherapist() != null && s.getTherapist().getName().toLowerCase().contains(searchBar.toLowerCase())) ||
                            (s.getProgram() != null && s.getProgram().getName().toLowerCase().contains(searchBar.toLowerCase())) ||
                            (s.getId() != null && String.valueOf(s.getId()).contains(searchBar.toLowerCase())))
                    .collect(Collectors.toList());

            tblSessions.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            System.err.println("Error filtering sessions: " + e.getMessage());
        }
    }

    @FXML
    void handleClearFilters(ActionEvent event) {
        dpFilterDate.setValue(null);
        cmbFilterTime.setValue(null);
        cmbFilterTherapist.setValue(null);
        cmbFilterProgram.setValue(null);
        loadData();
    }

    private void updateActionButtonsVisibility(boolean visible) {
        if (hboxSessionActions != null) {
            hboxSessionActions.setVisible(visible);
            hboxSessionActions.setManaged(visible);
        }
    }

    private void populateForm(TherapySession s) {
        cmbSessionId.setValue(s);
        cmbSessionPatient.setValue(s.getPatient());
        cmbSessionTherapist.setValue(s.getTherapist());
        cmbSessionProgram.setValue(s.getProgram());
        dpSessionDate.setValue(s.getSessionDate());
        if (s.getSessionTime() != null)
            cmbSessionTime.setValue(s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        cmbSessionStatus.setValue(s.getStatus() != null ? s.getStatus() : TherapySession.SessionStatus.SCHEDULED);
        txtSessionNotes.setText(s.getNotes());
    }

    @FXML
    void handleScheduleSession(ActionEvent event) {
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
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleUpdateSession(ActionEvent event) {
        if (selectedSession == null) {
            AlertUtil.showWarning("Warning", "Select a session first.");
            return;
        }
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
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleCompleteSession(ActionEvent event) {
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) s = tblPatientSessions.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Warning", "Select a session to complete.");
            return;
        }
        if (s.getStatus() == TherapySession.SessionStatus.COMPLETED) {
            AlertUtil.showWarning("Info", "Session is already completed.");
            return;
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
                if (cmbSessionPatient.getValue() != null) loadPatientSessions(cmbSessionPatient.getValue());
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleCancelSession(ActionEvent event) {
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) s = tblPatientSessions.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Warning", "Select a session to cancel/reschedule.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Cancel and return session to UNSCHEDULED status?")) {
            try {
                sessionService.cancelAndReschedule(s);
                AlertUtil.showInfo("Success", "Session cancelled and returned to unscheduled pool.");
                handleClearSession(event);
                loadData();
                if (cmbSessionPatient.getValue() != null) loadPatientSessions(cmbSessionPatient.getValue());
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleDeleteSession(ActionEvent event) {
        // We probably shouldn't allow raw deletion since they are pre-generated, but keeping for admin
        TherapySession s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) s = tblPatientSessions.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Warning", "Select a session to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete this session completely?")) {
            try {
                sessionService.deleteSession(s);
                handleClearSession(event);
                loadData();
                if (cmbSessionPatient.getValue() != null) loadPatientSessions(cmbSessionPatient.getValue());
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleClearSession(ActionEvent event) {
        cmbSessionPatient.setValue(null);
        cmbSessionTherapist.setValue(null);
        cmbSessionProgram.setValue(null);
        dpSessionDate.setValue(null);
        cmbSessionTime.setValue(null);
        cmbSessionStatus.setValue(TherapySession.SessionStatus.SCHEDULED);
        txtSessionNotes.clear();
        selectedSession = null;
        tblSessions.getSelectionModel().clearSelection();
        tblPatientSessions.getSelectionModel().clearSelection();
        updateActionButtonsVisibility(false);
        loadAllData();
        loadComboBoxes();
        cmbSessionId.setMouseTransparent(false);
        cmbSessionPatient.setMouseTransparent(false);
        cmbSessionProgram.setMouseTransparent(false);
    }

    private LocalTime parseTime() {
        String t = cmbSessionTime.getValue();
        return (t == null || t.isEmpty()) ? null : LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
