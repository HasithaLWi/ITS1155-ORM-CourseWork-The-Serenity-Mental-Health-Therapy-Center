package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.PaymentService;
import lk.ijse.theserenitymentalhealththerapycenter.bo.TherapySessionService;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PaymentManagementController implements Initializable {

    @FXML private ComboBox<TherapySession> cmbPaymentSession;
    @FXML private TextField txtPaymentAmount;
    @FXML private ComboBox<Payment.PaymentMethod> cmbPaymentMethod;
    @FXML private Label lblTotalRevenue;

    @FXML private TableView<Payment> tblPayments;
    @FXML private TableColumn<Payment, Long> colPaymentId;
    @FXML private TableColumn<Payment, String> colPaymentPatient;
    @FXML private TableColumn<Payment, BigDecimal> colPaymentAmount;
    @FXML private TableColumn<Payment, String> colPaymentMethod;
    @FXML private TableColumn<Payment, String> colPaymentDate;
    @FXML private TableColumn<Payment, String> colPaymentStatus;

    private final PaymentService paymentService = new PaymentService();
    private final TherapySessionService sessionService = new TherapySessionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadComboBoxes();
        setupTable();
        loadData();
    }

    private void loadComboBoxes() {
        try {
            cmbPaymentSession.setItems(FXCollections.observableArrayList(sessionService.getAllSessions()));
        } catch (Exception e) { System.err.println("Error: " + e.getMessage()); }

        cmbPaymentMethod.setItems(FXCollections.observableArrayList(Payment.PaymentMethod.values()));

        cmbPaymentSession.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TherapySession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("");
                else setText("#" + item.getId() + " - " + (item.getPatient() != null ? item.getPatient().getName() : "N/A")
                    + " (" + item.getSessionDate() + ")");
            }
        });
        cmbPaymentSession.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TherapySession item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText("");
                else setText("#" + item.getId() + " - " + (item.getPatient() != null ? item.getPatient().getName() : "N/A")
                    + " (" + item.getSessionDate() + ")");
            }
        });
    }

    private void setupTable() {
        colPaymentId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colPaymentPatient.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getSession() != null && d.getValue().getSession().getPatient() != null
                ? d.getValue().getSession().getPatient().getName() : "N/A"));
        colPaymentAmount.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getAmount()));
        colPaymentMethod.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getMethod() != null ? d.getValue().getMethod().name() : ""));
        colPaymentDate.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getPaymentDate() != null
                ? d.getValue().getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : ""));
        colPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""));
    }

    private void loadData() {
        try {
            tblPayments.setItems(FXCollections.observableArrayList(paymentService.getAllPayments()));
            BigDecimal total = paymentService.getTotalRevenue();
            lblTotalRevenue.setText("Total Revenue: LKR " + (total != null ? total.toPlainString() : "0"));
        } catch (Exception e) { AlertUtil.showError("Error", "Failed to load payments: " + e.getMessage()); }
    }

    @FXML void handleProcessPayment(ActionEvent event) {
        try {
            Payment p = new Payment();
            p.setSession(cmbPaymentSession.getValue());
            String amtText = txtPaymentAmount.getText();
            p.setAmount(amtText != null && !amtText.trim().isEmpty() ? new BigDecimal(amtText.trim()) : null);
            p.setMethod(cmbPaymentMethod.getValue());
            paymentService.processPayment(p);
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
