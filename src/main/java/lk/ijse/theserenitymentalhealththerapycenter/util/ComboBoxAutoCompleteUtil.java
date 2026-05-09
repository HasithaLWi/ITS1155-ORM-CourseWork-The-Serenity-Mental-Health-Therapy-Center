package lk.ijse.theserenitymentalhealththerapycenter.util;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;

public final class ComboBoxAutoCompleteUtil {

    private ComboBoxAutoCompleteUtil() {
    }

    public static <T> void setupAutocomplete(ComboBox<T> comboBox, List<T> allItems,
            Function<T, String> displayFunc,
            Function<T, String> searchFunc) {

        comboBox.setEditable(true);
        ObservableList<T> items = FXCollections.observableArrayList(allItems);
        FilteredList<T> filteredItems = new FilteredList<>(items, p -> true);
        comboBox.setItems(filteredItems);

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : displayFunc.apply(object);
            }

            @Override
            public T fromString(String string) {
                if (string == null || string.isEmpty())
                    return null;
                return items.stream()
                        .filter(item -> {
                            String displayValue = displayFunc.apply(item);
                            String searchValue = searchFunc.apply(item);
                            if (displayValue != null && displayValue.equals(string))
                                return true;
                            return searchValue != null && searchValue.toLowerCase().contains(string.toLowerCase());
                        })
                        .findFirst().orElse(null);
            }
        });

        final boolean[] isUpdating = { false };

        comboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (isUpdating[0])
                return;

            Platform.runLater(() -> {
                if (isUpdating[0])
                    return;

                String text = comboBox.getEditor().getText();
                if (text == null || text.isEmpty())
                    return;
                String currentText = text.trim();

                T selected = comboBox.getSelectionModel().getSelectedItem();
                String selectedText = selected == null ? null : displayFunc.apply(selected);
                if (selectedText != null && selectedText.equals(currentText)) {
                    return;
                }

                isUpdating[0] = true;
                try {
                    if (currentText.isEmpty()) {
                        filteredItems.setPredicate(p -> true);
                        comboBox.hide();
                    } else {
                        String lowerCaseFilter = currentText.toLowerCase();
                        filteredItems.setPredicate(item -> {
                            String searchValue = searchFunc.apply(item);
                            return searchValue != null && searchValue.toLowerCase().contains(lowerCaseFilter);
                        });
                        if (!filteredItems.isEmpty() && !comboBox.isShowing()) {
                            comboBox.show();
                        } else if (filteredItems.isEmpty()) {
                            comboBox.hide();
                        }
                    }
                } finally {
                    isUpdating[0] = false;
                }
            });
        });

        comboBox.setOnAction(event -> {
            T newVal = comboBox.getSelectionModel().getSelectedItem();
            if (newVal == null)
                return;
            isUpdating[0] = true;
            try {
                filteredItems.setPredicate(p -> true);
                comboBox.hide();
            } finally {
                isUpdating[0] = false;
            }
        });

        comboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : displayFunc.apply(item));
            }
        });
    }
}
