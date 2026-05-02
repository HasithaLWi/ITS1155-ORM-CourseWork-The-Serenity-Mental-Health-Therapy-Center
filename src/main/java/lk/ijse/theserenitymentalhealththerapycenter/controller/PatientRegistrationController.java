package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.PatientService;
import lk.ijse.theserenitymentalhealththerapycenter.bo.TherapyProgramService;
import lk.ijse.theserenitymentalhealththerapycenter.entity.Patient;
import lk.ijse.theserenitymentalhealththerapycenter.entity.TherapyProgram;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import static java.util.Locale.filter;

public class PatientRegistrationController implements Initializable {

    @FXML
    private TextField txtPatientName;
    @FXML
    private TextField txtPatientEmail;
    @FXML
    private TextField txtPatientPhone;
    @FXML
    private TextField txtPatientAddress;
    @FXML
    private ComboBox<TherapyProgram> cmbPatientProgram;
    @FXML
    private Label lblRegMessage;
    @FXML
    private Button btnAddPgm;
    @FXML
    private Button btnPayUpfront;
    @FXML
    private Label lblUpfrontPaymentStatus;
    @FXML
    private Label lblUpfrontPrice;
    @FXML
    private TableView<TherapyProgram> tblSelectedPgm;
    @FXML
    private TableColumn<?, String> columnPgm;
    @FXML
    private TableColumn<?, ?> columnUpfront;
    @FXML
    private TableColumn<TherapyProgram, Void> columnAction;
    @FXML
    private TextArea txtInterviewNote;


    private final PatientService patientService = new PatientService();
    private final TherapyProgramService programService = new TherapyProgramService();

    ObservableList<TherapyProgram> selectedPrograms = FXCollections.observableArrayList();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            cmbPatientProgram.setItems(FXCollections.observableArrayList(programService.getAllPrograms()));
        } catch (Exception e) {
            System.err.println("Error loading programs: " + e.getMessage());
        }

        cmbPatientProgram.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Therapy Program" : item.getName());
            }
        });
        cmbPatientProgram.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TherapyProgram item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select Therapy Program" : item.getName());
            }
        });

        cmbPatientProgram.valueProperty().addListener((obs, oldPgm, newPgm) -> {
            if (newPgm != null && !selectedPrograms.contains(newPgm)) {
                selectedPrograms.add(newPgm);
                double fee = 0.0;
                for (TherapyProgram p : tblSelectedPgm.getItems()) {
                    fee += p.getFee().doubleValue();
                }
                double upfront = fee * 0.1; // 10% upfront
                lblUpfrontPaymentStatus.setStyle("-fx-text-fill: #C47171; -fx-font-size: 12px;");
                lblUpfrontPaymentStatus.setText("PENDING...");
                lblUpfrontPrice.setStyle("-fx-text-fill: #C47171; -fx-font-size: 12px;");
                lblUpfrontPrice.setText("Rs " + String.format("%.2f", upfront));


            }
        });


        try {
            tblSelectedPgm.setItems(selectedPrograms);
        } catch (Exception e) {
            System.err.println("Error loading programs for table: " + e.getMessage());
        }

        tblSelectedPgm.setPlaceholder(new Label("No programs added yet"));
        columnPgm.setCellValueFactory(new PropertyValueFactory<>("name"));

        columnUpfront.setCellValueFactory(new PropertyValueFactory<>("fee"));


        columnAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemove = new Button("Remove");


            {

                btnRemove.getStyleClass().add("btn-Pgm-remove");

                btnRemove.setOnAction(event -> {
                    if (isEmpty() || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                        return;
                    }

                    TherapyProgram program = getTableView().getItems().get(getIndex());
                    selectedPrograms.remove(program);

                    double updatedFee = 0.0;
                    for (TherapyProgram p : getTableView().getItems()) {
                        updatedFee += p.getFee().doubleValue();
                    }
                    lblUpfrontPrice.setText("Rs " + String.format("%.2f", updatedFee * 0.1));

                    cmbPatientProgram.setValue(null);

                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
                setText(null);
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
        tblSelectedPgm.getItems().clear();
        txtInterviewNote.clear();
        lblUpfrontPaymentStatus.setStyle("-fx-text-fill: #000000; -fx-font-size: 12px;");
        lblUpfrontPaymentStatus.setText("...");
        lblUpfrontPrice.setStyle("-fx-text-fill: #000000; -fx-font-size: 12px;");
        lblUpfrontPrice.setText("Rs 0.00");
        cmbPatientProgram.setValue(null);

    }
}
