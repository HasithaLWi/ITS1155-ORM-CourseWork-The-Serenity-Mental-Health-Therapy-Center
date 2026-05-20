package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.*;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PaymentTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ComboBoxAutoCompleteUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;

public class PaymentManagementController implements Initializable {

    // ─── Make Payment: Type selector ───
    @FXML private ComboBox<String> cmbPaymentType;

    // ─── Session Payment pane ───
    @FXML private VBox paneSessionPayment;
    @FXML private ComboBox<TherapySessionDTO> cmbSessionId;
    @FXML private TextField txtSessionCost;
    @FXML private ComboBox<PatientDTO> cmbSessionPatient;
    @FXML private ComboBox<PatientTherapyProgramDTO> cmbSessionProgram;
    @FXML private ComboBox<Integer> cmbSessionCount;
    @FXML private TextField txtBulkCost;
    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;

    // ─── Expense pane ───
    @FXML private VBox paneExpense;
    @FXML private ComboBox<String> cmbExpenseType;
    @FXML private ComboBox<PatientDTO> cmbExpensePatient;
    @FXML private TextField txtExpenseAmount;
    @FXML private ComboBox<PaymentMethod> cmbExpenseMethod;
    @FXML private TextField txtExpenseDescription;

    // ─── View Payments: Filters ───
    @FXML private ComboBox<PatientDTO> cmbFilterPatient;
    @FXML private DatePicker dpFilterFrom;
    @FXML private DatePicker dpFilterTo;
    @FXML private ComboBox<String> cmbFilterType;

    // ─── Table ───
    @FXML private TableView<PaymentTM> tblPayments;
    @FXML private TableColumn<PaymentTM, String> colPaymentId;
    @FXML private TableColumn<PaymentTM, String> colPaymentPatient;
    @FXML private TableColumn<PaymentTM, BigDecimal> colPaymentAmount;
    @FXML private TableColumn<PaymentTM, String> colPaymentMethod;
    @FXML private TableColumn<PaymentTM, String> colPaymentType;
    @FXML private TableColumn<PaymentTM, String> colPaymentDate;
    @FXML private TableColumn<PaymentTM, String> colPaymentStatus;
    @FXML private TableColumn<PaymentTM, String> colPaymentDesc;
    @FXML private Label lblTotalRevenue;

    private final PaymentBOImpl paymentService = new PaymentBOImpl();
    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();
    private final PatientBOImpl patientService = new PatientBOImpl();

    private List<PatientDTO> allPatientsList;
    private List<TherapySessionDTO> unpaidSessionsList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadBaseData();
        setupPaymentTypeSelector();
        setupSimpleCombos();
        setupSessionIdCombo();
        setupPatientProgramChain();
        setupTable();
        loadData();
    }

    // ═══════════════════ DATA LOADING ═══════════════════

    private void loadBaseData() {
        try {
            allPatientsList = patientService.getAllPatients();
        } catch (Exception e) {
            allPatientsList = new ArrayList<>();
            System.err.println("Error loading patients: " + e.getMessage());
        }

        try {
            unpaidSessionsList = sessionService.getAllSessionDTOs().stream()
                .filter(s -> s.getPaymentStatus() == SessionPaymentStatus.PENDING)
                .toList();
        } catch (Exception e) {
            unpaidSessionsList = new ArrayList<>();
            System.err.println("Error loading sessions: " + e.getMessage());
        }
    }

    // ═══════════════════ SETUP ═══════════════════

    private void setupPaymentTypeSelector() {
        cmbPaymentType.setItems(FXCollections.observableArrayList("Session Payment", "Expense"));
        cmbPaymentType.setValue("Session Payment");

        cmbPaymentType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSession = "Session Payment".equals(newVal);
            paneSessionPayment.setVisible(isSession);
            paneSessionPayment.setManaged(isSession);
            paneExpense.setVisible(!isSession);
            paneExpense.setManaged(!isSession);
        });
    }

    private void setupSimpleCombos() {
        // Payment methods
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        cmbExpenseMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));

        // Expense types
        cmbExpenseType.setItems(FXCollections.observableArrayList("Refund", "Administrative", "Other"));

        // Filter type
        cmbFilterType.setItems(FXCollections.observableArrayList("ALL", "SINGLE", "UPFRONT", "EXPENSE"));

        // Program combo cell factory (not editable, no autocomplete needed)
        cmbSessionProgram.setButtonCell(createProgramListCell());
        cmbSessionProgram.setCellFactory(lv -> createProgramListCell());

        // Session count combo (not editable)
        // No special setup needed

        // ── Patient autocomplete combos (using setupAutocomplete) ──
        Function<PatientDTO, String> patientDisplay = p -> "#" + p.getId() + " - " + p.getName();
        Function<PatientDTO, String> patientSearch = p -> "#" + p.getId() + " " + p.getName();

        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionPatient,
            new ArrayList<>(allPatientsList), patientDisplay, patientSearch);

        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbExpensePatient,
            new ArrayList<>(allPatientsList), patientDisplay, patientSearch);

        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbFilterPatient,
            new ArrayList<>(allPatientsList), patientDisplay, patientSearch);
    }

    private void setupSessionIdCombo() {
        // Session ID combo with autocomplete
        Function<TherapySessionDTO, String> sessionDisplay = s ->
            "#" + s.getId() + " - " + (s.getPatientName() != null ? s.getPatientName() : "N/A")
                + " | " + (s.getProgramName() != null ? s.getProgramName() : "");
        Function<TherapySessionDTO, String> sessionSearch = s ->
            "#" + s.getId() + " " + (s.getPatientName() != null ? s.getPatientName() : "")
                + " " + (s.getProgramName() != null ? s.getProgramName() : "");

        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbSessionId,
            new ArrayList<>(unpaidSessionsList), sessionDisplay, sessionSearch);

        // Listen for selection changes to auto-fill cost
        cmbSessionId.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getProgramId() != null) {
                fillSessionCost(newVal);
            } else {
                txtSessionCost.clear();
            }
        });
    }

    private void fillSessionCost(TherapySessionDTO session) {
        try {
            if (session.getPatientId() != null && session.getProgramId() != null) {
                List<PatientTherapyProgramDTO> ptps = patientService.getPatientPrograms(session.getPatientId());
                for (PatientTherapyProgramDTO ptp : ptps) {
                    if (ptp.getProgramId() != null && ptp.getProgramId().equals(session.getProgramId())) {
                        BigDecimal fee = calculatePerSessionFee(ptp);
                        txtSessionCost.setText(fee.toPlainString());
                        return;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading session cost: " + ex.getMessage());
        }
        txtSessionCost.setText("0.00");
    }

    private BigDecimal calculatePerSessionFee(PatientTherapyProgramDTO ptp) {
        TherapyProgramDTO program = ptp.getProgram();
        if (program == null) return BigDecimal.ZERO;
        if (program.getSessionFee() != null) return program.getSessionFee();
        if (program.getFee() != null && program.getTotalSessions() != null && program.getTotalSessions() > 0) {
            return program.getFee().divide(new BigDecimal(program.getTotalSessions()), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    // ─── Patient → Program → Session count chain ───
    private void setupPatientProgramChain() {
        // When patient changes in session payment form
        cmbSessionPatient.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbSessionProgram.getItems().clear();
            cmbSessionCount.getItems().clear();
            txtBulkCost.clear();

            if (newVal != null) {
                try {
                    List<PatientTherapyProgramDTO> programs = patientService.getPatientPrograms(newVal.getId());
                    cmbSessionProgram.setItems(FXCollections.observableArrayList(programs));
                } catch (Exception ex) {
                    System.err.println("Error loading programs: " + ex.getMessage());
                }
            }
        });

        // When program changes
        cmbSessionProgram.valueProperty().addListener((obs, oldVal, newVal) -> {
            cmbSessionCount.getItems().clear();
            txtBulkCost.clear();

            if (newVal != null) {
                PatientDTO patient = cmbSessionPatient.getValue();
                if (patient != null) {
                    long unpaidCount = unpaidSessionsList.stream()
                        .filter(s -> s.getPatientId() != null && s.getPatientId().equals(patient.getId()))
                        .filter(s -> s.getProgramId() != null && s.getProgramId().equals(newVal.getProgramId()))
                        .count();

                    ObservableList<Integer> counts = FXCollections.observableArrayList();
                    for (int i = 1; i <= unpaidCount; i++) counts.add(i);
                    if (counts.isEmpty()) counts.add(0);
                    cmbSessionCount.setItems(counts);
                }
            }
        });

        // When session count changes
        cmbSessionCount.valueProperty().addListener((obs, oldVal, newVal) -> {
            PatientTherapyProgramDTO ptp = cmbSessionProgram.getValue();
            if (newVal != null && newVal > 0 && ptp != null) {
                BigDecimal cost = calculateBulkCost(ptp, newVal);
                txtBulkCost.setText(cost.toPlainString());
            } else {
                txtBulkCost.clear();
            }
        });
    }

    private BigDecimal calculateBulkCost(PatientTherapyProgramDTO ptp, int sessions) {
        if (sessions == 0) return BigDecimal.ZERO;
        BigDecimal perSession = calculatePerSessionFee(ptp);
        return perSession.multiply(new BigDecimal(sessions));
    }

    // ═══════════════════ TABLE ═══════════════════

    private void setupTable() {
        colPaymentId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colPaymentPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));
        colPaymentAmount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getAmount()));
        colPaymentMethod.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMethod()));
        colPaymentType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentType()));
        colPaymentDate.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPaymentDate() != null
                ? d.getValue().getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));
        colPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colPaymentDesc.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getDescription() != null ? d.getValue().getDescription() : ""));
    }

    private void loadData() {
        try {
            List<PaymentDTO> dtos = paymentService.getAllPayments();
            tblPayments.setItems(FXCollections.observableArrayList(toTMList(dtos)));

            BigDecimal total = paymentService.getTotalRevenue();
            lblTotalRevenue.setText("Total Revenue: LKR " + (total != null ? total.toPlainString() : "0"));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load payments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<PaymentTM> toTMList(List<PaymentDTO> dtos) {
        return dtos.stream().map(dto -> new PaymentTM(
            String.format("PAY%03d", dto.getId()),
            dto.getAmount(),
            dto.getPaymentDate(),
            dto.getMethod() != null ? dto.getMethod().name() : "",
            dto.getStatus() != null ? dto.getStatus().name() : "",
            dto.getPaymentType() != null ? dto.getPaymentType().name() : "",
            dto.getPatientName() != null ? dto.getPatientName() : "N/A",
            dto.getSessionId() != null ? String.format("S%03d", dto.getSessionId()) : "",
            dto.getDescription(),
            dto.getPatientId()
        )).toList();
    }

    // ═══════════════════ HANDLERS: SESSION PAYMENT ═══════════════════

    @FXML
    void handleProcessPayment(ActionEvent event) {
        try {
            TherapySessionDTO selectedSession = cmbSessionId.getValue();

            if (selectedSession != null) {
                // ─── Path A: Pay by session ID ───
                processSessionIdPayment(selectedSession);
            } else if (cmbSessionPatient.getValue() != null && cmbSessionProgram.getValue() != null
                       && cmbSessionCount.getValue() != null && cmbSessionCount.getValue() > 0) {
                // ─── Path B: Pay by patient/program/count ───
                processPatientProgramPayment();
            } else {
                AlertUtil.showWarning("Warning", "Select a session OR choose patient \u2192 program \u2192 session count.");
                return;
            }

            AlertUtil.showInfo("Success", "Payment processed successfully.");
            handleClearPayment(event);
            refreshAll();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void processSessionIdPayment(TherapySessionDTO selectedSession) {
        PaymentMethod method = cmbPaymentMethod.getValue();
        if (method == null) {
            throw new RuntimeException("Please select a payment method.");
        }

        String costText = txtSessionCost.getText();
        BigDecimal amount = (costText != null && !costText.trim().isEmpty())
            ? new BigDecimal(costText.trim()) : BigDecimal.ZERO;

        if (amount.signum() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero.");
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setSessionId(selectedSession.getId());
        dto.setAmount(amount);
        dto.setMethod(method);
        paymentService.processPayment(dto);
    }

    private void processPatientProgramPayment() {
        PatientDTO patient = cmbSessionPatient.getValue();
        PatientTherapyProgramDTO ptp = cmbSessionProgram.getValue();
        int count = cmbSessionCount.getValue();
        PaymentMethod method = cmbPaymentMethod.getValue();

        if (method == null) {
            throw new RuntimeException("Please select a payment method.");
        }

        String costText = txtBulkCost.getText();
        BigDecimal totalCost = (costText != null && !costText.trim().isEmpty())
            ? new BigDecimal(costText.trim()) : BigDecimal.ZERO;

        if (totalCost.signum() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero.");
        }

        // Find unpaid sessions for this patient+program
        List<Long> sessionIds = unpaidSessionsList.stream()
            .filter(s -> s.getPatientId() != null && s.getPatientId().equals(patient.getId()))
            .filter(s -> s.getProgramId() != null && s.getProgramId().equals(ptp.getProgramId()))
            .limit(count)
            .map(TherapySessionDTO::getId)
            .toList();

        if (sessionIds.size() < count) {
            throw new RuntimeException("Not enough unpaid sessions available. Found " + sessionIds.size() + " but need " + count + ".");
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setPatientId(patient.getId());
        dto.setAmount(totalCost);
        dto.setMethod(method);
        dto.setDescription("Session payment for " + count + " sessions (" + ptp.getProgramName() + ")");
        paymentService.processUpfrontPayment(dto, sessionIds);
    }

    @FXML
    void handleClearPayment(ActionEvent event) {
        cmbSessionId.setValue(null);
        if (cmbSessionId.getEditor() != null) cmbSessionId.getEditor().clear();
        txtSessionCost.clear();
        cmbSessionPatient.setValue(null);
        if (cmbSessionPatient.getEditor() != null) cmbSessionPatient.getEditor().clear();
        cmbSessionProgram.getItems().clear();
        cmbSessionCount.getItems().clear();
        txtBulkCost.clear();
        cmbPaymentMethod.setValue(null);
    }

    // ═══════════════════ HANDLERS: EXPENSE ═══════════════════

    @FXML
    void handleProcessExpense(ActionEvent event) {
        try {
            String expenseType = cmbExpenseType.getValue();
            PatientDTO patient = cmbExpensePatient.getValue();
            PaymentMethod method = cmbExpenseMethod.getValue();
            String amountText = txtExpenseAmount.getText();
            String description = txtExpenseDescription.getText();

            if (expenseType == null) {
                AlertUtil.showWarning("Warning", "Please select an expense type.");
                return;
            }
            if (patient == null) {
                AlertUtil.showWarning("Warning", "Please select a patient.");
                return;
            }
            if (method == null) {
                AlertUtil.showWarning("Warning", "Please select a payment method.");
                return;
            }
            if (amountText == null || amountText.trim().isEmpty()) {
                AlertUtil.showWarning("Warning", "Please enter an amount.");
                return;
            }

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountText.trim());
            } catch (NumberFormatException ex) {
                AlertUtil.showWarning("Warning", "Invalid amount format.");
                return;
            }

            if (amount.signum() <= 0) {
                AlertUtil.showWarning("Warning", "Amount must be greater than zero.");
                return;
            }

            PaymentDTO dto = new PaymentDTO();
            dto.setPatientId(patient.getId());
            dto.setAmount(amount);
            dto.setMethod(method);
            dto.setDescription(expenseType + (description != null && !description.trim().isEmpty() ? ": " + description.trim() : ""));

            paymentService.processExpense(dto);

            AlertUtil.showInfo("Success", "Expense recorded successfully.");
            handleClearExpense(event);
            refreshAll();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearExpense(ActionEvent event) {
        cmbExpenseType.setValue(null);
        cmbExpensePatient.setValue(null);
        if (cmbExpensePatient.getEditor() != null) cmbExpensePatient.getEditor().clear();
        txtExpenseAmount.clear();
        cmbExpenseMethod.setValue(null);
        txtExpenseDescription.clear();
    }

    // ═══════════════════ HANDLERS: FILTERS ═══════════════════

    @FXML
    void handleFilterPayments(ActionEvent event) {
        try {
            PatientDTO patient = cmbFilterPatient.getValue();
            Long patientId = patient != null ? patient.getId() : null;

            LocalDate fromDate = dpFilterFrom.getValue();
            LocalDate toDate = dpFilterTo.getValue();
            LocalDateTime start = fromDate != null ? fromDate.atStartOfDay() : null;
            LocalDateTime end = toDate != null ? toDate.atTime(23, 59, 59) : null;

            String type = cmbFilterType.getValue();

            List<PaymentDTO> filtered = paymentService.getFilteredPayments(patientId, start, end, type);
            tblPayments.setItems(FXCollections.observableArrayList(toTMList(filtered)));
        } catch (Exception e) {
            AlertUtil.showError("Error", "Filter failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearFilters(ActionEvent event) {
        cmbFilterPatient.setValue(null);
        if (cmbFilterPatient.getEditor() != null) cmbFilterPatient.getEditor().clear();
        dpFilterFrom.setValue(null);
        dpFilterTo.setValue(null);
        cmbFilterType.setValue(null);
        loadData();
    }

    // ═══════════════════ HELPERS ═══════════════════

    private void refreshAll() {
        loadData();
        try {
            unpaidSessionsList = sessionService.getAllSessionDTOs().stream()
                .filter(s -> s.getPaymentStatus() == SessionPaymentStatus.PENDING)
                .toList();
            // Re-setup session combo with fresh data
            setupSessionIdCombo();
        } catch (Exception e) {
            System.err.println("Error refreshing sessions: " + e.getMessage());
        }
    }

    private ListCell<PatientTherapyProgramDTO> createProgramListCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(PatientTherapyProgramDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("");
                else setText(item.getProgramName() != null ? item.getProgramName() : "Program #" + item.getProgramId());
            }
        };
    }
}
