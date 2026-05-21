package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramPaymentTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UpfrontPaymentDialogController implements Initializable {

    @FXML private TableView<ProgramPaymentTM> tblPaymentPrograms;
    @FXML private TableColumn<ProgramPaymentTM, String> colProgramName;
    @FXML private TableColumn<ProgramPaymentTM, Integer> colTotalSessions;
    @FXML private TableColumn<ProgramPaymentTM, Integer> colSessionsToPay;
    @FXML private TableColumn<ProgramPaymentTM, BigDecimal> colSubtotal;

    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
    @FXML private TextField txtDiscount;

    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalDue;

    private Long currentPatientId;
    private List<TherapyProgramDTO> enrolledPrograms;
    private Runnable onSuccessCallback;

    private final TherapySessionBO sessionService = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);
    private final PaymentBO paymentService = (PaymentBO) BOFactory.getInstance().getBO(BOFactory.BOType.PAYMENT);

    private final ObservableList<ProgramPaymentTM> paymentModels = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        
        setupTable();
        
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotals());
    }

    private void setupTable() {
        colProgramName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgramName()));
        colTotalSessions.setCellValueFactory(d -> new SimpleObjectProperty<>(
                d.getValue().getProgram().getTotalSessions() != null ? d.getValue().getProgram().getTotalSessions() : 1));
        
        // Custom cell factory for the sessions-to-pay ComboBox
        colSessionsToPay.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSessionsToPay()));
        colSessionsToPay.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<Integer> comboBox = new ComboBox<>();

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ProgramPaymentTM model = getTableView().getItems().get(getIndex());
                    int total = model.getProgram().getTotalSessions() != null ? model.getProgram().getTotalSessions() : 1;
                    ObservableList<Integer> options = FXCollections.observableArrayList();
                    for (int i = 0; i <= total; i++) options.add(i);
                    comboBox.setItems(options);
                    comboBox.setOnAction(null);
                    comboBox.setValue(model.getSessionsToPay());
                    comboBox.setOnAction(e -> {
                        if (comboBox.getValue() != null) {
                            model.setSessionsToPay(comboBox.getValue());
                            model.setSubtotal(calculateLineTotal(model.getProgram(), comboBox.getValue()));
                            calculateTotals();
                            getTableView().refresh();
                        }
                    });
                    setGraphic(comboBox);
                }
            }
        });
        
        colSubtotal.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSubtotal()));
    }

    public void initData(Long patientId, List<TherapyProgramDTO> programs, Runnable onSuccess) {
        this.currentPatientId = patientId;
        this.enrolledPrograms = programs;
        this.onSuccessCallback = onSuccess;
        
        loadPrograms();
    }

    private void loadPrograms() {
        paymentModels.clear();
        for (TherapyProgramDTO program : enrolledPrograms) {
            int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
            BigDecimal subtotal = calculateLineTotal(program, total);
            ProgramPaymentTM model = new ProgramPaymentTM(program, program.getName(), total, total, subtotal);
            paymentModels.add(model);
        }
        tblPaymentPrograms.setItems(paymentModels);
        calculateTotals();
    }

    private void calculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ProgramPaymentTM model : paymentModels) {
            subtotal = subtotal.add(model.getSubtotal());
        }
        
        BigDecimal discount = parseDiscount();
        BigDecimal totalDue = subtotal.subtract(discount);
        
        if (totalDue.signum() < 0) {
            totalDue = BigDecimal.ZERO;
        }

        lblSubtotal.setText(subtotal.toPlainString() + " LKR");
        lblDiscount.setText("- " + discount.toPlainString() + " LKR");
        lblTotalDue.setText(totalDue.toPlainString() + " LKR");
    }

    private BigDecimal parseDiscount() {
        String text = txtDiscount.getText();
        if (text == null || text.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    @FXML
    void handleProcessPayment(ActionEvent event) {
        try {
            if (currentPatientId == null) {
                AlertUtil.showWarning("Warning", "Patient must be registered first before processing upfront payment.");
                return;
            }

            PaymentMethod method = cmbPaymentMethod.getValue();
            if (method == null) {
                AlertUtil.showWarning("Warning", "Please select a payment method.");
                return;
            }

            BigDecimal discount = parseDiscount();
            BigDecimal subtotal = BigDecimal.ZERO;
            List<Long> sessionIdsToPayFor = new ArrayList<>();

            // Find unscheduled sessions for the patient (via DTO)
            List<TherapySessionDTO> allUnscheduled = sessionService.getAllSessionDTOs().stream()
                    .filter(s -> s.getPatientId() != null && s.getPatientId().equals(currentPatientId))
                    .filter(s -> s.getPaymentStatus() == SessionPaymentStatus.PENDING)
                    .toList();

            for (ProgramPaymentTM model : paymentModels) {
                int selectedSessions = model.getSessionsToPay();
                if (selectedSessions > 0) {
                    subtotal = subtotal.add(model.getSubtotal());
                    
                    long programId = model.getProgram().getId();
                    List<TherapySessionDTO> programSessions = allUnscheduled.stream()
                            .filter(s -> s.getProgramId() != null && s.getProgramId() == programId)
                            .limit(selectedSessions)
                            .toList();
                            
                    if (programSessions.size() < selectedSessions) {
                        AlertUtil.showWarning("Warning", "Not enough pending sessions available for " + model.getProgramName() + ".");
                        return;
                    }
                    for (TherapySessionDTO s : programSessions) {
                        sessionIdsToPayFor.add(s.getId());
                    }
                }
            }

            if (sessionIdsToPayFor.isEmpty()) {
                AlertUtil.showWarning("Warning", "Please select at least one session to pay for.");
                return;
            }

            BigDecimal totalDue = subtotal.subtract(discount);
            if (totalDue.signum() < 0) totalDue = BigDecimal.ZERO;

            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setPatientId(currentPatientId);
            paymentDTO.setAmount(totalDue);
            paymentDTO.setMethod(method);
            paymentDTO.setDiscount(discount);
            paymentDTO.setDescription("Upfront package payment for " + sessionIdsToPayFor.size() + " sessions.");

            paymentService.processUpfrontPayment(paymentDTO, sessionIdsToPayFor);
            
            AlertUtil.showInfo("Success", "Upfront payment processed successfully.");
            
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            handleClose(event);
            
        } catch (Exception e) {
            AlertUtil.showError("Error", "Payment failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClose(ActionEvent event) {
        Stage stage = (Stage) tblPaymentPrograms.getScene().getWindow();
        stage.close();
    }

    private BigDecimal calculateLineTotal(TherapyProgramDTO program, int sessions) {
        if (sessions == 0) return BigDecimal.ZERO;

        if (program.getSessionFee() != null) {
            return program.getSessionFee().multiply(new BigDecimal(sessions));
        } else if (program.getFee() != null) {
            int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
            BigDecimal perSession = program.getFee().divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
            return perSession.multiply(new BigDecimal(sessions));
        }
        return BigDecimal.ZERO;
    }
}
