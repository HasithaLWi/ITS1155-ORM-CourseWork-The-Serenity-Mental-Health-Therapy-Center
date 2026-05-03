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
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProgramManagementController implements Initializable {

    @FXML private TextField txtProgramName;
    @FXML private TextField txtProgramDuration;
    @FXML private TextField txtProgramFee;
    @FXML private TextArea txtProgramDescription;
    @FXML private TextField txtSearchProgram;

    @FXML private TableView<TherapyProgram> tblPrograms;
    @FXML private TableColumn<TherapyProgram, Long> colProgramId;
    @FXML private TableColumn<TherapyProgram, String> colProgramName;
    @FXML private TableColumn<TherapyProgram, String> colProgramDuration;
    @FXML private TableColumn<TherapyProgram, BigDecimal> colProgramFee;
    @FXML private TableColumn<TherapyProgram, String> colProgramDescription;

    private final TherapyProgramBOImpl programService = new TherapyProgramBOImpl();
    private FilteredList<TherapyProgram> filteredPrograms;
    private TherapyProgram selectedProgram;

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
        colProgramId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getId()));
        colProgramName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colProgramDuration.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDuration()));
        colProgramFee.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFee()));
        colProgramDescription.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
    }

    private void loadData() {
        try {
            List<TherapyProgram> list = programService.getAllPrograms();
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

    private void populateForm(TherapyProgram p) {
        txtProgramName.setText(p.getName());
        txtProgramDuration.setText(p.getDuration());
        txtProgramFee.setText(p.getFee() != null ? p.getFee().toPlainString() : "");
        txtProgramDescription.setText(p.getDescription());
    }

    @FXML
    void handleSaveProgram(ActionEvent event) {
        try {
            TherapyProgram p = new TherapyProgram();
            p.setName(txtProgramName.getText());
            p.setDuration(txtProgramDuration.getText());
            p.setFee(parseFee());
            p.setDescription(txtProgramDescription.getText());
            programService.saveProgram(p);
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
            selectedProgram.setName(txtProgramName.getText());
            selectedProgram.setDuration(txtProgramDuration.getText());
            selectedProgram.setFee(parseFee());
            selectedProgram.setDescription(txtProgramDescription.getText());
            programService.updateProgram(selectedProgram);
            AlertUtil.showInfo("Success", "Program updated successfully.");
            handleClearProgram(event);
            loadData();
        } catch (Exception e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    void handleDeleteProgram(ActionEvent event) {
        TherapyProgram p = tblPrograms.getSelectionModel().getSelectedItem();
        if (p == null) {
            AlertUtil.showWarning("Warning", "Please select a program to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm", "Delete program \"" + p.getName() + "\"?")) {
            try {
                programService.deleteProgram(p);
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
}
