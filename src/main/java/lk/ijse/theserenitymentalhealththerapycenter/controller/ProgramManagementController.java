package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.impl.TherapyProgramBOImpl;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.TherapyProgramTM;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProgramManagementController implements Initializable {

    @FXML private TextField txtProgramName;
    @FXML private TextField txtProgramDuration;
    @FXML private TextField txtProgramFee;
    @FXML private TextField txtTotalSessions;
    @FXML private TextArea txtProgramDescription;
    @FXML private TextField txtSearchProgram;

    @FXML private TableView<TherapyProgramTM> tblPrograms;
    @FXML private TableColumn<TherapyProgramTM, String> colProgramId;
    @FXML private TableColumn<TherapyProgramTM, String> colProgramName;
    @FXML private TableColumn<TherapyProgramTM, String> colProgramDuration;
    @FXML private TableColumn<TherapyProgramTM, BigDecimal> colProgramFee;
    @FXML private TableColumn<TherapyProgramTM, Integer> colTotalSessions;
    @FXML private TableColumn<TherapyProgramTM, String> colProgramDescription;

    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
    private FilteredList<TherapyProgramTM> filteredPrograms;
    private TherapyProgramTM selectedProgram;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadData();
        setupSearch();

        tblPrograms.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProgram = newVal;
                populateForm(newVal);
            }
        });
    }

    private void setupTable() {
        colProgramId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStringId()));
        colProgramName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colProgramDuration.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuration()));
        colProgramFee.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFee()));
        colTotalSessions.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTotalSessions()));
        colProgramDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
    }

    private void loadData() {
        try {
            List<TherapyProgramTM> list = programService.getAllPrograms().stream().map(dto -> new TherapyProgramTM(
                    dto.getStringId(), dto.getName(), dto.getDuration(), dto.getFee(), dto.getTotalSessions(), dto.getSessionFee(), dto.getDescription()
            )).toList();

            filteredPrograms = new FilteredList<>(FXCollections.observableArrayList(list), p -> true);
            tblPrograms.setItems(filteredPrograms);
        } catch (Exception e) {
            AlertUtil.showError("Error", "Failed to load programs: " + e.getMessage());
        }
    }

    private void setupSearch() {
        txtSearchProgram.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredPrograms != null) {
                filteredPrograms.setPredicate(p -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    return p.getName() != null && p.getName().toLowerCase().contains(newVal.toLowerCase());
                });
            }
        });
    }

    private void populateForm(TherapyProgramTM p) {
        txtProgramName.setText(p.getName());
        txtProgramDuration.setText(p.getDuration());
        txtTotalSessions.setText(p.getTotalSessions() != null ? String.valueOf(p.getTotalSessions()) : "");
        txtProgramFee.setText(p.getFee() != null ? p.getFee().toPlainString() : "");
        txtProgramDescription.setText(p.getDescription());
    }

    @FXML
    void handleSaveProgram(ActionEvent event) {
        try {
            TherapyProgramDTO dto = new TherapyProgramDTO();
            dto.setName(txtProgramName.getText());
            dto.setDuration(txtProgramDuration.getText());
            dto.setTotalSessions(parseSessions());
            dto.setFee(parseFee());
            dto.setDescription(txtProgramDescription.getText());
            programService.saveProgram(dto);
            AlertUtil.showInfo("Success", "Program saved successfully.");
            handleClearProgram(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleUpdateProgram(ActionEvent event) {
        if (selectedProgram == null) {
            AlertUtil.showWarning("Warning", "Please select a program to update.");
            return;
        }
        try {
            TherapyProgramDTO dto = new TherapyProgramDTO();
            dto.setId(selectedProgram.getId());
            dto.setName(txtProgramName.getText());
            dto.setDuration(txtProgramDuration.getText());
            dto.setTotalSessions(parseSessions());
            dto.setFee(parseFee());
            dto.setDescription(txtProgramDescription.getText());

            programService.updateProgram(dto);
            AlertUtil.showInfo("Success", "Program updated successfully.");
            handleClearProgram(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleDeleteProgram(ActionEvent event) {
        TherapyProgramTM p = tblPrograms.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showWarning("Warning", "Please select a program to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete program \"" + p.getName() + "\"?")) {
            try {
                programService.deleteProgram(p.getId());
                AlertUtil.showInfo("Deleted", "Program deleted.");
                handleClearProgram(event);
                loadData();
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    void handleClearProgram(ActionEvent event) {
        txtProgramName.clear();
        txtProgramDuration.clear();
        txtTotalSessions.clear();
        txtProgramFee.clear();
        txtProgramDescription.clear();
        selectedProgram = null;
        tblPrograms.getSelectionModel().clearSelection();
    }

    private BigDecimal parseFee() {
        String text = txtProgramFee.getText();
        if (text == null || text.trim().isEmpty()) return null;
        return new BigDecimal(text.trim());
    }

    private Integer parseSessions() {
        String text = txtTotalSessions.getText();
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Total Sessions must be a valid integer.");
        }
    }
}
