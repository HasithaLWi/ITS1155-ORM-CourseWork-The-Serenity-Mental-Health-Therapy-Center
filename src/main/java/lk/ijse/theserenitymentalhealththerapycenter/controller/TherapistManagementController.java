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
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.TherapistTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TherapistManagementController implements Initializable {

    @FXML private TextField txtTherapistName;
    @FXML private TextField txtTherapistSpecialty;
    @FXML private TextField txtTherapistPhone;
    @FXML private TextField txtTherapistEmail;
    @FXML private ComboBox<TherapistStatus> cmbTherapistStatus;
    @FXML private TextField txtSearchTherapist;

    @FXML private TableView<TherapistTM> tblTherapists;
    @FXML private TableColumn<TherapistTM, String> colTherapistId;
    @FXML private TableColumn<TherapistTM, String> colTherapistName;
    @FXML private TableColumn<TherapistTM, String> colTherapistSpecialty;
    @FXML private TableColumn<TherapistTM, String> colTherapistPhone;
    @FXML private TableColumn<TherapistTM, String> colTherapistEmail;
    @FXML private TableColumn<TherapistTM, String> colTherapistStatus;

    private final TherapistBOImpl therapistService = new TherapistBOImpl();
    private FilteredList<TherapistTM> filteredTherapists;
    private TherapistTM selectedTherapist;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbTherapistStatus.setItems(FXCollections.observableArrayList(TherapistStatus.values()));
        cmbTherapistStatus.setValue(TherapistStatus.ACTIVE);

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
        colTherapistId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        colTherapistName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colTherapistSpecialty.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSpecialty()));
        colTherapistPhone.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPhone()));
        colTherapistEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colTherapistStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus() != null ? d.getValue().getStatus().name() : ""));
    }

    private void loadData() {
        try {
            List<TherapistDTO> dtos = therapistService.getAllTherapists();
            List<TherapistTM> tms = dtos.stream().map(dto -> new TherapistTM(
                    dto.getStringId(),
                    dto.getName(),
                    dto.getSpecialty(),
                    dto.getPhone(),
                    dto.getEmail(),
                    dto.getStatus()
            )).toList();
            filteredTherapists = new FilteredList<>(FXCollections.observableArrayList(tms), p -> true);
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

    private void populateForm(TherapistTM t) {
        txtTherapistName.setText(t.getName());
        txtTherapistSpecialty.setText(t.getSpecialty());
        txtTherapistPhone.setText(t.getPhone());
        txtTherapistEmail.setText(t.getEmail());
        cmbTherapistStatus.setValue(t.getStatus());
    }

    @FXML
    void handleSaveTherapist(ActionEvent event) {
        try {
            TherapistDTO dto = new TherapistDTO();
            dto.setName(txtTherapistName.getText());
            dto.setSpecialty(txtTherapistSpecialty.getText());
            dto.setPhone(txtTherapistPhone.getText());
            dto.setEmail(txtTherapistEmail.getText());
            dto.setStatus(cmbTherapistStatus.getValue());
            therapistService.saveTherapist(dto);
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
            TherapistDTO dto = new TherapistDTO();
            // Parse the Long ID from the formatted String ID (e.g., "T001" -> 1)
            String rawId = selectedTherapist.getId();
            if (rawId != null && rawId.startsWith("T")) {
                dto.setId(Long.parseLong(rawId.substring(1)));
            }
            dto.setName(txtTherapistName.getText());
            dto.setSpecialty(txtTherapistSpecialty.getText());
            dto.setPhone(txtTherapistPhone.getText());
            dto.setEmail(txtTherapistEmail.getText());
            dto.setStatus(cmbTherapistStatus.getValue());
            therapistService.updateTherapist(dto);
            AlertUtil.showInfo("Success", "Therapist updated successfully.");
            handleClearTherapist(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleDeleteTherapist(ActionEvent event) {
        TherapistTM t = tblTherapists.getSelectionModel().getSelectedItem();
        if (t == null) {
            AlertUtil.showWarning("Warning", "Please select a therapist to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete therapist \"" + t.getName() + "\"?")) {
            try {
                // Parse the Long ID from the formatted String ID
                String rawId = t.getId();
                long id = 0;
                if (rawId != null && rawId.startsWith("T")) {
                    id = Long.parseLong(rawId.substring(1));
                }
                therapistService.deleteTherapist(id);
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
        cmbTherapistStatus.setValue(TherapistStatus.ACTIVE);
        selectedTherapist = null;
        tblTherapists.getSelectionModel().clearSelection();
    }
}
