package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.*;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.TherapySessionTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardOverviewController implements Initializable {

    @FXML
    private Label lblTotalPatients;

    @FXML
    private Label lblTotalTherapists;

    @FXML
    private Label lblTotalPrograms;

    @FXML
    private Label lblMonthlyRevenue;

    @FXML
    private Label lblTodayCount;

    @FXML
    private TableView<TherapySessionTM> tblTodaySessions;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayDate;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayTime;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayPatient;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayTherapist;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayProgram;

    @FXML
    private TableColumn<TherapySessionTM, String> colTodayStatus;

    private final PatientBO patientService = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BOType.PATIENT);
    private final TherapistBO therapistService = (TherapistBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPIST);
    private final TherapyProgramBO programService = (TherapyProgramBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_PROGRAM);
    private final PaymentBO paymentService = (PaymentBO) BOFactory.getInstance().getBO(BOFactory.BOType.PAYMENT);
    private final TherapySessionBO sessionService = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadStats();
        loadSessions();
    }

    private void setupTable() {
        colTodayDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""
        ));
        colTodayTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""
        ));
        colTodayPatient.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        colTodayTherapist.setCellValueFactory(new PropertyValueFactory<>("therapistName"));
        colTodayProgram.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colTodayStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadStats() {
        try {
            long patientCount = patientService.getPatientCount();
            lblTotalPatients.setText(String.valueOf(patientCount));

            long therapistCount = therapistService.getTherapistCount();
            lblTotalTherapists.setText(String.valueOf(therapistCount));

            long programCount = programService.getProgramCount();
            lblTotalPrograms.setText(String.valueOf(programCount));

            BigDecimal revenue = paymentService.getMonthlyRevenue();
            if (revenue == null) {
                revenue = BigDecimal.ZERO;
            }
            lblMonthlyRevenue.setText(String.format("LKR %,.2f", revenue));
        } catch (Exception e) {
            System.err.println("Error loading dashboard stats: " + e.getMessage());
        }
    }

    private void loadSessions() {
        try {
            List<TherapySessionDTO> sessions = sessionService.getScheduledSessionsSortedByDate();
            List<TherapySessionTM> tms = sessions.stream().map(this::toTM).toList();

            tblTodaySessions.setItems(FXCollections.observableArrayList(tms));
            lblTodayCount.setText(tms.size() + " sessions");
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load scheduled sessions: " + e.getMessage());
        }
    }

    private TherapySessionTM toTM(TherapySessionDTO d) {
        return new TherapySessionTM(
                d.getId() == 0 ? "" : "S" + String.format("%03d", d.getId()),
                d.getSequenceNumber(),
                d.getSessionDate(),
                d.getSessionTime(),
                d.getStatus() != null ? d.getStatus().name() : "",
                d.getPaymentStatus() != null ? d.getPaymentStatus().name() : "",
                d.getPatientName(),
                d.getTherapistName(),
                d.getProgramName(),
                d.getPatientPhone()
        );
    }
}
