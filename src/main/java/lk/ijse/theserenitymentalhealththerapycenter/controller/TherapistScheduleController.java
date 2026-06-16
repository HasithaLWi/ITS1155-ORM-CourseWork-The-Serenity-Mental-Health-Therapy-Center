package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

public class TherapistScheduleController implements Initializable {

    @FXML private ComboBox<String> comboTherapist;
    @FXML private Label lblCurrentWeek;
    @FXML private GridPane scheduleGrid;
    @FXML private Button btnPrevWeek;
    @FXML private Button btnNextWeek;
    @FXML private Button btnSaveSchedule;

    // --- State Variables ---
    private LocalDate currentWeekStart;
    private ContextMenu sharedContextMenu;
    private Set<SlotPane> selectedSlots = new HashSet<>();

    // Colors matching your FXML legend
    private final String COLOR_AVAILABLE = "#2ecc71";
    private final String COLOR_TIME_OFF = "#e74c3c";
    private final String COLOR_DEFAULT = "#ffffff";
    private final String BORDER_HIGHLIGHT = "-fx-border-color: #2980b9; -fx-border-width: 2;";
    private final String BORDER_DEFAULT = "-fx-border-color: #eaeded; -fx-border-width: 1;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Initialize Date State (Start on current week's Monday)
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        updateWeekLabel();

        // 2. Setup the UI Components
        setupContextMenu();
        buildInteractiveGrid();
        loadTherapists();

        // 3. Setup Button Listeners
        btnPrevWeek.setOnAction(e -> changeWeek(-1));
        btnNextWeek.setOnAction(e -> changeWeek(1));
        btnSaveSchedule.setOnAction(e -> saveChangesToDatabase());

        comboTherapist.setOnAction(e -> loadScheduleFromDatabase());
    }

    /**
     * Creates the shared Context Menu to avoid memory bloat.
     */
    private void setupContextMenu() {
        sharedContextMenu = new ContextMenu();

        MenuItem itemAvailable = new MenuItem("Set Available (Master Rule)");
        itemAvailable.setOnAction(e -> applyStatusToSelection("AVAILABLE", COLOR_AVAILABLE));

        MenuItem itemTimeOff = new MenuItem("Mark Time Off (Exception)");
        itemTimeOff.setOnAction(e -> applyStatusToSelection("TIME_OFF", COLOR_TIME_OFF));

        MenuItem itemClear = new MenuItem("Clear Slot");
        itemClear.setOnAction(e -> applyStatusToSelection("CLEAR", COLOR_DEFAULT));

        sharedContextMenu.getItems().addAll(itemAvailable, itemTimeOff, new SeparatorMenuItem(), itemClear);
    }

    /**
     * Programmatically generates the 10x7 grid (08:00 to 17:00) 
     * and attaches the drag-to-select and right-click logic.
     */
    private void buildInteractiveGrid() {
        // Rows: 1 to 10 (representing 08:00 to 17:00)
        for (int row = 1; row <= 10; row++) {
            LocalTime time = LocalTime.of(row + 7, 0); // Starts at 08:00

            // Add Time Label to the first column
            Label timeLabel = new Label(time.toString());
            timeLabel.setStyle("-fx-padding: 0 10 0 10; -fx-text-fill: #7f8c8d;");
            scheduleGrid.add(timeLabel, 0, row);

            // Cols: 1 to 7 (Monday to Sunday)
            for (int col = 1; col <= 7; col++) {
                DayOfWeek day = DayOfWeek.of(col);

                // Create our custom SlotPane
                SlotPane slot = new SlotPane(day, time);
                slot.setPrefHeight(40.0);
                slot.setMaxWidth(Double.MAX_VALUE); // Fill cell
                slot.setDefaultStyle();

                // --- MOUSE EVENTS FOR MULTI-SELECTION & CONTEXT MENU ---

                // 1. Mouse Pressed: Clear old selection, start new selection, or show menu
                slot.setOnMousePressed(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        clearSelection();
                        selectSlot(slot);
                    } else if (e.getButton() == MouseButton.SECONDARY) {
                        // If right-clicking an unselected slot, select ONLY this slot first
                        if (!selectedSlots.contains(slot)) {
                            clearSelection();
                            selectSlot(slot);
                        }
                        sharedContextMenu.show(slot, e.getScreenX(), e.getScreenY());
                    }
                });

                // 2. Drag Detected: Required to trigger "Full Drag" in JavaFX
                slot.setOnDragDetected(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        slot.startFullDrag();
                    }
                });

                // 3. Mouse Drag Entered: Add to selection as mouse moves over it
                slot.setOnMouseDragEntered(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        selectSlot(slot);
                    }
                });

                scheduleGrid.add(slot, col, row);
            }
        }
    }

    // --- UI HELPER METHODS ---

    private void selectSlot(SlotPane slot) {
        selectedSlots.add(slot);
        slot.setStyle("-fx-background-color: " + slot.getCurrentColor() + "; " + BORDER_HIGHLIGHT);
    }

    private void clearSelection() {
        for (SlotPane slot : selectedSlots) {
            slot.setDefaultStyle();
        }
        selectedSlots.clear();
        sharedContextMenu.hide();
    }

    private void applyStatusToSelection(String status, String hexColor) {
        for (SlotPane slot : selectedSlots) {
            slot.setCurrentStatus(status);
            slot.setCurrentColor(hexColor);
            slot.setDefaultStyle(); // Removes highlight border, applies new color
        }
        selectedSlots.clear();
    }

    private void changeWeek(int weeksToAdd) {
        currentWeekStart = currentWeekStart.plusWeeks(weeksToAdd);
        updateWeekLabel();
        loadScheduleFromDatabase(); // Refresh data for the new week
    }

    private void updateWeekLabel() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        lblCurrentWeek.setText(currentWeekStart.format(formatter) + "  -  " + weekEnd.format(formatter));
    }

    private void loadTherapists() {
        // TODO: Hibernate query -> SELECT name FROM doctors
        comboTherapist.getItems().addAll("Dr. Smith", "Dr. Adams", "Dr. Lee");
    }

    // --- HIBERNATE / DATABASE LOGIC ---

    private void loadScheduleFromDatabase() {
        if (comboTherapist.getValue() == null) return;

        System.out.println("Loading data for " + comboTherapist.getValue() + " for week of " + currentWeekStart);

        // 1. Reset grid to blank
        // 2. Hibernate: SELECT * FROM schedule_rules WHERE doctor_id = X
        //    -> Loop through results, find the matching SlotPane, set status to "AVAILABLE" (Green)
        // 3. Hibernate: SELECT * FROM schedule_exceptions WHERE doctor_id = X AND specific_date BETWEEN start AND end
        //    -> Loop through results, override matching SlotPane to "TIME_OFF" (Red)
    }

    private void saveChangesToDatabase() {
        if (comboTherapist.getValue() == null) {
            showAlert("Error", "Please select a therapist first.");
            return;
        }

        System.out.println("Saving changes to Hibernate...");

        // Loop through all nodes in the GridPane
        for (javafx.scene.Node node : scheduleGrid.getChildren()) {
            if (node instanceof SlotPane) {
                SlotPane slot = (SlotPane) node;

                // Get the exact date for this specific column
                LocalDate slotDate = currentWeekStart.plusDays(slot.getDayOfWeek().getValue() - 1);

                if (slot.getCurrentStatus().equals("AVAILABLE")) {
                    // TODO: Hibernate -> Save to `schedule_rules` table (Master Rule)
                    System.out.println("Saving Master Rule: " + slot.getDayOfWeek() + " at " + slot.getTime());
                }
                else if (slot.getCurrentStatus().equals("TIME_OFF")) {
                    // TODO: Hibernate -> Save to `schedule_exceptions` table (Specific Date)
                    System.out.println("Saving Exception: " + slotDate + " at " + slot.getTime());
                }
            }
        }

        showAlert("Success", "Schedule saved successfully!");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Custom Inner Class representing a single 1-hour block on the calendar.
     * This holds the data so you don't have to parse UI strings later.
     */
    private class SlotPane extends Pane {
        private final DayOfWeek dayOfWeek;
        private final LocalTime time;
        private String currentStatus = "UNSET";
        private String currentColor = COLOR_DEFAULT;

        public SlotPane(DayOfWeek dayOfWeek, LocalTime time) {
            this.dayOfWeek = dayOfWeek;
            this.time = time;
        }

        public void setDefaultStyle() {
            this.setStyle("-fx-background-color: " + currentColor + "; " + BORDER_DEFAULT);
        }

        // Getters and Setters
        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public LocalTime getTime() { return time; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
        public String getCurrentColor() { return currentColor; }
        public void setCurrentColor(String currentColor) { this.currentColor = currentColor; }
    }
}