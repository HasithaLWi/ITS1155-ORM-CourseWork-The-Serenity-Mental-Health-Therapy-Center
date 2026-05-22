package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapyProgramBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.SessionStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.enums.TherapistStatus;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.TherapistTM;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapySession;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class TherapistManagementController implements Initializable {

    @FXML private TextField txtTherapistName;
    @FXML private TextField txtTherapistSpecialty;
    @FXML private TextField txtTherapistPhone;
    @FXML private TextField txtTherapistEmail;
    @FXML private ComboBox<TherapistStatus> cmbTherapistStatus;
    @FXML private TextField txtSearchTherapist;
    @FXML private FlowPane flowPrograms;

    @FXML private TableView<TherapistTM> tblTherapists;
    @FXML private TableColumn<TherapistTM, String> colTherapistId;
    @FXML private TableColumn<TherapistTM, String> colTherapistName;
    @FXML private TableColumn<TherapistTM, String> colTherapistSpecialty;
    @FXML private TableColumn<TherapistTM, String> colTherapistPhone;
    @FXML private TableColumn<TherapistTM, String> colTherapistEmail;
    @FXML private TableColumn<TherapistTM, String> colTherapistStatus;

    private final TherapistBO therapistService = (TherapistBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPIST);
    private final TherapyProgramBO programService = (TherapyProgramBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_PROGRAM);
    private  final TherapySessionBO therapySessionBO = (TherapySessionBO) BOFactory.getInstance().getBO(BOFactory.BOType.THERAPY_SESSION);
    private FilteredList<TherapistTM> filteredTherapists;
    private TherapistTM selectedTherapist;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbTherapistStatus.setItems(FXCollections.observableArrayList(TherapistStatus.values()));
        cmbTherapistStatus.setValue(TherapistStatus.ACTIVE);

        setupTable();
        loadProgramsCheckboxList();
        loadData();
        setupSearch();

        tblTherapists.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedTherapist = newVal;
                populateForm(newVal);
            }
        });
    }

    private void loadProgramsCheckboxList() {
        try {
            flowPrograms.getChildren().clear();
            List<TherapyProgramDTO> programs = programService.getAllPrograms();
            for (TherapyProgramDTO p : programs) {
                CheckBox cb = new CheckBox(p.getName());
                cb.setUserData(p.getId());
                cb.setStyle("-fx-text-fill: #2D3436; -fx-font-size: 13px; -fx-cursor: hand;");
                flowPrograms.getChildren().add(cb);
            }
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load therapy programs: " + e.getMessage());
        }
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
                    dto.getStatus(),
                    dto.getProgramIds()
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
                            || (t.getSpecialty() != null && t.getSpecialty().toLowerCase().contains(lower))
                            || (t.getId() != null && t.getId().toLowerCase().contains(lower));

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

        List<Long> assignedIds = t.getProgramIds();
        for (javafx.scene.Node node : flowPrograms.getChildren()) {
            if (node instanceof CheckBox cb) {
                Long progId = (Long) cb.getUserData();
                cb.setSelected(assignedIds != null && assignedIds.contains(progId));
            }
        }
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

            List<Long> progIds = new ArrayList<>();
            for (javafx.scene.Node node : flowPrograms.getChildren()) {
                if (node instanceof CheckBox cb && cb.isSelected()) {
                    progIds.add((Long) cb.getUserData());
                }
            }
            dto.setProgramIds(progIds);

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

            List<Long> progIds = new ArrayList<>();
            for (Node node : flowPrograms.getChildren()) {
                if (node instanceof CheckBox cb && cb.isSelected()) {
                    progIds.add((Long) cb.getUserData());
                }
            }
            dto.setProgramIds(progIds);

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
                String rawId = t.getId();
                long id = 0;
                if (rawId != null && rawId.startsWith("T")) {
                    id = Long.parseLong(rawId.substring(1));
                }

                // Check if therapist has any active/scheduled/pending sessions
                TherapySessionDTO session = therapySessionBO.getAllSessionDTOs().stream()
                        .filter(s -> s.getTherapistId() != null && s.getTherapistId().equals(t.getLongId()))
                        .filter(s -> !s.getStatus().equals(SessionStatus.CANCELLED))
                        .findFirst().orElse(null);
                if (session != null) {
                    AlertUtil.showError("Error", "Cannot delete therapist with active or scheduled sessions.");
                    return;
                }else {
                    // If no active sessions, we can proceed to delete
                    therapistService.deleteTherapist(id);
                    AlertUtil.showInfo("Success", "Therapist deleted successfully.");
                    handleClearTherapist(event);
                    loadData();
                }

            } catch (Exception e) {
                if(e instanceof SecurityException){
                    AlertUtil.showWarning("Error", e.getMessage());
                    return;
                }
                AlertUtil.showError("Error", "Failed to delete therapist: ");
                e.printStackTrace();
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

        for (javafx.scene.Node node : flowPrograms.getChildren()) {
            if (node instanceof CheckBox cb) {
                cb.setSelected(false);
            }
        }
    }
}
