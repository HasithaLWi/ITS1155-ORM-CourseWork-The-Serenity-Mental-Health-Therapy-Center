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
import lk.ijse.theserenitymentalhealththerapycenter.config.FactoryConfiguration;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramSummaryTM;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Payment;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.JasperReportUtil;
import org.hibernate.Session;

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
        try (Session session = FactoryConfiguration.getInstance().getSession()) {
            // 1. Total Revenue
            BigDecimal revenue = session.createQuery(
                    "SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status",
                    BigDecimal.class
            ).setParameter("status", Payment.PaymentStatus.COMPLETED).uniqueResult();
            rawRevenue = revenue != null ? revenue : BigDecimal.ZERO;
            lblReportRevenue.setText("LKR " + String.format("%,.2f", rawRevenue));

            // 2. Total Patients
            Long patientCount = session.createQuery("SELECT COUNT(p) FROM Patient p", Long.class).uniqueResult();
            rawPatients = patientCount != null ? patientCount : 0;
            lblReportPatients.setText(String.valueOf(rawPatients));

            // 3. Total Sessions
            Long sessionCount = session.createQuery("SELECT COUNT(s) FROM TherapySession s", Long.class).uniqueResult();
            rawSessions = sessionCount != null ? sessionCount : 0;
            lblReportSessions.setText(String.valueOf(rawSessions));

            // 4. Active Therapists
            Long therapistCount = session.createQuery(
                    "SELECT COUNT(t) FROM Therapist t WHERE t.status = :status",
                    Long.class
            ).setParameter("status", Therapist.Status.ACTIVE).uniqueResult();
            rawTherapists = therapistCount != null ? therapistCount : 0;
            lblReportTherapists.setText(String.valueOf(rawTherapists));

            // 5. Program Summaries
            List<ProgramSummaryTM> programList = session.createQuery(
                    "SELECT new lk.ijse.theserenitymentalhealththerapycenter.dto.tm.ProgramSummaryTM(tp.name, tp.duration, tp.fee, COUNT(ptp.id)) " +
                    "FROM TherapyProgram tp " +
                    "LEFT JOIN tp.patientTherapyPrograms ptp " +
                    "GROUP BY tp.id, tp.name, tp.duration, tp.fee " +
                    "ORDER BY COUNT(ptp.id) DESC",
                    ProgramSummaryTM.class
            ).list();

            tblProgramSummary.getItems().setAll(programList);

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "Failed to load dashboard and analytics: " + e.getMessage());
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
