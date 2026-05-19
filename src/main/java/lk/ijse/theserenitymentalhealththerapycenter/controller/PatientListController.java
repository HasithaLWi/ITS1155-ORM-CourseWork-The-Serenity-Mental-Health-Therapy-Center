package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PatientTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatientListController implements Initializable {

    // --- Edit form fields ---
    @FXML private TextField txtEditName;
    @FXML private TextField txtEditEmail;
    @FXML private TextField txtEditPhone;
    @FXML private TextField txtEditAddress;
    @FXML private TextArea txtEditInterviewNote;
    @FXML private TextField txtSearchPatient;

    // --- Patient table ---
    @FXML private TableView<PatientTM> tblPatients;
    @FXML private TableColumn<PatientTM, String> colPatientId;
    @FXML private TableColumn<PatientTM, String> colPatientName;
    @FXML private TableColumn<PatientTM, String> colPatientEmail;
    @FXML private TableColumn<PatientTM, String> colPatientPhone;
    @FXML private TableColumn<PatientTM, String> colPatientAddress;
    @FXML private TableColumn<PatientTM, String> colPatientDate;

    // --- Enrolled programs section ---
    @FXML private VBox vboxEnrolledPrograms;
    @FXML private TableView<EnrolledProgramRow> tblEnrolledPrograms;
    @FXML private TableColumn<EnrolledProgramRow, String> colEnrProgramName;
    @FXML private TableColumn<EnrolledProgramRow, Integer> colEnrTotalSessions;
    @FXML private TableColumn<EnrolledProgramRow, Integer> colEnrUpfrontPaid;
    @FXML private TableColumn<EnrolledProgramRow, Integer> colEnrUsed;
    @FXML private TableColumn<EnrolledProgramRow, Integer> colEnrRemaining;
    @FXML private TableColumn<EnrolledProgramRow, Integer> colEnrCompleted;
    @FXML private TableColumn<EnrolledProgramRow, String> colEnrStatus;

    // --- Enroll new program controls ---
    @FXML private ComboBox<TherapyProgramDTO> cmbNewProgram;
    @FXML private ComboBox<Integer> cmbNewProgramSessions;
    @FXML private Label lblNewProgramCost;
    @FXML private ComboBox<PaymentMethod> cmbNewProgramPayMethod;

    private final PatientBOImpl patientService = new PatientBOImpl();
    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();
    private final PaymentBOImpl paymentService = new PaymentBOImpl();

    private FilteredList<PatientTM> filteredPatients;
    private PatientTM selectedPatient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupEnrolledProgramsTable();
        loadData();
        setupSearch();

        // Payment method combo
        cmbNewProgramPayMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));

        // Program combo cell factory
        setComboCellFactory(cmbNewProgram, TherapyProgramDTO::getName);

        // When program combo changes, populate session count options
        cmbNewProgram.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int total = newVal.getTotalSessions() != null ? newVal.getTotalSessions() : 1;
                ObservableList<Integer> opts = FXCollections.observableArrayList();
                for (int i = 0; i <= total; i++) opts.add(i);
                cmbNewProgramSessions.setItems(opts);
                cmbNewProgramSessions.setValue(total);
                updateNewProgramCost();
            } else {
                cmbNewProgramSessions.setItems(FXCollections.observableArrayList());
                lblNewProgramCost.setText("0 LKR");
            }
        });

        // When session count changes, update cost
        cmbNewProgramSessions.valueProperty().addListener((obs, o, n) -> updateNewProgramCost());

        // Patient selection listener
        tblPatients.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedPatient = n;
                populateForm(n);
                loadEnrolledPrograms(n);
                loadAvailablePrograms(n);
                vboxEnrolledPrograms.setVisible(true);
                vboxEnrolledPrograms.setManaged(true);
            } else {
                vboxEnrolledPrograms.setVisible(false);
                vboxEnrolledPrograms.setManaged(false);
            }
        });
    }

    // ===================== TABLE SETUP =====================

    private void setupTable() {
        colPatientId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colPatientName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colPatientEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPatientPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colPatientAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
        colPatientDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegisteredDate()));
    }

    private void setupEnrolledProgramsTable() {
        colEnrProgramName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programName));
        colEnrTotalSessions.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().totalSessions));
        colEnrUpfrontPaid.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().upfrontPaid));
        colEnrUsed.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().sessionsUsed));
        colEnrRemaining.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().creditRemaining));
        colEnrCompleted.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().completedSessions));
        colEnrStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().status));

        // Status badge styling
        colEnrStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("COMPLETED".equals(item)) {
                        setStyle("-fx-text-fill: #7AB88F; -fx-font-weight: bold;");
                    } else if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: #4A7FA5; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2D3436;");
                    }
                }
            }
        });
    }

    // ===================== DATA LOADING =====================

    private void loadData() {
        try {
            List<PatientDTO> dtos = patientService.getAllPatients();
            List<PatientTM> tms = dtos.stream().map(dto -> {
                PatientTM tm = new PatientTM();
                tm.setId(dto.getStringId());
                tm.setName(dto.getName());
                tm.setEmail(dto.getEmail());
                tm.setPhone(dto.getPhone());
                tm.setAddress(dto.getAddress());
                tm.setRegisteredDate(dto.getRegisteredDate() != null
                        ? dto.getRegisteredDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
                return tm;
            }).toList();
            filteredPatients = new FilteredList<>(FXCollections.observableArrayList(tms), p -> true);
            tblPatients.setItems(filteredPatients);
        } catch (Exception e) { AlertUtil.showError("Error", "Failed to load patients: " + e.getMessage()); }
    }

    private void setupSearch() {
        txtSearchPatient.textProperty().addListener((obs, o, n) -> {
            if (filteredPatients != null) {
                filteredPatients.setPredicate(p -> {
                    if (n == null || n.isEmpty()) return true;
                    String lower = n.toLowerCase();
                    return (p.getName() != null && p.getName().toLowerCase().contains(lower))
                        || (p.getPhone() != null && p.getPhone().contains(lower));
                });
            }
        });
    }

    /**
     * Load enrolled programs for the selected patient into the programs table.
     */
    private void loadEnrolledPrograms(PatientTM patient) {
        try {
            long patientId = parsePatientId(patient.getId());
            List<PatientTherapyProgramDTO> enrollments = patientService.getPatientPrograms(patientId);
            ObservableList<EnrolledProgramRow> rows = FXCollections.observableArrayList();

            for (PatientTherapyProgramDTO ptp : enrollments) {
                int totalSessions = ptp.getTotalSessions();
                long completed = sessionService.countCompletedByPatientAndProgram(patientId, ptp.getProgramId());

                String status;
                if (completed >= totalSessions && totalSessions > 0) {
                    status = "COMPLETED";
                } else {
                    status = "ACTIVE";
                }

                rows.add(new EnrolledProgramRow(
                    ptp.getProgramName(),
                    totalSessions,
                    ptp.getUpfrontSessionsPaid(),
                    ptp.getSessionsUsed(),
                    ptp.getRemainingCredit(),
                    (int) completed,
                    status
                ));
            }
            tblEnrolledPrograms.setItems(rows);
        } catch (Exception e) {
            System.err.println("Error loading enrolled programs: " + e.getMessage());
        }
    }

    /**
     * Populate the "Enroll in New Program" combo with programs that the patient
     * is NOT currently enrolled in, or has fully completed (all sessions done).
     */
    private void loadAvailablePrograms(PatientTM patient) {
        try {
            long patientId = parsePatientId(patient.getId());
            List<TherapyProgramDTO> allPrograms = programService.getAllPrograms();
            List<PatientTherapyProgramDTO> enrollments = patientService.getPatientPrograms(patientId);

            List<TherapyProgramDTO> available = allPrograms.stream().filter(prog -> {
                PatientTherapyProgramDTO existing = enrollments.stream()
                        .filter(e -> e.getProgramId().equals(prog.getId()))
                        .findFirst().orElse(null);

                if (existing == null) {
                    // Not enrolled at all — available
                    return true;
                }

                // Enrolled — only available if all sessions are completed
                int totalSessions = prog.getTotalSessions() != null ? prog.getTotalSessions() : 0;
                if (totalSessions > 0) {
                    long completed = sessionService.countCompletedByPatientAndProgram(patientId, prog.getId());
                    return completed >= totalSessions;
                }
                return false;
            }).collect(Collectors.toList());

            cmbNewProgram.setItems(FXCollections.observableArrayList(available));
            cmbNewProgram.setValue(null);
            cmbNewProgramSessions.setItems(FXCollections.observableArrayList());
            lblNewProgramCost.setText("0 LKR");
        } catch (Exception e) {
            System.err.println("Error loading available programs: " + e.getMessage());
        }
    }

    // ===================== FORM POPULATION =====================

    private void populateForm(PatientTM p) {
        txtEditName.setText(p.getName());
        txtEditEmail.setText(p.getEmail());
        txtEditPhone.setText(p.getPhone());
        txtEditAddress.setText(p.getAddress());
        // Interview note would need to be fetched from full DTO if needed
        // For now we clear it since TM doesn't carry it
        txtEditInterviewNote.clear();
    }

    private void updateNewProgramCost() {
        TherapyProgramDTO prog = cmbNewProgram.getValue();
        Integer sessions = cmbNewProgramSessions.getValue();
        if (prog == null || sessions == null || sessions == 0) {
            lblNewProgramCost.setText("0 LKR");
            return;
        }
        BigDecimal cost;
        if (prog.getSessionFee() != null) {
            cost = prog.getSessionFee().multiply(new BigDecimal(sessions));
        } else if (prog.getFee() != null) {
            int total = prog.getTotalSessions() != null ? prog.getTotalSessions() : 1;
            BigDecimal perSession = prog.getFee().divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
            cost = perSession.multiply(new BigDecimal(sessions));
        } else {
            cost = BigDecimal.ZERO;
        }
        lblNewProgramCost.setText(cost.toPlainString() + " LKR");
    }

    // ===================== EVENT HANDLERS =====================

    @FXML void handleUpdatePatient(ActionEvent event) {
        if (selectedPatient == null) { AlertUtil.showWarning("Warning", "Select a patient first."); return; }
        try {
            long patientId = parsePatientId(selectedPatient.getId());
            PatientDTO dto = new PatientDTO();
            dto.setId(patientId);
            dto.setName(txtEditName.getText());
            dto.setEmail(txtEditEmail.getText());
            dto.setPhone(txtEditPhone.getText());
            dto.setAddress(txtEditAddress.getText());
            dto.setInterviewNote(txtEditInterviewNote.getText());
            patientService.updatePatient(dto);
            AlertUtil.showInfo("Success", "Patient updated.");
            handleClearEditForm(event);
            loadData();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void handleDeletePatient(ActionEvent event) {
        PatientTM p = tblPatients.getSelectionModel().getSelectedItem();
        if (p == null) { AlertUtil.showWarning("Warning", "Select a patient to delete."); return; }
        if (AlertUtil.showConfirmation("Confirm", "Delete patient \"" + p.getName() + "\"?")) {
            try {
                long patientId = parsePatientId(p.getId());
                patientService.deletePatient(patientId);
                handleClearEditForm(event);
                loadData();
            } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }

    @FXML void handleEnrollNewProgram(ActionEvent event) {
        if (selectedPatient == null) {
            AlertUtil.showWarning("Warning", "Select a patient first.");
            return;
        }

        TherapyProgramDTO program = cmbNewProgram.getValue();
        if (program == null) {
            AlertUtil.showWarning("Warning", "Please select a program to enroll in.");
            return;
        }

        Integer sessionsToPay = cmbNewProgramSessions.getValue();
        if (sessionsToPay == null) sessionsToPay = 0;

        // Calculate cost
        BigDecimal cost = BigDecimal.ZERO;
        if (sessionsToPay > 0) {
            if (program.getSessionFee() != null) {
                cost = program.getSessionFee().multiply(new BigDecimal(sessionsToPay));
            } else if (program.getFee() != null) {
                int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
                BigDecimal perSession = program.getFee().divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
                cost = perSession.multiply(new BigDecimal(sessionsToPay));
            }
        }

        // Require payment method if cost > 0
        PaymentMethod method = cmbNewProgramPayMethod.getValue();
        if (cost.compareTo(BigDecimal.ZERO) > 0 && method == null) {
            AlertUtil.showWarning("Warning", "Please select a payment method.");
            return;
        }

        try {
            long patientId = parsePatientId(selectedPatient.getId());

            // 1. Enroll via BO
            patientService.enrollPatientInProgram(patientId, program.getId(), sessionsToPay);

            // 2. If cost > 0, record the upfront payment via PaymentBO
            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setPatientId(patientId);
                paymentDTO.setAmount(cost);
                paymentDTO.setMethod(method);
                paymentDTO.setDescription("Upfront payment for " + sessionsToPay + " sessions of " + program.getName());
                paymentService.saveRegistrationPayment(paymentDTO);
            }

            AlertUtil.showInfo("Success", "Patient enrolled in " + program.getName()
                    + (sessionsToPay > 0 ? " with " + sessionsToPay + " sessions paid upfront." : "."));

            // Refresh
            loadEnrolledPrograms(selectedPatient);
            loadAvailablePrograms(selectedPatient);
            cmbNewProgramPayMethod.setValue(null);

        } catch (Exception e) {
            AlertUtil.showError("Error", "Enrollment failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML void handleClearEditForm(ActionEvent event) {
        txtEditName.clear(); txtEditEmail.clear();
        txtEditPhone.clear(); txtEditAddress.clear();
        txtEditInterviewNote.clear();
        selectedPatient = null;
        tblPatients.getSelectionModel().clearSelection();
        vboxEnrolledPrograms.setVisible(false);
        vboxEnrolledPrograms.setManaged(false);
    }

    // ===================== HELPERS =====================

    private <T> void setComboCellFactory(ComboBox<T> combo, Function<T, String> nameFunc) {
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

    /**
     * Parse patient ID from formatted string (e.g., "P001" -> 1)
     */
    private long parsePatientId(String formattedId) {
        if (formattedId != null && formattedId.startsWith("P")) {
            return Long.parseLong(formattedId.substring(1));
        }
        return 0;
    }

    /**
     * Simple row model for the enrolled programs table.
     */
    public static class EnrolledProgramRow {
        public final String programName;
        public final int totalSessions;
        public final int upfrontPaid;
        public final int sessionsUsed;
        public final int creditRemaining;
        public final int completedSessions;
        public final String status;

        public EnrolledProgramRow(String programName, int totalSessions, int upfrontPaid,
                                  int sessionsUsed, int creditRemaining, int completedSessions, String status) {
            this.programName = programName;
            this.totalSessions = totalSessions;
            this.upfrontPaid = upfrontPaid;
            this.sessionsUsed = sessionsUsed;
            this.creditRemaining = creditRemaining;
            this.completedSessions = completedSessions;
            this.status = status;
        }
    }
}
