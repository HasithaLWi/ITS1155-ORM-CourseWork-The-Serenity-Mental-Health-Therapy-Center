package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.Locale.filter;

public class PatientRegistrationController implements Initializable {

    @FXML
    private TextField txtPatientName;
    @FXML
    private TextField txtPatientEmail;
    @FXML
    private TextField txtPatientPhone;
    @FXML
    private TextField txtPatientAddress;
    @FXML
    private ComboBox<TherapyProgram> cmbPatientProgram;
    @FXML
    private Label lblRegMessage;
    @FXML
    private Button btnAddPgm;
    @FXML
    private TextArea txtInterviewNote;

    @FXML
    private TableView<ProgramPaymentModel> tblSelectedPgm;
    @FXML
    private TableColumn<ProgramPaymentModel, String> colProgramName;
    @FXML
    private TableColumn<ProgramPaymentModel, Integer> colTotalSessions;
    @FXML
    private TableColumn<ProgramPaymentModel, ComboBox<Integer>> colSessionsToPay;
    @FXML
    private TableColumn<ProgramPaymentModel, java.math.BigDecimal> colSubtotal;
    @FXML
    private TableColumn<ProgramPaymentModel, Void> colAction;

    @FXML
    private ComboBox<lk.ijse.theserenitymentalhealththerapycenter.entity.Payment.PaymentMethod> cmbPaymentMethod;
    @FXML
    private TextField txtDiscount;
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblDiscount;
    @FXML
    private Label lblTotalDue;

    private final PatientBOImpl patientService = new PatientBOImpl();
    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
    private final lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl paymentService = new lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl();

    private final ObservableList<ProgramPaymentModel> paymentModels = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            cmbPatientProgram.setItems(FXCollections.observableArrayList(programService.getAllPrograms()));
        } catch (Exception e) {
            System.err.println("Error loading programs: " + e.getMessage());
        }

        cmbPatientProgram.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Therapy Program" : item.getName());
            }
        });
        cmbPatientProgram.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Therapy Program" : item.getName());
            }
        });

        cmbPaymentMethod.setItems(FXCollections.observableArrayList(
                lk.ijse.theserenitymentalhealththerapycenter.entity.Payment.PaymentMethod.values()));
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotals());

        cmbPatientProgram.valueProperty().addListener((obs, oldPgm, newPgm) -> {
            if (newPgm != null) {
                boolean alreadyAdded = paymentModels.stream()
                        .anyMatch(m -> m.getProgram().getId().equals(newPgm.getId()));
                if (!alreadyAdded) {
                    ProgramPaymentModel model = new ProgramPaymentModel(newPgm);
                    model.getSessionSelector().valueProperty().addListener((o, oldV, newV) -> {
                        model.updateSubtotal();
                        calculateTotals();
                        tblSelectedPgm.refresh();
                    });
                    paymentModels.add(model);
                    calculateTotals();
                }
                Platform.runLater(() -> cmbPatientProgram.setValue(null));
            }
        });

        setupTable();
    }

    private void setupTable() {
        tblSelectedPgm.setItems(paymentModels);
        tblSelectedPgm.setPlaceholder(new Label("No programs added yet"));

        colProgramName.setCellValueFactory(
                d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getProgram().getName()));
        colTotalSessions.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(
                d.getValue().getProgram().getTotalSessions() != null ? d.getValue().getProgram().getTotalSessions()
                        : 1));
        colSessionsToPay.setCellValueFactory(
                d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getSessionSelector()));
        colSubtotal.setCellValueFactory(d -> d.getValue().subtotalProperty());

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Remove");
            {
                btnRemove.getStyleClass().add("btn-Pgm-remove");
                btnRemove.setOnAction(event -> {
                    if (isEmpty() || getIndex() < 0 || getIndex() >= getTableView().getItems().size())
                        return;
                    ProgramPaymentModel model = getTableView().getItems().get(getIndex());
                    paymentModels.remove(model);
                    calculateTotals();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
                setText(null);
            }
        });
    }

    private void calculateTotals() {
        java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
        for (ProgramPaymentModel model : paymentModels) {
            subtotal = subtotal.add(model.getSubtotal());
        }

        java.math.BigDecimal discount = parseDiscount();
        java.math.BigDecimal totalDue = subtotal.subtract(discount);

        if (totalDue.signum() < 0)
            totalDue = java.math.BigDecimal.ZERO;

        lblSubtotal.setText(subtotal.toPlainString() + " LKR");
        lblDiscount.setText("- " + discount.toPlainString() + " LKR");
        lblTotalDue.setText(totalDue.toPlainString() + " LKR");
    }

    private java.math.BigDecimal parseDiscount() {
        String text = txtDiscount.getText();
        if (text == null || text.trim().isEmpty())
            return java.math.BigDecimal.ZERO;
        try {
            return new java.math.BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    @FXML
    void handleRegisterPatient(ActionEvent event) {
        try {
            if (paymentModels.isEmpty()) {
                AlertUtil.showWarning("Warning", "Please add at least one program.");
                return;
            }

            java.math.BigDecimal discount = parseDiscount();
            java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;

            for (ProgramPaymentModel model : paymentModels) {
                subtotal = subtotal.add(model.getSubtotal());
            }

            java.math.BigDecimal totalDue = subtotal.subtract(discount);
            if (totalDue.signum() < 0)
                totalDue = java.math.BigDecimal.ZERO;

            lk.ijse.theserenitymentalhealththerapycenter.entity.Payment.PaymentMethod method = cmbPaymentMethod
                    .getValue();
            if (totalDue.compareTo(java.math.BigDecimal.ZERO) > 0 && method == null) {
                AlertUtil.showWarning("Warning", "Please select a payment method for the upfront payment.");
                return;
            }

            // 1. Build the PatientDTO with programs and upfront session counts
            PatientDTO p = new PatientDTO();
            p.setName(txtPatientName.getText());
            p.setEmail(txtPatientEmail.getText());
            p.setPhone(txtPatientPhone.getText());
            p.setAddress(txtPatientAddress.getText());
            p.setInterviewNote(txtInterviewNote.getText());

            ArrayList<TherapyProgram> programs = new ArrayList<>();
            Map<Long, Integer> upfrontMap = new HashMap<>();

            for (ProgramPaymentModel model : paymentModels) {
                programs.add(model.getProgram());
                int sessionsToPay = model.getSessionSelector().getValue() != null ? model.getSessionSelector().getValue() : 0;
                upfrontMap.put(model.getProgram().getId(), sessionsToPay);
            }
            p.setPrograms(programs);
            p.setUpfrontSessionsPerProgram(upfrontMap);

            // 2. Register patient — saves PatientTherapyProgram records with upfront credit (NO sessions created)
            Long patientId = patientService.registerPatient(p);

            // 3. If there's an upfront payment, record the payment
            if (totalDue.compareTo(java.math.BigDecimal.ZERO) > 0) {
                lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl pDao = new lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PatientDAOImpl();
                lk.ijse.theserenitymentalhealththerapycenter.entity.Patient registeredPatient = pDao.getById(patientId);

                lk.ijse.theserenitymentalhealththerapycenter.entity.Payment payment = new lk.ijse.theserenitymentalhealththerapycenter.entity.Payment();
                payment.setPatient(registeredPatient);
                payment.setAmount(totalDue);
                payment.setMethod(method);
                payment.setDiscount(discount);
                payment.setPaymentType(
                        lk.ijse.theserenitymentalhealththerapycenter.entity.Payment.PaymentType.UPFRONT);

                int totalSessionsPaid = upfrontMap.values().stream().mapToInt(Integer::intValue).sum();
                payment.setDescription("Upfront payment at registration for " + totalSessionsPaid + " sessions.");
                payment.setStatus(lk.ijse.theserenitymentalhealththerapycenter.entity.Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(java.time.LocalDateTime.now());

                // Save the payment record (no session linking needed — credit is tracked in PatientTherapyProgram)
                new lk.ijse.theserenitymentalhealththerapycenter.dao.custom.impl.PaymentDAOImpl().save(payment);
            }

            lblRegMessage.setText("Patient registered & upfront credit saved successfully!");
            lblRegMessage.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 12px;");
            AlertUtil.showInfo("Success", "Patient registered successfully. Sessions will be created on-demand in Session Management.");
            handleClearPatientForm(event);

        } catch (Exception e) {
            lblRegMessage.setText(e.getMessage());
            lblRegMessage.setStyle("-fx-text-fill: #C47171; -fx-font-size: 12px;");
            AlertUtil.showError("Error", "Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearPatientForm(ActionEvent event) {
        txtPatientName.clear();
        txtPatientEmail.clear();
        txtPatientPhone.clear();
        txtPatientAddress.clear();
        txtInterviewNote.clear();
        cmbPatientProgram.setValue(null);
        cmbPaymentMethod.setValue(null);
        txtDiscount.clear();
        paymentModels.clear();
        calculateTotals();
    }

    /**
     * Inner class for table row model
     */
    public class ProgramPaymentModel {
        private final TherapyProgram program;
        private final ComboBox<Integer> sessionSelector;
        private final javafx.beans.property.SimpleObjectProperty<java.math.BigDecimal> subtotalProp;

        public ProgramPaymentModel(TherapyProgram program) {
            this.program = program;

            int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
            ObservableList<Integer> options = FXCollections.observableArrayList();
            for (int i = 0; i <= total; i++) {
                options.add(i);
            }

            this.sessionSelector = new ComboBox<>(options);
            // Default to full package
            this.sessionSelector.getSelectionModel().select(Integer.valueOf(total));

            this.subtotalProp = new javafx.beans.property.SimpleObjectProperty<>(calculateLineTotal(total));
        }

        public void updateSubtotal() {
            int selected = sessionSelector.getValue() != null ? sessionSelector.getValue() : 0;
            this.subtotalProp.set(calculateLineTotal(selected));
        }

        private java.math.BigDecimal calculateLineTotal(int sessions) {
            if (sessions == 0)
                return java.math.BigDecimal.ZERO;

            if (program.getSessionFee() != null) {
                return program.getSessionFee().multiply(new java.math.BigDecimal(sessions));
            } else if (program.getFee() != null) {
                int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
                java.math.BigDecimal perSession = program.getFee().divide(new java.math.BigDecimal(total), 2,
                        java.math.RoundingMode.HALF_UP);
                return perSession.multiply(new java.math.BigDecimal(sessions));
            }
            return java.math.BigDecimal.ZERO;
        }

        public TherapyProgram getProgram() {
            return program;
        }

        public ComboBox<Integer> getSessionSelector() {
            return sessionSelector;
        }

        public java.math.BigDecimal getSubtotal() {
            return subtotalProp.get();
        }

        public javafx.beans.property.SimpleObjectProperty<java.math.BigDecimal> subtotalProperty() {
            return subtotalProp;
        }
    }
}
