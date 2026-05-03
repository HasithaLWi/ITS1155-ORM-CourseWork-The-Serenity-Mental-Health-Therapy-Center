package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.PatientBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class PatientListController implements Initializable {

    @FXML private TextField txtEditName;
    @FXML private TextField txtEditEmail;
    @FXML private TextField txtEditPhone;
    @FXML private TextField txtEditAddress;
    @FXML private TextField txtSearchPatient;

    @FXML private TableView<Patient> tblPatients;
    @FXML private TableColumn<Patient, Long> colPatientId;
    @FXML private TableColumn<Patient, String> colPatientName;
    @FXML private TableColumn<Patient, String> colPatientEmail;
    @FXML private TableColumn<Patient, String> colPatientPhone;
    @FXML private TableColumn<Patient, String> colPatientAddress;
    @FXML private TableColumn<Patient, String> colPatientDate;

    private final PatientBOImpl patientService = new PatientBOImpl();
    private FilteredList<Patient> filteredPatients;
    private Patient selectedPatient;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadData();
        setupSearch();
        tblPatients.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { selectedPatient = n; populateForm(n); }
        });
    }

    private void setupTable() {
        colPatientId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colPatientName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colPatientEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPatientPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colPatientAddress.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAddress()));
        colPatientDate.setCellValueFactory(d -> new SimpleStringProperty(
            d.getValue().getRegisteredDate() != null
                ? d.getValue().getRegisteredDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : ""));
    }

    private void loadData() {
        try {
            filteredPatients = new FilteredList<>(FXCollections.observableArrayList(patientService.getAllPatients()), p -> true);
            tblPatients.setItems(filteredPatients);
        } catch (Exception e) { AlertUtil.showError("Error", "Failed to load patients: " + e.getMessage()); }
    }

    private void setupSearch() {
        txtSearchPatient.textProperty().addListener((obs, o, n) -> {
            if (filteredPatients != null) {
                filteredPatients.setPredicate(p -> {
                    if (n == null || n.isEmpty()) return true;
                    String lower = n.toLowerCase();
                    return (p.getName() != null && p.getName().toLowerCase().contains(lower))
                        || (p.getPhone() != null && p.getPhone().contains(lower));
                });
            }
        });
    }

    private void populateForm(Patient p) {
        txtEditName.setText(p.getName());
        txtEditEmail.setText(p.getEmail());
        txtEditPhone.setText(p.getPhone());
        txtEditAddress.setText(p.getAddress());
    }

    @FXML void handleUpdatePatient(ActionEvent event) {
        if (selectedPatient == null) { AlertUtil.showWarning("Warning", "Select a patient first."); return; }
        try {
            selectedPatient.setName(txtEditName.getText());
            selectedPatient.setEmail(txtEditEmail.getText());
            selectedPatient.setPhone(txtEditPhone.getText());
            selectedPatient.setAddress(txtEditAddress.getText());
            patientService.updatePatient(selectedPatient);
            AlertUtil.showInfo("Success", "Patient updated.");
            handleClearEditForm(event);
            loadData();
        } catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
    }

    @FXML void handleDeletePatient(ActionEvent event) {
        Patient p = tblPatients.getSelectionModel().getSelectedItem();
        if (p == null) { AlertUtil.showWarning("Warning", "Select a patient to delete."); return; }
        if (AlertUtil.showConfirmation("Confirm", "Delete patient \"" + p.getName() + "\"?")) {
            try { patientService.deletePatient(p); handleClearEditForm(event); loadData(); }
            catch (Exception e) { AlertUtil.showError("Error", e.getMessage()); }
        }
    }

    @FXML void handleClearEditForm(ActionEvent event) {
        txtEditName.clear(); txtEditEmail.clear();
        txtEditPhone.clear(); txtEditAddress.clear();
        selectedPatient = null;
        tblPatients.getSelectionModel().clearSelection();
    }
}
