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
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
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


    @FXML private ComboBox<String> cmbPaymentType;


    @FXML private VBox paneSingleSessionPayment;
    @FXML private ComboBox<TherapySessionDTO> cmbSessionId;
    @FXML private TextField txtSessionCost;
    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;


    @FXML private VBox paneMultipleSessionPayment;
    @FXML private ComboBox<PatientDTO> cmbBulkPatient;
    @FXML private ComboBox<PatientTherapyProgramDTO> cmbBulkProgram;
    @FXML private TextField txtBulkAlreadyPaid;
    @FXML private ComboBox<Integer> cmbBulkSessionCount;
    @FXML private TextField txtBulkCost;
    @FXML private ComboBox<PaymentMethod> cmbBulkPaymentMethod;


    @FXML private VBox paneExpense;
    @FXML private ComboBox<String> cmbExpenseType;
    @FXML private ComboBox<PatientDTO> cmbExpensePatient;
    @FXML private TextField txtExpenseAmount;
    @FXML private ComboBox<PaymentMethod> cmbExpenseMethod;
    @FXML private TextField txtExpenseDescription;


    @FXML private ComboBox<PatientDTO> cmbFilterPatient;
    @FXML private DatePicker dpFilterFrom;
    @FXML private DatePicker dpFilterTo;
    @FXML private ComboBox<String> cmbFilterType;


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

    private final PaymentBO paymentService = (PaymentBO) BOFactory.getInstance().getBO(BOFactory.BOType.PAYMENT);
    private final TherapySessionBO sessionService = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);
    private final PatientBO patientService = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BOType.PATIENT);

    private List<PatientDTO> allPatientsList;
    private List<TherapySessionDTO> unpaidSessionsList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadBaseData();
        setupPaymentTypeSelector();
        setupSimpleCombos();
        setupSessionIdCombo();
        setupTable();
        loadData();
    }



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
            System.err.println("Error loading unpaid sessions: " + e.getMessage());
        }
    }



    private void setupPaymentTypeSelector() {
        cmbPaymentType.setItems(FXCollections.observableArrayList("Single Session Payment", "Multiple Session Payment", "Expense"));
        cmbPaymentType.setValue("Single Session Payment");

        cmbPaymentType.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSingle = "Single Session Payment".equals(newVal);
            boolean isMultiple = "Multiple Session Payment".equals(newVal);
            boolean isExpense = "Expense".equals(newVal);

            paneSingleSessionPayment.setVisible(isSingle);
            paneSingleSessionPayment.setManaged(isSingle);

            paneMultipleSessionPayment.setVisible(isMultiple);
            paneMultipleSessionPayment.setManaged(isMultiple);

            paneExpense.setVisible(isExpense);
            paneExpense.setManaged(isExpense);
        });
    }

    private void setupSimpleCombos() {

        cmbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        cmbBulkPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        cmbExpenseMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));


        cmbExpenseType.setItems(FXCollections.observableArrayList("Refund", "Administrative", "Other"));


        cmbFilterType.setItems(FXCollections.observableArrayList("ALL", "SINGLE", "UPFRONT", "EXPENSE"));

        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbExpensePatient,
            new ArrayList<>(allPatientsList), PatientDTO::getStringId, PatientDTO::getStringId);

        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbFilterPatient,
            new ArrayList<>(allPatientsList), PatientDTO::getStringId, PatientDTO::getStringId);


        ComboBoxAutoCompleteUtil.setupAutocomplete(cmbBulkPatient,
            new ArrayList<>(allPatientsList), PatientDTO::getStringId, PatientDTO::getStringId);

        cmbBulkPatient.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadBulkPrograms(newVal.getId());
            } else {
                cmbBulkProgram.setItems(FXCollections.emptyObservableList());
                clearBulkProgramDependentFields();
            }
        });

        cmbBulkProgram.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtBulkAlreadyPaid.setText(String.valueOf(newVal.getSessionsPaid()));
                int total = newVal.getTotalSessions();
                int paid = newVal.getSessionsPaid();
                int maxToPay = (total > 0) ? (total - paid) : 20;
                List<Integer> counts = new ArrayList<>();
                for (int i = 1; i <= maxToPay; i++) {
                    counts.add(i);
                }
                cmbBulkSessionCount.setItems(FXCollections.observableArrayList(counts));
                if (counts.isEmpty()) {
                    cmbBulkSessionCount.setPromptText("Fully Paid");
                } else {
                    cmbBulkSessionCount.setPromptText("Select count");
                }
                updateBulkCost();
            } else {
                clearBulkProgramDependentFields();
            }
        });

        cmbBulkSessionCount.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateBulkCost();
        });
    }

    private void loadBulkPrograms(Long patientId) {
        try {
            List<PatientTherapyProgramDTO> programs = patientService.getPatientPrograms(patientId);
            cmbBulkProgram.setItems(FXCollections.observableArrayList(programs));
            cmbBulkProgram.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(PatientTherapyProgramDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getProgramName());
                }
            });
            cmbBulkProgram.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(PatientTherapyProgramDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getProgramName());
                }
            });
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load patient programs: " + e.getMessage());
        }
    }

    private void updateBulkCost() {
        PatientTherapyProgramDTO ptp = cmbBulkProgram.getValue();
        Integer count = cmbBulkSessionCount.getValue();
        if (ptp != null && count != null) {
            BigDecimal fee = calculatePerSessionFee(ptp);
            BigDecimal totalCost = fee.multiply(new BigDecimal(count));
            txtBulkCost.setText(totalCost.toPlainString());
        } else {
            txtBulkCost.clear();
        }
    }

    private void clearBulkProgramDependentFields() {
        txtBulkAlreadyPaid.clear();
        cmbBulkSessionCount.setValue(null);
        cmbBulkSessionCount.setItems(FXCollections.emptyObservableList());
        txtBulkCost.clear();
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


    @FXML
    void handleProcessPayment(ActionEvent event) {
        try {
            TherapySessionDTO selectedSession = cmbSessionId.getValue();

            if (selectedSession != null) {

                processSessionIdPayment(selectedSession);

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



    @FXML
    void handleClearPayment(ActionEvent event) {
        cmbSessionId.setValue(null);
        if (cmbSessionId.getEditor() != null) cmbSessionId.getEditor().clear();
        txtSessionCost.clear();
        cmbPaymentMethod.setValue(null);
    }

    @FXML
    void handleProcessBulkPayment(ActionEvent event) {
        try {
            PatientDTO patient = cmbBulkPatient.getValue();
            PatientTherapyProgramDTO ptp = cmbBulkProgram.getValue();
            Integer count = cmbBulkSessionCount.getValue();
            PaymentMethod method = cmbBulkPaymentMethod.getValue();
            String costText = txtBulkCost.getText();

            if (patient == null) {
                AlertUtil.showWarning("Warning", "Please select a patient.");
                return;
            }
            if (ptp == null) {
                AlertUtil.showWarning("Warning", "Please select a program.");
                return;
            }
            if (count == null) {
                AlertUtil.showWarning("Warning", "Please select sessions to pay.");
                return;
            }
            if (method == null) {
                AlertUtil.showWarning("Warning", "Please select a payment method.");
                return;
            }

            BigDecimal amount = (costText != null && !costText.trim().isEmpty())
                ? new BigDecimal(costText.trim()) : BigDecimal.ZERO;
            if (amount.signum() <= 0) {
                AlertUtil.showWarning("Warning", "Amount must be greater than zero.");
                return;
            }

            paymentService.processMultipleSessionPayment(patient.getId(), ptp.getProgramId(), count, amount, method);

            AlertUtil.showInfo("Success", "Bulk payment processed successfully.");
            handleClearBulkPayment(event);
            refreshAll();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearBulkPayment(ActionEvent event) {
        cmbBulkPatient.setValue(null);
        if (cmbBulkPatient.getEditor() != null) cmbBulkPatient.getEditor().clear();
        cmbBulkProgram.setValue(null);
        txtBulkAlreadyPaid.clear();
        cmbBulkSessionCount.setValue(null);
        txtBulkCost.clear();
        cmbBulkPaymentMethod.setValue(null);
    }



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


}
