package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.PatientService;
import lk.ijse.theserenitymentalhealththerapycenter.bo.TherapyProgramService;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class PatientRegistrationController implements Initializable {

    @FXML private TextField txtPatientName;
    @FXML private TextField txtPatientEmail;
    @FXML private TextField txtPatientPhone;
    @FXML private TextField txtPatientAddress;
    @FXML private ComboBox<TherapyProgram> cmbPatientProgram;
    @FXML private Label lblRegMessage;

    private final PatientService patientService = new PatientService();
    private final TherapyProgramService programService = new TherapyProgramService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            cmbPatientProgram.setItems(FXCollections.observableArrayList(programService.getAllPrograms()));
        } catch (Exception e) { System.err.println("Error loading programs: " + e.getMessage()); }

        cmbPatientProgram.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cmbPatientProgram.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
    }

    @FXML
    void handleRegisterPatient(ActionEvent event) {
        try {
            Patient p = new Patient();
            p.setName(txtPatientName.getText());
            p.setEmail(txtPatientEmail.getText());
            p.setPhone(txtPatientPhone.getText());
            p.setAddress(txtPatientAddress.getText());

            TherapyProgram program = cmbPatientProgram.getValue();
            if (program != null) {
                p.getPrograms().add(program);
            }

            patientService.registerPatient(p);
            lblRegMessage.setText("Patient registered successfully!");
            lblRegMessage.setStyle("-fx-text-fill: #7AB88F; -fx-font-size: 12px;");
            handleClearPatientForm(event);
        } catch (Exception e) {
            lblRegMessage.setText(e.getMessage());
            lblRegMessage.setStyle("-fx-text-fill: #C47171; -fx-font-size: 12px;");
        }
    }

    @FXML
    void handleClearPatientForm(ActionEvent event) {
        txtPatientName.clear();
        txtPatientEmail.clear();
        txtPatientPhone.clear();
        txtPatientAddress.clear();
        cmbPatientProgram.setValue(null);
    }
}
