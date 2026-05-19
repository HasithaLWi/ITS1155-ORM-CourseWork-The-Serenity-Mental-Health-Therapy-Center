package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PaymentBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapySessionBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PaymentTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentManagementController implements Initializable {

    @FXML private ComboBox<TherapySessionDTO> cmbPaymentSession;
    @FXML private TextField txtPaymentAmount;
    @FXML private ComboBox<PaymentMethod> cmbPaymentMethod;
    @FXML private Label lblTotalRevenue;

    @FXML private TableView<PaymentTM> tblPayments;
    @FXML private TableColumn<PaymentTM, String> colPaymentId;
    @FXML private TableColumn<PaymentTM, String> colPaymentPatient;
    @FXML private TableColumn<PaymentTM, BigDecimal> colPaymentAmount;
    @FXML private TableColumn<PaymentTM, String> colPaymentMethod;
    @FXML private TableColumn<PaymentTM, String> colPaymentDate;
    @FXML private TableColumn<PaymentTM, String> colPaymentStatus;

    private final PaymentBOImpl paymentService = new PaymentBOImpl();
    private final TherapySessionBOImpl sessionService = new TherapySessionBOImpl();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadComboBoxes();
        setupTable();
        loadData();
    }

    private void loadComboBoxes() {
        try {
            cmbPaymentSession.setItems(FXCollections.observableArrayList(sessionService.getAllSessionDTOs()));
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }

        cmbPaymentMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));

        cmbPaymentSession.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TherapySessionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("");
                else setText("#" + item.getId() + " - " + (item.getPatientName() != null ? item.getPatientName() : "N/A")
                    + " (" + item.getSessionDate() + ")");
            }
        });
        cmbPaymentSession.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TherapySessionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("");
                else setText("#" + item.getId() + " - " + (item.getPatientName() != null ? item.getPatientName() : "N/A")
                    + " (" + item.getSessionDate() + ")");
            }
        });
    }

    private void setupTable() {
        colPaymentId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colPaymentPatient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPatientName()));
        colPaymentAmount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getAmount()));
        colPaymentMethod.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMethod()));
        colPaymentDate.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPaymentDate() != null
                ? d.getValue().getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));
        colPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
    }

    private void loadData() {
        try {
            List<PaymentDTO> dtos = paymentService.getAllPayments();
            List<PaymentTM> tms = dtos.stream().map(dto -> new PaymentTM(
                    String.format("PAY%03d", dto.getId()),
                    dto.getAmount(),
                    dto.getPaymentDate(),
                    dto.getMethod() != null ? dto.getMethod().name() : "",
                    dto.getStatus() != null ? dto.getStatus().name() : "",
                    dto.getPaymentType() != null ? dto.getPaymentType().name() : "",
                    dto.getPatientName() != null ? dto.getPatientName() : "N/A",
                    dto.getSessionId() != null ? String.format("S%03d", dto.getSessionId()) : ""
            )).toList();
            tblPayments.setItems(FXCollections.observableArrayList(tms));

            BigDecimal total = paymentService.getTotalRevenue();
            lblTotalRevenue.setText("Total Revenue: LKR " + (total != null ? total.toPlainString() : "0"));
        } catch (Exception e) { AlertUtil.showError("Error", "Failed to load payments: " + e.getMessage()); }
    }

    @FXML void handleProcessPayment(ActionEvent event) {
        try {
            TherapySessionDTO selectedSession = cmbPaymentSession.getValue();
            if (selectedSession == null) {
                AlertUtil.showWarning("Warning", "Please select a session.");
                return;
            }

            PaymentDTO dto = new PaymentDTO();
            dto.setSessionId(selectedSession.getId());
            String amtText = txtPaymentAmount.getText();
            dto.setAmount(amtText != null && !amtText.trim().isEmpty() ? new BigDecimal(amtText.trim()) : null);
            dto.setMethod(cmbPaymentMethod.getValue());
            paymentService.processPayment(dto);
            AlertUtil.showInfo("Success", "Payment processed.");
            handleClearPayment(event);
            loadData();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void handleClearPayment(ActionEvent event) {
        cmbPaymentSession.setValue(null);
        txtPaymentAmount.clear();
        cmbPaymentMethod.setValue(null);
    }
}
