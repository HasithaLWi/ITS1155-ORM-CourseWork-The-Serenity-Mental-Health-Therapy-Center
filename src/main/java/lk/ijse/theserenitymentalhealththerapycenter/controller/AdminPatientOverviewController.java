package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ComboBoxAutoCompleteUtil;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminPatientOverviewController implements Initializable {

    @FXML
    private ComboBox<PatientDTO> cmbPatient;

    @FXML
    private VBox vboxPatientDetails;
    @FXML
    private Label lblPatientId;
    @FXML
    private Label lblPatientName;
    @FXML
    private Label lblPatientEmail;
    @FXML
    private Label lblPatientPhone;
    @FXML
    private Label lblPatientAddress;
    @FXML
    private Label lblPatientRegistered;
    @FXML
    private TextArea txtInterviewNote;

    @FXML
    private VBox vboxProgramsOverview;
    @FXML
    private VBox vboxProgramsContainer;

    @FXML
    private VBox vboxSessionHistory;
    @FXML
    private ComboBox<PatientTherapyProgramDTO> cmbProgramDetail;

    @FXML
    private TableView<TherapySessionDTO> tblSessions;
    @FXML
    private TableColumn<TherapySessionDTO, Integer> colSeqNo;
    @FXML
    private TableColumn<TherapySessionDTO, String> colDate;
    @FXML
    private TableColumn<TherapySessionDTO, String> colTime;
    @FXML
    private TableColumn<TherapySessionDTO, String> colTherapist;
    @FXML
    private TableColumn<TherapySessionDTO, String> colStatus;
    @FXML
    private TableColumn<TherapySessionDTO, String> colPaymentStatus;

    @FXML
    private VBox vboxSessionNotes;
    @FXML
    private Label lblSessionId;
    @FXML
    private Label lblSessionSeq;
    @FXML
    private Label lblSessionTherapist;
    @FXML
    private Label lblSessionDate;
    @FXML
    private Label lblSessionTime;
    @FXML
    private Label lblSessionPayment;
    @FXML
    private TextArea txtSessionNotes;

    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BOType.PATIENT);
    private final TherapySessionBO sessionBO = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);

    private List<PatientDTO> allPatients;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTables();
        loadAllPatients();
        setupListeners();
    }

    private void setupTables() {
        colSeqNo.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSequenceNumber()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionDate() != null ? d.getValue().getSessionDate().toString() : ""
        ));
        colTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getSessionTime() != null ? d.getValue().getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : ""
        ));
        colTherapist.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTherapistName() != null ? d.getValue().getTherapistName() : ""
        ));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""
        ));
        colPaymentStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPaymentStatus() != null ? d.getValue().getPaymentStatus().name() : ""
        ));


        colPaymentStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("badge-paid", "badge-pending");
                } else {
                    setText(item);
                    if ("PAID".equals(item)) {
                        getStyleClass().add("badge-paid");
                        getStyleClass().remove("badge-pending");
                    } else {
                        getStyleClass().add("badge-pending");
                        getStyleClass().remove("badge-paid");
                    }
                }
            }
        });

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("COMPLETED".equals(item)) {
                        setStyle("-fx-text-fill: #7AB88F; -fx-font-weight: bold;");
                    } else if ("SCHEDULED".equals(item)) {
                        setStyle("-fx-text-fill: #4A7FA5; -fx-font-weight: bold;");
                    } else if ("CANCELLED".equals(item)) {
                        setStyle("-fx-text-fill: #C47171; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2D3436;");
                    }
                }
            }
        });
    }

    private void loadAllPatients() {
        try {
            allPatients = patientBO.getAllPatients();
            ComboBoxAutoCompleteUtil.setupAutocomplete(
                    cmbPatient,
                    allPatients,
                    p -> p.getStringId() + " - " + p.getName(),
                    p -> p.getStringId() + " " + p.getName() + " " + (p.getPhone() != null ? p.getPhone() : "")
            );
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load patients: " + e.getMessage());
        }
    }

    private void setupListeners() {
        cmbPatient.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadPatientData(newVal);
            } else {
                clearPatientDetails();
            }
        });

        cmbProgramDetail.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && cmbPatient.getValue() != null) {
                loadProgramSessions(cmbPatient.getValue().getId(), newVal.getProgramId());
            } else {
                tblSessions.setVisible(false);
                tblSessions.setManaged(false);
                tblSessions.setItems(FXCollections.observableArrayList());
                vboxSessionNotes.setVisible(false);
                vboxSessionNotes.setManaged(false);
            }
        });

        tblSessions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showSessionDetails(newVal);
            } else {
                vboxSessionNotes.setVisible(false);
                vboxSessionNotes.setManaged(false);
            }
        });
    }

    private void loadPatientData(PatientDTO patient) {
        // Fetch full/latest details
        try {
            PatientDTO fullPatient = patientBO.getPatientById(patient.getId());
            lblPatientId.setText(fullPatient.getStringId());
            lblPatientName.setText(fullPatient.getName());
            lblPatientEmail.setText(fullPatient.getEmail() != null ? fullPatient.getEmail() : "N/A");
            lblPatientPhone.setText(fullPatient.getPhone() != null ? fullPatient.getPhone() : "N/A");
            lblPatientAddress.setText(fullPatient.getAddress() != null ? fullPatient.getAddress() : "N/A");
            lblPatientRegistered.setText(fullPatient.getRegisteredDate() != null
                    ? fullPatient.getRegisteredDate().toString() : "N/A");
            txtInterviewNote.setText(fullPatient.getInterviewNote() != null ? fullPatient.getInterviewNote() : "");

            vboxPatientDetails.setVisible(true);
            vboxPatientDetails.setManaged(true);

            renderProgramStats(fullPatient);
            vboxProgramsOverview.setVisible(true);
            vboxProgramsOverview.setManaged(true);

            loadProgramDropdown(fullPatient.getId());
            vboxSessionHistory.setVisible(true);
            vboxSessionHistory.setManaged(true);

        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to retrieve patient details: " + e.getMessage());
        }
    }

    private void renderProgramStats(PatientDTO patient) {
        vboxProgramsContainer.getChildren().clear();
        try {
            List<PatientTherapyProgramDTO> enrollments = patientBO.getPatientPrograms(patient.getId());
            List<TherapySessionDTO> patientSessions = sessionBO.getSessionsByPatient(patient.getId());

            if (enrollments.isEmpty()) {
                Label lblNoProgs = new Label("No enrolled programs found.");
                lblNoProgs.setStyle("-fx-text-fill: #95A5A6; -fx-font-style: italic; -fx-font-size: 13px;");
                vboxProgramsContainer.getChildren().add(lblNoProgs);
                return;
            }

            for (PatientTherapyProgramDTO ptp : enrollments) {
                // Filter sessions for this program
                List<TherapySessionDTO> programSessions = patientSessions.stream()
                        .filter(s -> s.getProgramId() != null && s.getProgramId().equals(ptp.getProgramId()))
                        .toList();

                long scheduledCount = programSessions.stream()
                        .filter(s -> s.getStatus() == SessionStatus.SCHEDULED)
                        .count();

                long completedCount = programSessions.stream()
                        .filter(s -> s.getStatus() == SessionStatus.COMPLETED)
                        .count();

                long cancelledCount = programSessions.stream()
                        .filter(s -> s.getStatus() == SessionStatus.CANCELLED)
                        .count();

                long noShowCount = programSessions.stream()
                        .filter(s -> s.getStatus() == SessionStatus.NO_SHOW)
                        .count();

                String status = (completedCount >= ptp.getTotalSessions() && ptp.getTotalSessions() > 0) ? "COMPLETED" : "ACTIVE";

                VBox card = new VBox(10);
                card.setStyle(
                        "-fx-background-color: #FAF8F5; " +
                                "-fx-border-color: #E5DFD8; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 12; " +
                                "-fx-background-radius: 12; " +
                                "-fx-padding: 16;"
                );

                HBox header = new HBox(12);
                header.setAlignment(Pos.CENTER_LEFT);

                Label lblName = new Label(ptp.getProgramName());
                lblName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2D3436;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblStatus = new Label(status);
                if ("COMPLETED".equals(status)) {
                    lblStatus.setStyle(
                            "-fx-background-color: #E8F5E9; " +
                                    "-fx-text-fill: #2E7D32; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-font-size: 11px; " +
                                    "-fx-padding: 4 8; " +
                                    "-fx-background-radius: 4;"
                    );
                } else {
                    lblStatus.setStyle(
                            "-fx-background-color: #E8F1F8; " +
                                    "-fx-text-fill: #4A7FA5; " +
                                    "-fx-font-weight: bold; " +
                                    "-fx-font-size: 11px; " +
                                    "-fx-padding: 4 8; " +
                                    "-fx-background-radius: 4;"
                    );
                }

                header.getChildren().addAll(lblName, spacer, lblStatus);

                FlowPane statsFlow = new FlowPane();
                statsFlow.setHgap(20);
                statsFlow.setVgap(8);

                statsFlow.getChildren().addAll(
                        createStatBlock("TOTAL SESSIONS", String.valueOf(ptp.getTotalSessions()), "#8BAF9E"),
                        createStatBlock("PAID UPFRONT", String.valueOf(ptp.getSessionsPaid()), "#4A7FA5"),
                        createStatBlock("SCHEDULED", String.valueOf(scheduledCount), "#6B9DC2"),
                        createStatBlock("COMPLETED", String.valueOf(completedCount), "#7AB88F"),
                        createStatBlock("REMAINING CREDIT", String.valueOf(ptp.getRemainingCredit()), "#E65100"),
                        createStatBlock("CANCELLED", String.valueOf(cancelledCount), "#C47171"),
                        createStatBlock("NO SHOW", String.valueOf(noShowCount), "#2C4A52")
                );

                card.getChildren().addAll(header, statsFlow);
                vboxProgramsContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createStatBlock(String title, String value, String colorHex) {
        VBox vbox = new VBox(2);
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-text-fill: #95A5A6; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        vbox.getChildren().addAll(lblTitle, lblValue);
        return vbox;
    }

    private void loadProgramDropdown(long patientId) {
        try {
            List<PatientTherapyProgramDTO> enrollments = patientBO.getPatientPrograms(patientId);
            cmbProgramDetail.setItems(FXCollections.observableArrayList(enrollments));
            cmbProgramDetail.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(PatientTherapyProgramDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getProgramName());
                }
            });
            cmbProgramDetail.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(PatientTherapyProgramDTO item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getProgramName());
                }
            });
            cmbProgramDetail.setValue(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProgramSessions(long patientId, long programId) {
        try {
            List<TherapySessionDTO> patientSessions = sessionBO.getSessionsByPatient(patientId);
            List<TherapySessionDTO> filtered = patientSessions.stream()
                    .filter(s -> s.getProgramId() != null && s.getProgramId().equals(programId))
                    .toList();

            tblSessions.setItems(FXCollections.observableArrayList(filtered));
            tblSessions.setVisible(true);
            tblSessions.setManaged(true);
            tblSessions.getSelectionModel().clearSelection();
            vboxSessionNotes.setVisible(false);
            vboxSessionNotes.setManaged(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSessionDetails(TherapySessionDTO session) {
        lblSessionId.setText(session.getStringId());
        lblSessionSeq.setText(session.getSequenceNumber() != null ? String.valueOf(session.getSequenceNumber()) : "N/A");
        lblSessionTherapist.setText(session.getTherapistName() != null ? session.getTherapistName() : "N/A");
        lblSessionDate.setText(session.getSessionDate() != null ? session.getSessionDate().toString() : "N/A");
        lblSessionTime.setText(session.getSessionTime() != null
                ? session.getSessionTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A");
        lblSessionPayment.setText(session.getPaymentStatus() != null ? session.getPaymentStatus().name() : "PENDING");

        if (session.getPaymentStatus() == lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionPaymentStatus.PAID) {
            lblSessionPayment.setStyle(
                    "-fx-background-color: #E8F5E9; " +
                            "-fx-text-fill: #2E7D32; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 11px; " +
                            "-fx-padding: 4 8; " +
                            "-fx-background-radius: 4;"
            );
        } else {
            lblSessionPayment.setStyle(
                    "-fx-background-color: #FFF3E0; " +
                            "-fx-text-fill: #E65100; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 11px; " +
                            "-fx-padding: 4 8; " +
                            "-fx-background-radius: 4;"
            );
        }

        txtSessionNotes.setText(session.getNotes() != null ? session.getNotes() : "");

        vboxSessionNotes.setVisible(true);
        vboxSessionNotes.setManaged(true);
    }

    private void clearPatientDetails() {
        lblPatientId.setText("");
        lblPatientName.setText("");
        lblPatientEmail.setText("");
        lblPatientPhone.setText("");
        lblPatientAddress.setText("");
        lblPatientRegistered.setText("");
        txtInterviewNote.clear();

        vboxPatientDetails.setVisible(false);
        vboxPatientDetails.setManaged(false);

        vboxProgramsContainer.getChildren().clear();
        vboxProgramsOverview.setVisible(false);
        vboxProgramsOverview.setManaged(false);

        cmbProgramDetail.setItems(FXCollections.observableArrayList());
        vboxSessionHistory.setVisible(false);
        vboxSessionHistory.setManaged(false);

        tblSessions.setItems(FXCollections.observableArrayList());
        tblSessions.setVisible(false);
        tblSessions.setManaged(false);

        vboxSessionNotes.setVisible(false);
        vboxSessionNotes.setManaged(false);
    }
}
