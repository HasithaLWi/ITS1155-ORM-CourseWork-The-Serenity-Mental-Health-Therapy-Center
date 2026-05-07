package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class UpfrontPaymentDialogController implements Initializable {

    @FXML private TableView<ProgramPaymentModel> tblPaymentPrograms;
    @FXML private TableColumn<ProgramPaymentModel, String> colProgramName;
    @FXML private TableColumn<ProgramPaymentModel, Integer> colTotalSessions;
    @FXML private TableColumn<ProgramPaymentModel, ComboBox<Integer>> colSessionsToPay;
    @FXML private TableColumn<ProgramPaymentModel, BigDecimal> colSubtotal;

    @FXML private ComboBox<Payment.PaymentMethod> cmbPaymentMethod;
    @FXML private TextField txtDiscount;

    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblTotalDue;

    private Patient currentPatient;
    private List<TherapyProgram> enrolledPrograms;
    private Runnable onSuccessCallback;

    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();
    private final PaymentBOImpl paymentService = new PaymentBOImpl();

    private final ObservableList<ProgramPaymentModel> paymentModels = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbPaymentMethod.setItems(FXCollections.observableArrayList(Payment.PaymentMethod.values()));
        
        setupTable();
        
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotals());
    }

    private void setupTable() {
        colProgramName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProgram().getName()));
        colTotalSessions.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getProgram().getTotalSessions() != null ? d.getValue().getProgram().getTotalSessions() : 1));
        
        colSessionsToPay.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSessionSelector()));
        
        colSubtotal.setCellValueFactory(d -> d.getValue().subtotalProperty());
    }

    public void initData(Patient patient, List<TherapyProgram> programs, Runnable onSuccess) {
        this.currentPatient = patient;
        this.enrolledPrograms = programs;
        this.onSuccessCallback = onSuccess;
        
        loadPrograms();
    }

    private void loadPrograms() {
        paymentModels.clear();
        for (TherapyProgram program : enrolledPrograms) {
            ProgramPaymentModel model = new ProgramPaymentModel(program);
            
            // Add listener to recalculate totals when combo box changes
            model.getSessionSelector().valueProperty().addListener((obs, oldVal, newVal) -> {
                model.updateSubtotal();
                calculateTotals();
                tblPaymentPrograms.refresh();
            });
            
            paymentModels.add(model);
        }
        tblPaymentPrograms.setItems(paymentModels);
        calculateTotals();
    }

    private void calculateTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ProgramPaymentModel model : paymentModels) {
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
            if (currentPatient == null || currentPatient.getId() == null) {
                AlertUtil.showWarning("Warning", "Patient must be registered first before processing upfront payment.");
                return;
            }

            Payment.PaymentMethod method = cmbPaymentMethod.getValue();
            if (method == null) {
                AlertUtil.showWarning("Warning", "Please select a payment method.");
                return;
            }

            BigDecimal discount = parseDiscount();
            BigDecimal subtotal = BigDecimal.ZERO;
            List<TherapySession> sessionsToPayFor = new ArrayList<>();

            // Find unscheduled sessions for the patient
            List<TherapySession> allUnscheduled = sessionService.findUnscheduledByPatient(currentPatient.getId());

            for (ProgramPaymentModel model : paymentModels) {
                int selectedSessions = model.getSessionSelector().getValue();
                if (selectedSessions > 0) {
                    subtotal = subtotal.add(model.getSubtotal());
                    
                    // Filter unscheduled sessions for this program and grab the selected number
                    long programId = model.getProgram().getId();
                    List<TherapySession> programSessions = allUnscheduled.stream()
                            .filter(s -> s.getProgram() != null && s.getProgram().getId() == programId && s.getPaymentStatus() == TherapySession.PaymentStatus.PENDING)
                            .limit(selectedSessions)
                            .toList();
                            
                    if (programSessions.size() < selectedSessions) {
                        AlertUtil.showWarning("Warning", "Not enough pending sessions available for " + model.getProgram().getName() + ".");
                        return;
                    }
                    sessionsToPayFor.addAll(programSessions);
                }
            }

            if (sessionsToPayFor.isEmpty()) {
                AlertUtil.showWarning("Warning", "Please select at least one session to pay for.");
                return;
            }

            BigDecimal totalDue = subtotal.subtract(discount);
            if (totalDue.signum() < 0) totalDue = BigDecimal.ZERO;

            Payment payment = new Payment();
            payment.setPatient(currentPatient);
            payment.setAmount(totalDue);
            payment.setMethod(method);
            payment.setDiscount(discount);
            payment.setPaymentType(Payment.PaymentType.UPFRONT);
            payment.setDescription("Upfront package payment for " + sessionsToPayFor.size() + " sessions.");

            paymentService.processUpfrontPayment(payment, sessionsToPayFor);
            
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
        Stage stage = (Stage) btnCloseOrAnyNode().getScene().getWindow();
        stage.close();
    }
    
    private javafx.scene.Node btnCloseOrAnyNode() {
        return tblPaymentPrograms;
    }

    /**
     * Inner class for table row model
     */
    public class ProgramPaymentModel {
        private final TherapyProgram program;
        private final ComboBox<Integer> sessionSelector;
        private final SimpleObjectProperty<BigDecimal> subtotalProp;

        public ProgramPaymentModel(TherapyProgram program) {
            this.program = program;
            
            int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
            ObservableList<Integer> options = FXCollections.observableArrayList();
            for (int i = 0; i <= total; i++) {
                options.add(i);
            }
            
            this.sessionSelector = new ComboBox<>(options);
            // Default to full package if enrolled, or 0
            this.sessionSelector.getSelectionModel().select(Integer.valueOf(total));
            
            this.subtotalProp = new SimpleObjectProperty<>(calculateLineTotal(total));
        }

        public void updateSubtotal() {
            int selected = sessionSelector.getValue() != null ? sessionSelector.getValue() : 0;
            this.subtotalProp.set(calculateLineTotal(selected));
        }

        private BigDecimal calculateLineTotal(int sessions) {
            if (sessions == 0) return BigDecimal.ZERO;
            
            // If sessionFee is set, use it. Otherwise, fee / totalSessions
            if (program.getSessionFee() != null) {
                return program.getSessionFee().multiply(new BigDecimal(sessions));
            } else if (program.getFee() != null) {
                int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
                BigDecimal perSession = program.getFee().divide(new BigDecimal(total), 2, java.math.RoundingMode.HALF_UP);
                return perSession.multiply(new BigDecimal(sessions));
            }
            return BigDecimal.ZERO;
        }

        public TherapyProgram getProgram() { return program; }
        public ComboBox<Integer> getSessionSelector() { return sessionSelector; }
        public BigDecimal getSubtotal() { return subtotalProp.get(); }
        public SimpleObjectProperty<BigDecimal> subtotalProperty() { return subtotalProp; }
    }
}
