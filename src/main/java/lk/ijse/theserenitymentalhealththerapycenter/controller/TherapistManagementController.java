package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapistBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Therapist;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TherapistManagementController implements Initializable {

    @FXML private TextField txtTherapistName;
    @FXML private TextField txtTherapistSpecialty;
    @FXML private TextField txtTherapistPhone;
    @FXML private TextField txtTherapistEmail;
    @FXML private ComboBox<Therapist.Status> cmbTherapistStatus;
    @FXML private TextField txtSearchTherapist;

    @FXML private TableView<Therapist> tblTherapists;
    @FXML private TableColumn<Therapist, Long> colTherapistId;
    @FXML private TableColumn<Therapist, String> colTherapistName;
    @FXML private TableColumn<Therapist, String> colTherapistSpecialty;
    @FXML private TableColumn<Therapist, String> colTherapistPhone;
    @FXML private TableColumn<Therapist, String> colTherapistEmail;
    @FXML private TableColumn<Therapist, String> colTherapistStatus;

    private final TherapistBOImpl therapistService = new TherapistBOImpl();
    private FilteredList<Therapist> filteredTherapists;
    private Therapist selectedTherapist;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbTherapistStatus.setItems(FXCollections.observableArrayList(Therapist.Status.values()));
        cmbTherapistStatus.setValue(Therapist.Status.ACTIVE);

        setupTable();
        loadData();
        setupSearch();

        tblTherapists.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTherapist = newVal;
                populateForm(newVal);
            }
        });
    }

    private void setupTable() {
        colTherapistId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colTherapistName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colTherapistSpecialty.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSpecialty()));
        colTherapistPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colTherapistEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colTherapistStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""));
    }

    private void loadData() {
        try {
            List<Therapist> list = therapistService.getAllTherapists();
            filteredTherapists = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
            tblTherapists.setItems(filteredTherapists);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load therapists: " + e.getMessage());
        }
    }

    private void setupSearch() {
        txtSearchTherapist.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredTherapists != null) {
                filteredTherapists.setPredicate(t -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String lower = newVal.toLowerCase();
                    return (t.getName() != null && t.getName().toLowerCase().contains(lower))
                            || (t.getSpecialty() != null && t.getSpecialty().toLowerCase().contains(lower));
                });
            }
        });
    }

    private void populateForm(Therapist t) {
        txtTherapistName.setText(t.getName());
        txtTherapistSpecialty.setText(t.getSpecialty());
        txtTherapistPhone.setText(t.getPhone());
        txtTherapistEmail.setText(t.getEmail());
        cmbTherapistStatus.setValue(t.getStatus());
    }

    @FXML
    void handleSaveTherapist(ActionEvent event) {
        try {
            Therapist t = new Therapist();
            t.setName(txtTherapistName.getText());
            t.setSpecialty(txtTherapistSpecialty.getText());
            t.setPhone(txtTherapistPhone.getText());
            t.setEmail(txtTherapistEmail.getText());
            t.setStatus(cmbTherapistStatus.getValue());
            therapistService.saveTherapist(t);
            AlertUtil.showInfo("Success", "Therapist saved successfully.");
            handleClearTherapist(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleUpdateTherapist(ActionEvent event) {
        if (selectedTherapist == null) {
            AlertUtil.showWarning("Warning", "Please select a therapist to update.");
            return;
        }
        try {
            selectedTherapist.setName(txtTherapistName.getText());
            selectedTherapist.setSpecialty(txtTherapistSpecialty.getText());
            selectedTherapist.setPhone(txtTherapistPhone.getText());
            selectedTherapist.setEmail(txtTherapistEmail.getText());
            selectedTherapist.setStatus(cmbTherapistStatus.getValue());
            therapistService.updateTherapist(selectedTherapist);
            AlertUtil.showInfo("Success", "Therapist updated successfully.");
            handleClearTherapist(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleDeleteTherapist(ActionEvent event) {
        Therapist t = tblTherapists.getSelectionModel().getSelectedItem();
        if (t == null) {
            AlertUtil.showWarning("Warning", "Please select a therapist to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete therapist \"" + t.getName() + "\"?")) {
            try {
                therapistService.deleteTherapist(t);
                AlertUtil.showInfo("Deleted", "Therapist deleted.");
                handleClearTherapist(event);
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleClearTherapist(ActionEvent event) {
        txtTherapistName.clear();
        txtTherapistSpecialty.clear();
        txtTherapistPhone.clear();
        txtTherapistEmail.clear();
        cmbTherapistStatus.setValue(Therapist.Status.ACTIVE);
        selectedTherapist = null;
        tblTherapists.getSelectionModel().clearSelection();
    }
}
