package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientTherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.PaymentMethod;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.EnrolledProgramTM;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PatientTM;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDeleteSummaryDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PatientListController implements Initializable {

    @FXML
    private TextField txtEditName;
    @FXML
    private TextField txtEditEmail;
    @FXML
    private TextField txtEditPhone;
    @FXML
    private TextField txtEditAddress;
    @FXML
    private TextArea txtEditInterviewNote;
    @FXML
    private TextField txtSearchPatient;


    @FXML
    private TableView<PatientTM> tblPatients;
    @FXML
    private TableColumn<PatientTM, String> colPatientId;
    @FXML
    private TableColumn<PatientTM, String> colPatientName;
    @FXML
    private TableColumn<PatientTM, String> colPatientEmail;
    @FXML
    private TableColumn<PatientTM, String> colPatientPhone;
    @FXML
    private TableColumn<PatientTM, String> colPatientAddress;
    @FXML
    private TableColumn<PatientTM, String> colPatientDate;


    @FXML
    private VBox vboxEnrolledPrograms;
    @FXML
    private TableView<EnrolledProgramTM> tblEnrolledPrograms;
    @FXML
    private TableColumn<EnrolledProgramTM, String> colEnrProgramName;
    @FXML
    private TableColumn<EnrolledProgramTM, Integer> colEnrTotalSessions;
    @FXML
    private TableColumn<EnrolledProgramTM, Integer> colEnrUpfrontPaid;
    @FXML
    private TableColumn<EnrolledProgramTM, Integer> colEnrUsed;
    @FXML
    private TableColumn<EnrolledProgramTM, Integer> colEnrRemaining;
    @FXML
    private TableColumn<EnrolledProgramTM, Integer> colEnrCompleted;
    @FXML
    private TableColumn<EnrolledProgramTM, String> colEnrStatus;


    @FXML
    private ComboBox<TherapyProgramDTO> cmbNewProgram;
    @FXML
    private ComboBox<Integer> cmbNewProgramSessions;
    @FXML
    private Label lblNewProgramCost;
    @FXML
    private ComboBox<PaymentMethod> cmbNewProgramPayMethod;

    private final PatientBO patientService = (PatientBO) BOFactory.getInstance().getBO(BOFactory.BOType.PATIENT);
    private final TherapyProgramBO programService = (TherapyProgramBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_PROGRAM);
    private final TherapySessionBO sessionService = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);
    private final PaymentBO paymentService = (PaymentBO) BOFactory.getInstance().getBO(BOFactory.BOType.PAYMENT);

    private FilteredList<PatientTM> filteredPatients;
    private PatientTM selectedPatient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupEnrolledProgramsTable();
        loadData();
        setupSearch();


        cmbNewProgramPayMethod.setItems(FXCollections.observableArrayList(PaymentMethod.values()));


        setComboCellFactory(cmbNewProgram, TherapyProgramDTO::getName);


        cmbNewProgram.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int total = newVal.getTotalSessions() != null ? newVal.getTotalSessions() : 1;
                ObservableList<Integer> opts = FXCollections.observableArrayList();
                for (int i = 0; i <= total; i++) opts.add(i);
                cmbNewProgramSessions.setItems(opts);
                cmbNewProgramSessions.setValue(total);
                updateNewProgramCost();
            } else {
                cmbNewProgramSessions.setItems(FXCollections.observableArrayList());
                lblNewProgramCost.setText("0 LKR");
            }
        });


        cmbNewProgramSessions.valueProperty().addListener((obs, o, n) -> updateNewProgramCost());


        tblPatients.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                selectedPatient = n;
                populateForm(n);
                loadEnrolledPrograms(n);
                loadAvailablePrograms(n);
                vboxEnrolledPrograms.setVisible(true);
                vboxEnrolledPrograms.setManaged(true);
            } else {
                vboxEnrolledPrograms.setVisible(false);
                vboxEnrolledPrograms.setManaged(false);
            }
        });
    }


    private void setupTable() {
        colPatientId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPatientName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPatientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPatientPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colPatientAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colPatientDate.setCellValueFactory(new PropertyValueFactory<>("registeredDate"));
    }

    private void setupEnrolledProgramsTable() {
        colEnrProgramName.setCellValueFactory(new PropertyValueFactory<>("programName"));
        colEnrTotalSessions.setCellValueFactory(new PropertyValueFactory<>("totalSessions"));
        colEnrUpfrontPaid.setCellValueFactory(new PropertyValueFactory<>("upfrontPaid"));
        colEnrUsed.setCellValueFactory(new PropertyValueFactory<>("sessionsUsed"));
        colEnrRemaining.setCellValueFactory(new PropertyValueFactory<>("creditRemaining"));
        colEnrCompleted.setCellValueFactory(new PropertyValueFactory<>("completedSessions"));
        colEnrStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colEnrStatus.setCellFactory(col -> new TableCell<>() {
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
                    } else if ("ACTIVE".equals(item)) {
                        setStyle("-fx-text-fill: #4A7FA5; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2D3436;");
                    }
                }
            }
        });
    }


    private void loadData() {
        try {
            List<PatientDTO> dtos = patientService.getAllPatients();
            List<PatientTM> tms = dtos.stream().map(dto -> {
                PatientTM tm = new PatientTM();
                tm.setId(dto.getStringId());
                tm.setName(dto.getName());
                tm.setEmail(dto.getEmail());
                tm.setPhone(dto.getPhone());
                tm.setAddress(dto.getAddress());
                tm.setRegisteredDate(dto.getRegisteredDate() != null
                        ? dto.getRegisteredDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
                return tm;
            }).toList();
            filteredPatients = new FilteredList<>(FXCollections.observableArrayList(tms), p -> true);
            tblPatients.setItems(filteredPatients);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load patients: " + e.getMessage());
        }
    }

    private void setupSearch() {
        txtSearchPatient.textProperty().addListener((obs, o, n) -> {
            if (filteredPatients != null) {
                filteredPatients.setPredicate(p -> {
                    if (n == null || n.isEmpty()) return true;
                    String lower = n.toLowerCase();
                    return (p.getName() != null && p.getName().toLowerCase().contains(lower))
                            || (p.getPhone() != null && p.getPhone().contains(lower))
                            || (p.getId() != null && p.getId().toLowerCase().contains(lower))
                            || (p.getEmail() != null && p.getEmail().toLowerCase().contains(lower));
                });
            }
        });
    }


    private void loadEnrolledPrograms(PatientTM patient) {
        try {
            long patientId = parsePatientId(patient.getId());
            List<PatientTherapyProgramDTO> enrollments = patientService.getPatientPrograms(patientId);
            ObservableList<EnrolledProgramTM> rows = FXCollections.observableArrayList();

            for (PatientTherapyProgramDTO ptp : enrollments) {
                int totalSessions = ptp.getTotalSessions();
                long completed = sessionService.countCompletedByPatientAndProgram(patientId, ptp.getProgramId());

                String status;
                if (completed >= totalSessions && totalSessions > 0) {
                    status = "COMPLETED";
                } else {
                    status = "ACTIVE";
                }

                rows.add(new EnrolledProgramTM(
                        ptp.getProgramName(),
                        totalSessions,
                        ptp.getSessionsPaid(),
                        ptp.getSessionsUsed(),
                        ptp.getRemainingCredit(),
                        (int) completed,
                        status
                ));
            }
            tblEnrolledPrograms.setItems(rows);
        } catch (Exception e) {
            System.err.println("Error loading enrolled programs: " + e.getMessage());
        }
    }


    private void loadAvailablePrograms(PatientTM patient) {
        try {
            long patientId = parsePatientId(patient.getId());
            List<TherapyProgramDTO> allPrograms = programService.getAllPrograms();
            List<PatientTherapyProgramDTO> enrollments = patientService.getPatientPrograms(patientId);

            List<TherapyProgramDTO> available = allPrograms.stream().filter(prog -> {
                PatientTherapyProgramDTO existing = enrollments.stream()
                        .filter(e -> e.getProgramId().equals(prog.getId()))
                        .findFirst().orElse(null);

                if (existing == null) {

                    return true;
                }

                int totalSessions = prog.getTotalSessions() != null ? prog.getTotalSessions() : 0;
                if (totalSessions > 0) {
                    long completed = sessionService.countCompletedByPatientAndProgram(patientId, prog.getId());
                    return completed >= totalSessions;
                }
                return false;
            }).collect(Collectors.toList());

            cmbNewProgram.setItems(FXCollections.observableArrayList(available));
            cmbNewProgram.setValue(null);
            cmbNewProgramSessions.setItems(FXCollections.observableArrayList());
            lblNewProgramCost.setText("0 LKR");
        } catch (Exception e) {
            System.err.println("Error loading available programs: " + e.getMessage());
        }
    }


    private void populateForm(PatientTM p) {
        txtEditName.setText(p.getName());
        txtEditEmail.setText(p.getEmail());
        txtEditPhone.setText(p.getPhone());
        txtEditAddress.setText(p.getAddress());

        //clear
        txtEditInterviewNote.clear();
    }

    private void updateNewProgramCost() {
        TherapyProgramDTO prog = cmbNewProgram.getValue();
        Integer sessions = cmbNewProgramSessions.getValue();
        if (prog == null || sessions == null || sessions == 0) {
            lblNewProgramCost.setText("0 LKR");
            return;
        }
        BigDecimal cost;
        if (prog.getSessionFee() != null) {
            cost = prog.getSessionFee().multiply(new BigDecimal(sessions));
        } else if (prog.getFee() != null) {
            int total = prog.getTotalSessions() != null ? prog.getTotalSessions() : 1;
            BigDecimal perSession = prog.getFee().divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
            cost = perSession.multiply(new BigDecimal(sessions));
        } else {
            cost = BigDecimal.ZERO;
        }
        lblNewProgramCost.setText(cost.toPlainString() + " LKR");
    }


    @FXML
    void handleUpdatePatient(ActionEvent event) {
        if (selectedPatient == null) {
            AlertUtil.showWarning("Warning", "Select a patient first.");
            return;
        }
        try {
            long patientId = parsePatientId(selectedPatient.getId());
            PatientDTO dto = new PatientDTO();
            dto.setId(patientId);
            dto.setName(txtEditName.getText());
            dto.setEmail(txtEditEmail.getText());
            dto.setPhone(txtEditPhone.getText());
            dto.setAddress(txtEditAddress.getText());
            dto.setInterviewNote(txtEditInterviewNote.getText());
            patientService.updatePatient(dto);
            AlertUtil.showInfo("Success", "Patient updated.");
            handleClearEditForm(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleDeletePatient(ActionEvent event) {
        PatientTM p = tblPatients.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showWarning("Warning", "Select a patient to delete.");
            return;
        }

        try {
            long patientId = parsePatientId(p.getId());
            PatientDeleteSummaryDTO summary = patientService.getPatientDeleteSummary(patientId);

            String warningMessage = "Are you sure you want to delete patient \"" + p.getName() + "\"?\n\n"
                    + "This patient has:\n"
                    + " - Enrolled Programs: " + summary.getProgramCount() + "\n"
                    + " - Sessions (Scheduled/Completed): " + summary.getSessionCount() + "\n"
                    + " - Payments recorded: " + summary.getPaymentCount() + " (Total Paid: Rs. " + String.format("%.2f", summary.getTotalPaidAmount()) + ")\n\n"
                    + "WARNING: Deleting this record will permanently delete ALL associated session records, program enrollments, and payment details! This action cannot be undone.";

            if (AlertUtil.showConfirmation("Confirm Patient Deletion", warningMessage)) {
                patientService.deletePatient(patientId);
                handleClearEditForm(event);
                loadData();
                AlertUtil.showInfo("Success", "Patient and all associated records deleted successfully.");
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleEnrollNewProgram(ActionEvent event) {
        if (selectedPatient == null) {
            AlertUtil.showWarning("Warning", "Select a patient first.");
            return;
        }

        TherapyProgramDTO program = cmbNewProgram.getValue();
        if (program == null) {
            AlertUtil.showWarning("Warning", "Please select a program to enroll in.");
            return;
        }

        Integer sessionsToPay = cmbNewProgramSessions.getValue();
        if (sessionsToPay == null) sessionsToPay = 0;

        // Calculate cost
        BigDecimal cost = BigDecimal.ZERO;
        if (sessionsToPay > 0) {
            if (program.getSessionFee() != null) {
                cost = program.getSessionFee().multiply(new BigDecimal(sessionsToPay));
            } else if (program.getFee() != null) {
                int total = program.getTotalSessions() != null ? program.getTotalSessions() : 1;
                BigDecimal perSession = program.getFee().divide(new BigDecimal(total), 2, RoundingMode.HALF_UP);
                cost = perSession.multiply(new BigDecimal(sessionsToPay));
            }
        }


        PaymentMethod method = cmbNewProgramPayMethod.getValue();
        if (cost.compareTo(BigDecimal.ZERO) > 0 && method == null) {
            AlertUtil.showWarning("Warning", "Please select a payment method.");
            return;
        }

        try {
            long patientId = parsePatientId(selectedPatient.getId());


            patientService.enrollPatientInProgram(patientId, program.getId(), sessionsToPay);


            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setPatientId(patientId);
                paymentDTO.setAmount(cost);
                paymentDTO.setMethod(method);
                paymentDTO.setDescription("Upfront payment for " + sessionsToPay + " sessions of " + program.getName());
                paymentService.saveRegistrationPayment(paymentDTO);
            }

            AlertUtil.showInfo("Success", "Patient enrolled in " + program.getName()
                    + (sessionsToPay > 0 ? " with " + sessionsToPay + " sessions paid upfront." : "."));

            // Refresh
            loadEnrolledPrograms(selectedPatient);
            loadAvailablePrograms(selectedPatient);
            cmbNewProgramPayMethod.setValue(null);

        } catch (Exception e) {
            AlertUtil.showError("Error", "Enrollment failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearEditForm(ActionEvent event) {
        txtEditName.clear();
        txtEditEmail.clear();
        txtEditPhone.clear();
        txtEditAddress.clear();
        txtEditInterviewNote.clear();
        selectedPatient = null;
        tblPatients.getSelectionModel().clearSelection();
        vboxEnrolledPrograms.setVisible(false);
        vboxEnrolledPrograms.setManaged(false);
    }


    private <T> void setComboCellFactory(ComboBox<T> combo, Function<T, String> nameFunc) {
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameFunc.apply(item));
            }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : nameFunc.apply(item));
            }
        });
    }


    private long parsePatientId(String formattedId) {
        if (formattedId != null && formattedId.startsWith("P")) {
            return Long.parseLong(formattedId.substring(1));
        }
        return 0;
    }

}
