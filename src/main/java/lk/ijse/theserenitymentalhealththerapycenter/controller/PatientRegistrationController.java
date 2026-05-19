package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramPaymentTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

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
    private ComboBox<TherapyProgramDTO> cmbPatientProgram;
    @FXML
    private Label lblRegMessage;
    @FXML
    private Button btnAddPgm;
    @FXML
    private TextArea txtInterviewNote;

    @FXML
    private TableView<ProgramPaymentTM> tblSelectedPgm;
    @FXML
    private TableColumn<ProgramPaymentTM, String> colProgramName;
    @FXML
    private TableColumn<ProgramPaymentTM, Integer> colTotalSessions;
    @FXML
    private TableColumn<ProgramPaymentTM, Integer> colSessionsToPay;
    @FXML
    private TableColumn<ProgramPaymentTM, BigDecimal> colSubtotal;
    @FXML
    private TableColumn<ProgramPaymentTM, Void> colAction;

    @FXML
    private ComboBox<PaymentMethod> cmbPaymentMethod;
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
    private final PaymentBOImpl paymentService = new PaymentBOImpl();

    private final ObservableList<ProgramPaymentTM> paymentModels = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            cmbPatientProgram.setItems(FXCollections.observableArrayList(programService.getAllPrograms()));
        } catch (Exception e) {
            System.err.println("Error loading programs: " + e.getMessage());
        }

        cmbPatientProgram.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TherapyProgramDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Therapy Program" : item.getName());
            }
        });
        cmbPatientProgram.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TherapyProgramDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Therapy Program" : item.getName());
            }
        });

        cmbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));
        txtDiscount.textProperty().addListener((obs, oldVal, newVal) -> calculateTotals());

        cmbPatientProgram.valueProperty().addListener((obs, oldPgm, newPgm) -> {
            if (newPgm != null) {
                boolean alreadyAdded = paymentModels.stream()
                        .anyMatch(m -> m.getProgram().getId() == newPgm.getId());
                if (!alreadyAdded) {
                    int total = newPgm.getTotalSessions() != null ? newPgm.getTotalSessions() : 1;
                    BigDecimal subtotal = calculateLineTotal(newPgm, total);
                    ProgramPaymentTM model = new ProgramPaymentTM(newPgm, newPgm.getName(), total, total, subtotal);
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

        colProgramName.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colTotalSessions.setCellValueFactory(new PropertyValueFactory<>("totalSessions"));
        colSessionsToPay.setCellValueFactory(new PropertyValueFactory<>("sessionsToPay"));
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
                    for (int i = 0; i <= total; i++) {
                        options.add(i);
                    }
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

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Remove");
            {
                btnRemove.getStyleClass().add("btn-Pgm-remove");
                btnRemove.setOnAction(event -> {
                    if (isEmpty() || getIndex() < 0 || getIndex() >= getTableView().getItems().size())
                        return;
                    ProgramPaymentTM model = getTableView().getItems().get(getIndex());
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
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ProgramPaymentTM model : paymentModels) {
            subtotal = subtotal.add(model.getSubtotal());
        }

        BigDecimal discount = parseDiscount();
        BigDecimal totalDue = subtotal.subtract(discount);

        if (totalDue.signum() < 0)
            totalDue = BigDecimal.ZERO;

        lblSubtotal.setText(subtotal.toPlainString() + " LKR");
        lblDiscount.setText("- " + discount.toPlainString() + " LKR");
        lblTotalDue.setText(totalDue.toPlainString() + " LKR");
    }

    private BigDecimal parseDiscount() {
        String text = txtDiscount.getText();
        if (text == null || text.trim().isEmpty())
            return BigDecimal.ZERO;
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    @FXML
    void handleRegisterPatient(ActionEvent event) {
        try {
            if (paymentModels.isEmpty()) {
                AlertUtil.showWarning("Warning", "Please add at least one program.");
                return;
            }

            BigDecimal discount = parseDiscount();
            BigDecimal subtotal = BigDecimal.ZERO;

            for (ProgramPaymentTM model : paymentModels) {
                subtotal = subtotal.add(model.getSubtotal());
            }

            BigDecimal totalDue = subtotal.subtract(discount);
            if (totalDue.signum() < 0)
                totalDue = BigDecimal.ZERO;

            PaymentMethod method = cmbPaymentMethod.getValue();
            if (totalDue.compareTo(BigDecimal.ZERO) > 0 && method == null) {
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

            ArrayList<TherapyProgramDTO> programs = new ArrayList<>();
            Map<Long, Integer> upfrontMap = new HashMap<>();

            for (ProgramPaymentTM model : paymentModels) {
                programs.add(model.getProgram());
                int sessionsToPay = model.getSessionsToPay();
                upfrontMap.put(model.getProgram().getId(), sessionsToPay);
            }
            p.setPrograms(programs);
            p.setUpfrontSessionsPerProgram(upfrontMap);

            // 2. Register patient — saves PatientTherapyProgram records with upfront credit
            Long patientId = patientService.registerPatient(p);

            // 3. If there's an upfront payment, record it via PaymentBO
            if (totalDue.compareTo(BigDecimal.ZERO) > 0) {
                int totalSessionsPaid = upfrontMap.values().stream().mapToInt(Integer::intValue).sum();

                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setPatientId(patientId);
                paymentDTO.setAmount(totalDue);
                paymentDTO.setMethod(method);
                paymentDTO.setDiscount(discount);
                paymentDTO.setDescription("Upfront payment at registration for " + totalSessionsPaid + " sessions.");

                paymentService.saveRegistrationPayment(paymentDTO);
            }

            lblRegMessage.setText("Patient registered & upfront credit saved successfully!");
            lblRegMessage.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 12px;");
            AlertUtil.showInfo("Success",
                    "Patient registered successfully. Sessions will be created on-demand in Session Management.");
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

    private BigDecimal calculateLineTotal(TherapyProgramDTO program, int sessions) {
        if (sessions == 0)
            return BigDecimal.ZERO;

        if (program.getSessionFee() != null) {
            return program.getSessionFee().multiply(new BigDecimal(sessions));
        } else if (program.getFee() != null) {
            int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
            BigDecimal perSession = program.getFee().divide(new BigDecimal(total), 2,
                    RoundingMode.HALF_UP);
            return perSession.multiply(new BigDecimal(sessions));
        }
        return BigDecimal.ZERO;
    }
}
