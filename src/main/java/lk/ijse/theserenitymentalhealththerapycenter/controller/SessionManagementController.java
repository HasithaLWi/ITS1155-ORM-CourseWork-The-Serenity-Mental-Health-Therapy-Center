package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapistBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.*;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.TherapySessionTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ComboBoxAutoCompleteUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SessionManagementController implements Initializable {

    @FXML private ComboBox<TherapySessionDTO> cmbSessionId;
    @FXML private TextField txtAllSessionSearch;
    @FXML private Button btnSessionPay;

    @FXML private ComboBox<PatientDTO> cmbSessionPatient;
    @FXML private ComboBox<TherapistDTO> cmbSessionTherapist;
    @FXML private ComboBox<TherapyProgramDTO> cmbSessionProgram;
    @FXML private DatePicker dpSessionDate;
    @FXML private ComboBox<String> cmbSessionTime;
    @FXML private ComboBox<SessionStatus> cmbSessionStatus;
    @FXML private TextArea txtSessionNotes;
    @FXML private Label lblCreditInfo;
    @FXML private HBox hboxInlinePayment;
    @FXML private Label lblInlinePaymentPrice;
    @FXML private ComboBox<PaymentMethod> cmbInlinePaymentMethod;
    @FXML private Button btnCreateSession;
    @FXML private Button btnCompleteSession;
    @FXML private Button btnCancelSession;
    @FXML private Button btnUpdateSession;
    @FXML private HBox hboxSessionActions;
    @FXML private VBox vBoxAllSession;
    @FXML private VBox vboxPatientSessions;

    @FXML private TableView<TherapySessionTM> tblPatientSessions;
    @FXML private TableColumn<TherapySessionTM, String> colPatSessionId;
    @FXML private TableColumn<TherapySessionTM, Integer> colPatSequenceNumber;
    @FXML private TableColumn<TherapySessionTM, String> colPatSessionDate;
    @FXML private TableColumn<TherapySessionTM, String> colPatSessionTime;
    @FXML private TableColumn<TherapySessionTM, String> colPatSessionTherapist;
    @FXML private TableColumn<TherapySessionTM, String> colPatSessionProgram;
    @FXML private TableColumn<TherapySessionTM, String> colPatSessionStatus;
    @FXML private TableColumn<TherapySessionTM, String> colPatPaymentStatus;

    @FXML private DatePicker dpFilterDate;
    @FXML private ComboBox<String> cmbFilterTime;
    @FXML private ComboBox<TherapistDTO> cmbFilterTherapist;
    @FXML private ComboBox<TherapyProgramDTO> cmbFilterProgram;

    @FXML private TableView<TherapySessionTM> tblSessions;
    @FXML private TableColumn<TherapySessionTM, String> colSessionId;
    @FXML private TableColumn<TherapySessionTM, String> colSessionDate;
    @FXML private TableColumn<TherapySessionTM, String> colSessionTime;
    @FXML private TableColumn<TherapySessionTM, String> colSessionPatient;
    @FXML private TableColumn<TherapySessionTM, String> colSessionTherapist;
    @FXML private TableColumn<TherapySessionTM, String> colSessionProgram;
    @FXML private TableColumn<TherapySessionTM, String> colSessionStatus;
    @FXML private TableColumn<TherapySessionTM, Integer> colSequenceNumber;
    @FXML private TableColumn<TherapySessionTM, String> colPaymentStatus;

    @FXML private Label lblTimeAvailabilityMsg;
    @FXML private Label lblDateAvailabilityMsg;

    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();
    private final PatientBOImpl patientService = new PatientBOImpl();
    private final TherapistBOImpl therapistService = new TherapistBOImpl();
    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
    private final PaymentBOImpl paymentService = new PaymentBOImpl();

    private TherapySessionDTO selectedSession;
    private int currentCredit = 0;
    private boolean keepPatient = false;
    private boolean isScheduleValid = true;
    private boolean isSessionOnCurrentPatient = false;


    private List<TherapySessionDTO> allSessionsCache;
    private List<PatientDTO> allPatientsCache;
    private List<TherapistDTO> allTherapistsCache;
    private List<TherapyProgramDTO> allProgramsCache;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadAllData();
        loadComboBoxes();
        setupTable();
        setupSelectFromSessionId();
        loadData();
        
        updateActionButtonsVisibility(false, false);
        vboxPatientSessions.setVisible(false);
        vboxPatientSessions.setManaged(false);

        cmbSessionProgram.setVisible(false);
        cmbSessionStatus.setVisible(false);
        cmbSessionTime.setVisible(false);
        cmbSessionTherapist.setVisible(false);
        dpSessionDate.setVisible(false);

        cmbInlinePaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));

        setupTblSelection(tblSessions);
        setupTblSelection(tblPatientSessions);

        cmbSessionPatient.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                vboxPatientSessions.setVisible(true);
                vboxPatientSessions.setManaged(true);
                vBoxAllSession.setVisible(false);
                vBoxAllSession.setManaged(false);

                try {
                    List<PatientTherapyProgramDTO> enrollments = patientService.getPatientPrograms(newVal.getId());
                    List<TherapyProgramDTO> enrolledPrograms = enrollments.stream()
                            .map(PatientTherapyProgramDTO::getProgram).collect(Collectors.toList());

                    ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionProgram, enrolledPrograms,
                            TherapyProgramDTO::getName, p -> p.getId() + " " + p.getName());
                    cmbSessionProgram.setVisible(true);
                } catch (Exception e) {
                    System.err.println("Error loading patient programs: " + e.getMessage());
                }

                resetForm();
                loadPatientSessions(newVal);
            } else {
                vboxPatientSessions.setVisible(false);
                vboxPatientSessions.setManaged(false);
                vBoxAllSession.setVisible(true);
                vBoxAllSession.setManaged(true);
                tblPatientSessions.setItems(FXCollections.observableArrayList());

                try {
                    ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionProgram, allProgramsCache,
                            TherapyProgramDTO::getName, p -> p.getId() + " " + p.getName());
                } catch (Exception e) {
                    System.out.println("Error resetting program combo: " + e.getMessage());
                }
                cmbSessionProgram.setVisible(true);
            }
        });

        cmbSessionProgram.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && cmbSessionPatient.getValue() != null) {
                try {
                    PatientTherapyProgramDTO ptp = patientService.getPatientProgram(cmbSessionPatient.getValue().getId(), newVal.getId());
                    if (ptp != null) {
                        currentCredit = ptp.getRemainingCredit();
                        if (currentCredit > 0) {
                            lblCreditInfo.setText("Credit available: " + currentCredit + " sessions");
                            lblCreditInfo.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 11px; -fx-font-weight: bold;");
                        } else {
                            lblCreditInfo.setText("No upfront credit. Payment required.");
                            lblCreditInfo.setStyle("-fx-text-fill: #C47171; -fx-font-size: 11px; -fx-font-weight: bold;");
                        }

                        cmbSessionStatus.setVisible(true);
                        cmbSessionTime.setVisible(true);
                        cmbSessionTherapist.setVisible(true);
                        dpSessionDate.setVisible(true);

                        TherapyProgramDTO program = programService.getProgramById(newVal.getId());
                        List<TherapistDTO> therapistsForProgram = program.getTherapists();
                        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionTherapist, therapistsForProgram,
                                TherapistDTO::getName, t -> t.getId() + " " + t.getName());
                    } else {
                        currentCredit = 0;
                        lblCreditInfo.setText("");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionTherapist, allTherapistsCache,
                            TherapistDTO::getName, t -> t.getId() + " " + t.getName());
                } catch (Exception e) {
                    System.out.println("Error resetting therapist combo: " + e.getMessage());
                }
                cmbSessionStatus.setVisible(false);
                cmbSessionTime.setVisible(false);
                cmbSessionTherapist.setVisible(false);
                dpSessionDate.setVisible(false);
            }
        });

        dpSessionDate.valueProperty().addListener((obs, oldVal, newVal) -> {
            isSessionOnCurrentPatient = false;
            isScheduleValid = true;
            lblDateAvailabilityMsg.setText("");




        if (newVal != null) {
            if(selectedSession != null && selectedSession.getSessionDate().equals(newVal)) return;


            List<TherapySessionDTO> selectedDateSessions = allSessionsCache.stream()
                    .filter(s -> s.getSessionDate() != null && s.getSessionDate().equals(newVal))
                    .filter(s -> s.getTherapistId() != null && cmbSessionTherapist.getValue() != null && s.getTherapistId().equals(cmbSessionTherapist.getValue().getId()))
                .toList();
            if (!selectedDateSessions.isEmpty()) {
                lblDateAvailabilityMsg.setText(selectedDateSessions.size() + " session(s) already scheduled on this date.");
                isScheduleValid = true;

                if(selectedDateSessions.stream().anyMatch(s -> s.getPatientId() != null && cmbSessionPatient.getValue() != null && s.getPatientId().equals(cmbSessionPatient.getValue().getId()))) {
                    lblDateAvailabilityMsg.setText(lblDateAvailabilityMsg.getText() + " (Patient have a session on this date.)");
                    isScheduleValid = true;
                    isSessionOnCurrentPatient = true;

                }else if(selectedDateSessions.stream().anyMatch(s -> s.getSessionTime() != null && cmbSessionTime.getValue() != null && s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")).equals(cmbSessionTime.getValue()))) {
                    lblTimeAvailabilityMsg.setText("Selected time slot is not available for the chosen therapist.");
                    isScheduleValid = false;
                }
            } else {
                lblDateAvailabilityMsg.setText("");
                isScheduleValid = true;
            }
        }
        });

        cmbSessionTime.valueProperty().addListener((obs, oldVal, newVal) -> {
            isScheduleValid = true;
            lblTimeAvailabilityMsg.setText("");


            if (newVal != null) {

                if(selectedSession != null && selectedSession.getSessionTime() != null
                        && selectedSession.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")).equals(newVal)) return;

                List<TherapySessionDTO> selectedDateSessions = allSessionsCache.stream()
                        .filter(s -> s.getSessionTime() != null && s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")).equals(newVal))
                        .filter(s -> s.getSessionDate() != null && dpSessionDate.getValue() != null && s.getSessionDate().equals(dpSessionDate.getValue()))
                        .filter(s -> s.getTherapistId() != null && cmbSessionTherapist.getValue() != null && s.getTherapistId().equals(cmbSessionTherapist.getValue().getId()))
                        .toList();
                if (!selectedDateSessions.isEmpty()) {
                    lblTimeAvailabilityMsg.setText("Selected time slot is not available for the chosen therapist.");
                    isScheduleValid = false;

                } else {
                    lblTimeAvailabilityMsg.setText("");
                    isScheduleValid = true;

                }
            }
        });

        txtAllSessionSearch.textProperty().addListener((obs, oldVal, newVal) -> filterAllSessions());
        dpFilterDate.valueProperty().addListener((obs, o, n) -> filterAllSessions());
        cmbFilterTime.valueProperty().addListener((obs, o, n) -> filterAllSessions());
        cmbFilterTherapist.valueProperty().addListener((obs, o, n) -> filterAllSessions());
        cmbFilterProgram.valueProperty().addListener((obs, o, n) -> filterAllSessions());
    }

    private void resetForm() {
        cmbSessionTherapist.setValue(null);
        cmbSessionProgram.setValue(null);
        dpSessionDate.setValue(null);
        cmbSessionTime.setValue(null);
        cmbSessionStatus.setValue(SessionStatus.SCHEDULED);
        txtSessionNotes.clear();
        selectedSession = null;
        lblCreditInfo.setText("");
        currentCredit = 0;
        updateActionButtonsVisibility(false, false);
    }

    private void setupTblSelection(TableView<TherapySessionTM> tbl) {
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                TherapySessionDTO fullDTO = sessionService.getSessionById(Long.parseLong(n.getId().replace("S", "")));
                if (fullDTO != null) {
                    boolean needsPay = (fullDTO.getPaymentStatus() == SessionPaymentStatus.PENDING);

                    selectedSession = fullDTO;
                    populateForm(fullDTO);
                    selectedSession = fullDTO;

                    updateActionButtonsVisibility(true, needsPay);

                    cmbSessionId.setMouseTransparent(true);
                    cmbSessionPatient.setMouseTransparent(true);
                    cmbSessionProgram.setMouseTransparent(true);


                    lblCreditInfo.setText("");
                }
            }
        });
    }

    private void setupSelectFromSessionId() {
        cmbSessionId.valueProperty().addListener((obs, o, n) -> {
            if (n != null) {
                boolean needsPay = (n.getPaymentStatus() == SessionPaymentStatus.PENDING);

                selectedSession = n;
                populateForm(n);
                selectedSession = n;

                updateActionButtonsVisibility(true, needsPay);
                lblCreditInfo.setText("");

                Platform.runLater(() -> {
                    cmbSessionId.setMouseTransparent(true);
                });
                cmbSessionPatient.setMouseTransparent(true);
                cmbSessionProgram.setMouseTransparent(true);
            }
        });
    }

    private void loadAllData() {
        allSessionsCache = sessionService.getAllSessionDTOs();
        allPatientsCache = patientService.getAllPatients();
        allTherapistsCache = therapistService.getAllTherapists();
        allProgramsCache = programService.getAllPrograms();
    }

    private void loadComboBoxes() {
        try {
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionPatient, allPatientsCache,
                    PatientDTO::getName, p -> p.getId() + " " + p.getName());
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionTherapist, allTherapistsCache,
                    TherapistDTO::getName, t -> t.getId() + " " + t.getName());
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionProgram, allProgramsCache,
                    TherapyProgramDTO::getName, p -> p.getId() + " " + p.getName());
            ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionId, allSessionsCache,
                    s -> s.getId() == 0 ? "" : s.getStringId(),
                    s -> s.getId() == 0 ? "" : s.getStringId());
        } catch (Exception e) {
            System.err.println("Error loading combos: " + e.getMessage());
        }

        cmbSessionTime.setItems(FXCollections.observableArrayList(
                "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00"));
        cmbFilterTime.setItems(FXCollections.observableArrayList(cmbSessionTime.getItems()));

        cmbSessionStatus.setItems(FXCollections.observableArrayList(SessionStatus.values()));
        cmbSessionStatus.setValue(SessionStatus.SCHEDULED);

        try {
            cmbFilterTherapist.setItems(FXCollections.observableArrayList(allTherapistsCache));
            cmbFilterProgram.setItems(FXCollections.observableArrayList(allProgramsCache));
        } catch (Exception e) {}
        
        setComboCellFactory(cmbFilterTherapist, TherapistDTO::getName);
        setComboCellFactory(cmbFilterProgram, TherapyProgramDTO::getName);
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
                d.getProgramName()
        );
    }

    private void setupTable() {
        colSessionId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colSequenceNumber.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSequenceNumber()));
        colSessionDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""));
        colSessionTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
        colSessionPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));
        colSessionTherapist.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTherapistName()));
        colSessionProgram.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramName()));
        colSessionStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentStatus()));

        // Setup Patient Sessions Table
        colPatSessionId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colPatSequenceNumber.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSequenceNumber()));
        colPatSessionDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""));
        colPatSessionTime.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""));
        colPatSessionTherapist.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTherapistName()));
        colPatSessionProgram.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramName()));
        colPatSessionStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colPatPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentStatus()));

        // Status Badges
        Callback<TableColumn<TherapySessionTM, String>, TableCell<TherapySessionTM, String>> cellFactory = column -> new TableCell<>() {
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
            List<TherapySessionTM> list = sessionService.getAllSessionDTOs().stream().map(this::toTM).toList();
            tblSessions.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load sessions: " + e.getMessage());
        }
    }

    private void loadPatientSessions(PatientDTO patient) {
        try {
            List<TherapySessionTM> list = sessionService.getSessionsByPatient(patient.getId())
                    .stream().map(this::toTM).toList();
            tblPatientSessions.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            System.err.println("Error loading patient sessions: " + e.getMessage());
        }
    }

    private void filterAllSessions() {
        try {
            LocalDate fDate = dpFilterDate.getValue();
            String fTime = cmbFilterTime.getValue();
            TherapistDTO fTherapist = cmbFilterTherapist.getValue();
            TherapyProgramDTO fProgram = cmbFilterProgram.getValue();
            String searchBar = txtAllSessionSearch.getText().trim().toLowerCase();

            List<TherapySessionTM> filtered = allSessionsCache.stream()
                    .filter(s -> fDate == null || fDate.equals(s.getSessionDate()))
                    .filter(s -> fTime == null || fTime.equals(s.getSessionTime() != null ? s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null))
                    .filter(s -> fTherapist == null || (s.getTherapistId() != null && fTherapist.getId() == s.getTherapistId()))
                    .filter(s -> fProgram == null || (s.getProgramId() != null && fProgram.getId() == s.getProgramId()))
                    .filter(s -> searchBar.isEmpty() ||
                            (s.getPatientName() != null && s.getPatientName().toLowerCase().contains(searchBar)) ||
                            (s.getTherapistName() != null && s.getTherapistName().toLowerCase().contains(searchBar)) ||
                            (s.getProgramName() != null && s.getProgramName().toLowerCase().contains(searchBar)) ||
                            ("S" + String.format("%03d", s.getId())).toLowerCase().contains(searchBar))
                    .map(this::toTM)
                    .toList();

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
        txtAllSessionSearch.clear();
        loadData();
    }

    private void updateActionButtonsVisibility(boolean isExistingSession, boolean needsPay) {
        if (hboxSessionActions != null) {
            hboxSessionActions.setVisible(true);
            hboxSessionActions.setManaged(true);

            btnCreateSession.setVisible(!isExistingSession);
            btnCreateSession.setManaged(!isExistingSession);

            btnUpdateSession.setVisible(isExistingSession);
            btnUpdateSession.setManaged(isExistingSession);

            btnCompleteSession.setVisible(isExistingSession && !needsPay);
            btnCompleteSession.setManaged(isExistingSession && !needsPay);

            btnCancelSession.setVisible(isExistingSession && !needsPay);
            btnCancelSession.setManaged(isExistingSession && !needsPay);

            btnSessionPay.setVisible(isExistingSession && needsPay);
            btnSessionPay.setManaged(isExistingSession && needsPay);

            if (!needsPay || !isExistingSession) {
                hboxInlinePayment.setVisible(false);
                hboxInlinePayment.setManaged(false);
            }
        }
    }

    private void populateForm(TherapySessionDTO s) {
        cmbSessionId.setValue(s);
        
        cmbSessionPatient.setValue(s.getPatientId() != null ? allPatientsCache.stream().filter(p -> p.getId() == s.getPatientId()).findFirst().orElse(null) : null);
        cmbSessionTherapist.setValue(s.getTherapistId() != null ? allTherapistsCache.stream().filter(t -> t.getId() == s.getTherapistId()).findFirst().orElse(null) : null);
        cmbSessionProgram.setValue(s.getProgramId() != null ? allProgramsCache.stream().filter(p -> p.getId() == s.getProgramId()).findFirst().orElse(null) : null);
        
        dpSessionDate.setValue(s.getSessionDate());
        if (s.getSessionTime() != null)
            cmbSessionTime.setValue(s.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        else 
            cmbSessionTime.setValue(null);
            
        cmbSessionStatus.setValue(s.getStatus() != null ? s.getStatus() : SessionStatus.SCHEDULED);
        txtSessionNotes.setText(s.getNotes());
    }

    @FXML
    void handleCreateAndScheduleSession(ActionEvent event) {
        if (cmbSessionPatient.getValue() == null || cmbSessionProgram.getValue() == null) {
            AlertUtil.showWarning("Warning", "Patient and Program are required.");
            return;
        }
        if(cmbSessionTherapist.getValue() == null || cmbSessionTime.getValue() == null || dpSessionDate.getValue() == null) {
            AlertUtil.showWarning("Warning", "Therapist, Date and Time are required to schedule the session.");
            return;
        }

        if(!isScheduleValid && !isSessionOnCurrentPatient){
            AlertUtil.showWarning("Warning", "Therapist, Date and Time is invalid.");
            return;
        }

        try {
            TherapySessionDTO newSession = new TherapySessionDTO();
            newSession.setPatientId(cmbSessionPatient.getValue().getId());
            newSession.setProgramId(cmbSessionProgram.getValue().getId());
            if (cmbSessionTherapist.getValue() != null) {
                newSession.setTherapistId(cmbSessionTherapist.getValue().getId());
            }
            newSession.setSessionDate(dpSessionDate.getValue());
            newSession.setSessionTime(parseTime());
            newSession.setStatus(cmbSessionStatus.getValue());
            newSession.setNotes(txtSessionNotes.getText());

            TherapySessionDTO created = sessionService.createAndScheduleSession(newSession);

            if (created.getPaymentStatus() == SessionPaymentStatus.PENDING) {
                AlertUtil.showWarning("Payment Required",
                        "Session created but upfront credit was 0.\nPlease pay to schedule this session.");
                selectedSession = created;
                updateActionButtonsVisibility(true, true);
                btnUpdateSession.setVisible(false);
                btnUpdateSession.setManaged(false);
            } else {
                AlertUtil.showInfo("Success", "Session created and scheduled using upfront credit!");
            }
            
            loadAllData();
            loadData();
            loadPatientSessions(cmbSessionPatient.getValue());

            handleInlinePay(event);

        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleInlinePay(ActionEvent event) {
        if (selectedSession == null || selectedSession.getPaymentStatus() == SessionPaymentStatus.PAID) {
            return;
        }

        BigDecimal price = BigDecimal.ZERO;
        TherapyProgramDTO prog = allProgramsCache.stream().filter(p -> p.getId() == selectedSession.getProgramId()).findFirst().orElse(null);
        if (prog != null) {
            if (prog.getSessionFee() != null) {
                price = prog.getSessionFee();
            } else if (prog.getFee() != null) {
                int total = prog.getTotalSessions() != null ? prog.getTotalSessions() : 1;
                price = prog.getFee().divide(new BigDecimal(total), 2, java.math.RoundingMode.HALF_UP);
            }
        }
        if (lblInlinePaymentPrice != null) {
            lblInlinePaymentPrice.setText(price.toPlainString() + " LKR");
        }

        hboxInlinePayment.setVisible(true);
        hboxInlinePayment.setManaged(true);
        btnSessionPay.setVisible(false);
    }

    @FXML
    void handleCancelInlinePayment(ActionEvent event) {
        hboxInlinePayment.setVisible(false);
        hboxInlinePayment.setManaged(false);
        btnSessionPay.setVisible(true);
        cmbInlinePaymentMethod.setValue(null);
    }

    @FXML
    void handleProcessInlinePayment(ActionEvent event) {
        if (selectedSession == null) return;

        PaymentMethod method = cmbInlinePaymentMethod.getValue();
        if (method == null) {
            AlertUtil.showWarning("Warning", "Please select a payment method.");
            return;
        }

        try {
            PaymentDTO p = new PaymentDTO();
            p.setMethod(method);

            BigDecimal amount = BigDecimal.ZERO;
            TherapyProgramDTO prog = allProgramsCache.stream().filter(pr -> pr.getId() == selectedSession.getProgramId()).findFirst().orElse(null);
            if (prog != null) {
                if (prog.getSessionFee() != null) {
                    amount = prog.getSessionFee();
                } else if (prog.getFee() != null) {
                    int total = prog.getTotalSessions() != null ? prog.getTotalSessions() : 1;
                    amount = prog.getFee().divide(new BigDecimal(total), 2, java.math.RoundingMode.HALF_UP);
                }
            }
            p.setAmount(amount);

            paymentService.processSessionPayment(p, selectedSession.getId());

            selectedSession.setStatus(SessionStatus.SCHEDULED);
            sessionService.updateSession(selectedSession);

            AlertUtil.showInfo("Success", "Payment processed. Session is now SCHEDULED.");

            hboxInlinePayment.setVisible(false);
            hboxInlinePayment.setManaged(false);

            loadAllData();
            loadData();
            if (cmbSessionPatient.getValue() != null) {
                loadPatientSessions(cmbSessionPatient.getValue());
            }

            handleUpdateSession(event);
//            handleClearSession(event);

        } catch (Exception e) {
            AlertUtil.showError("Error", "Payment failed: " + e.getMessage());
        }
    }

    @FXML
    void handleUpdateSession(ActionEvent event) {
        if (selectedSession == null) {
            AlertUtil.showWarning("Warning", "Select a session first.");
            return;
        }

        try {
            selectedSession.setPatientId(cmbSessionPatient.getValue().getId());
            selectedSession.setTherapistId(cmbSessionTherapist.getValue() != null ? cmbSessionTherapist.getValue().getId() : null);
            selectedSession.setProgramId(cmbSessionProgram.getValue() != null ? cmbSessionProgram.getValue().getId() : null);
            selectedSession.setSessionDate(dpSessionDate.getValue());
            selectedSession.setSessionTime(parseTime());
            selectedSession.setStatus(cmbSessionStatus.getValue());
            selectedSession.setNotes(txtSessionNotes.getText());

            if(selectedSession.getStatus().equals(SessionStatus.SCHEDULED)) {
                if (sessionService.getSessionById(selectedSession.getId()).getPaymentStatus() == SessionPaymentStatus.PENDING) {
                    AlertUtil.showWarning("Payment Required",
                            "Session is scheduled but upfront credit is 0.\nPlease pay to confirm this session.");
                    updateActionButtonsVisibility(true, true);
                    btnUpdateSession.setVisible(false);
                    btnUpdateSession.setManaged(false);
                    return;
                } else if (selectedSession.getTherapistId() == null ||
                selectedSession.getSessionTime() == null || selectedSession.getSessionDate() == null) {
                    AlertUtil.showWarning("Warning", "Therapist, Date and Time are required to schedule the session.");
                    return;
                }else if(!isScheduleValid && !isSessionOnCurrentPatient){
                    AlertUtil.showWarning("Warning", "Therapist, Date and Time is invalid.");
                }
            }
            
            sessionService.updateSession(selectedSession);
            AlertUtil.showInfo("Success", "Session updated.");

            keepPatient = true;
            handleClearSession(event);


        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleCompleteSession(ActionEvent event) {
        TherapySessionTM s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) s = tblPatientSessions.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Warning", "Select a session to complete.");
            return;
        }
        if ("COMPLETED".equals(s.getStatus())) {
            AlertUtil.showWarning("Info", "Session is already completed.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Mark session as COMPLETED?")) {
            try {
                long sid = Long.parseLong(s.getId().replace("S", ""));
                TherapySessionDTO nextSession = sessionService.completeSession(sid);
                AlertUtil.showInfo("Success", "Session completed.");

                loadAllData();
                loadData();

//                if (nextSession != null) {
//                    if (AlertUtil.showConfirmation("Next Session", "Would you like to schedule the next session ("
//                            + nextSession.getSequenceNumber() + ") now?")) {
//                        selectedSession = nextSession;
//                        populateForm(nextSession);
//                    }
//                }

                if (cmbSessionPatient.getValue() != null) {
                    loadPatientSessions(cmbSessionPatient.getValue());
                }
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleCancelSession(ActionEvent event) {
        TherapySessionTM s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) s = tblPatientSessions.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Warning", "Select a session to cancel/reschedule.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Cancel and return session to UNSCHEDULED status?")) {
            try {
                long sid = Long.parseLong(s.getId().replace("S", ""));
                sessionService.cancelAndReschedule(sid);
                AlertUtil.showInfo("Success", "Session cancelled and returned to unscheduled pool.");
                handleClearSession(event);

                loadAllData();
                loadData();
                if (cmbSessionPatient.getValue() != null) {
                    loadPatientSessions(cmbSessionPatient.getValue());
                }
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleDeleteSession(ActionEvent event) {
        TherapySessionTM s = tblSessions.getSelectionModel().getSelectedItem();
        if (s == null) s = tblPatientSessions.getSelectionModel().getSelectedItem();
        if (s == null) {
            AlertUtil.showWarning("Warning", "Select a session to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete this session completely?")) {
            try {
                long sid = Long.parseLong(s.getId().replace("S", ""));
                sessionService.deleteSession(sid);
                handleClearSession(event);

                loadAllData();
                loadData();
                if (cmbSessionPatient.getValue() != null) {
                    loadPatientSessions(cmbSessionPatient.getValue());
                }
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleClearSession(ActionEvent event) {
        cmbSessionId.setValue(null);
        if (!keepPatient) {
            cmbSessionPatient.setValue(null);
        }
        keepPatient = false;

        cmbSessionTherapist.setValue(null);
        cmbSessionProgram.setValue(null);
        dpSessionDate.setValue(null);
        cmbSessionTime.setValue(null);
        cmbSessionStatus.setValue(SessionStatus.SCHEDULED);
        txtSessionNotes.clear();
        selectedSession = null;
        lblCreditInfo.setText("");
        lblTimeAvailabilityMsg.setText("");
        lblDateAvailabilityMsg.setText("");
        currentCredit = 0;

        tblSessions.getSelectionModel().clearSelection();
        tblPatientSessions.getSelectionModel().clearSelection();

        updateActionButtonsVisibility(false, false);

        loadAllData();
        loadComboBoxes();
        loadData();

        cmbSessionId.setMouseTransparent(false);
        cmbSessionPatient.setMouseTransparent(false);
        cmbSessionProgram.setMouseTransparent(false);

        hboxInlinePayment.setVisible(false);
        hboxInlinePayment.setManaged(false);
        cmbInlinePaymentMethod.setValue(null);

        cmbSessionProgram.setVisible(false);
        cmbSessionStatus.setVisible(false);
        cmbSessionTime.setVisible(false);
        cmbSessionTherapist.setVisible(false);
        dpSessionDate.setVisible(false);

isScheduleValid = true;
        isSessionOnCurrentPatient = false;

    }

    private LocalTime parseTime() {
        String t = cmbSessionTime.getValue();
        return (t == null || t.isEmpty()) ? null : LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm"));
    }
}
