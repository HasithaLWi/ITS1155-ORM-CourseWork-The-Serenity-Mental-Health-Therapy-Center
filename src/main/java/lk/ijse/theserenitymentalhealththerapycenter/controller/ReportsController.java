package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.*;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramSummaryTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.JasperReportUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML
    private Label lblReportRevenue;
    @FXML
    private Label lblReportPatients;
    @FXML
    private Label lblReportSessions;
    @FXML
    private Label lblReportTherapists;
    @FXML
    private TableView<ProgramSummaryTM> tblProgramSummary;
    @FXML
    private TableColumn<ProgramSummaryTM, String> colRptProgramName;
    @FXML
    private TableColumn<ProgramSummaryTM, String> colRptProgramDuration;
    @FXML
    private TableColumn<ProgramSummaryTM, BigDecimal> colRptProgramFee;
    @FXML
    private TableColumn<ProgramSummaryTM, Long> colRptProgramPatients;
    @FXML
    private Button btnPrintFinancialReport;

    private final PaymentBO paymentBO = (PaymentBO) BOFactory.getInstance().getBO(BOFactory.BOType.PAYMENT);
    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BOType.PATIENT);
    private final TherapySessionBO sessionBO = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);
    private final TherapistBO therapistBO = (TherapistBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPIST);
    private final TherapyProgramBO programBO = (TherapyProgramBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_PROGRAM);

    private BigDecimal rawRevenue = BigDecimal.ZERO;
    private long rawPatients = 0;
    private long rawSessions = 0;
    private long rawTherapists = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadAnalytics();
    }

    private void setupTable() {
        colRptProgramName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRptProgramDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colRptProgramFee.setCellValueFactory(new PropertyValueFactory<>("fee"));
        colRptProgramPatients.setCellValueFactory(new PropertyValueFactory<>("enrolledPatients"));
    }

    private void loadAnalytics() {
        try {

            rawRevenue = paymentBO.getTotalRevenue();
            lblReportRevenue.setText("LKR " + String.format("%,.2f", rawRevenue));


            rawPatients = patientBO.getPatientCount();
            lblReportPatients.setText(String.valueOf(rawPatients));


            rawSessions = sessionBO.getSessionCount();
            lblReportSessions.setText(String.valueOf(rawSessions));


            rawTherapists = therapistBO.getActiveTherapistCount();
            lblReportTherapists.setText(String.valueOf(rawTherapists));

            List<ProgramSummaryTM> programList = programBO.getProgramSummaries();
            tblProgramSummary.getItems().setAll(programList);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Failed to load dashboard and analytics: " + e.getMessage());
        }
    }

    @FXML
    void handlePrintFinancialReport(ActionEvent event) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("totalRevenue", String.format("%,.2f", rawRevenue));
        parameters.put("totalPatients", String.valueOf(rawPatients));
        parameters.put("totalSessions", String.valueOf(rawSessions));
        parameters.put("activeTherapists", String.valueOf(rawTherapists));

        JasperReportUtil.printFinancialReport(parameters);
    }
}
