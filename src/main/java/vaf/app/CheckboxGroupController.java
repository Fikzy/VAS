package vaf.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.HashSet;

public class CheckboxGroupController<T> extends VBox {

    private final ObservableSet<T> selectedElements = FXCollections.observableSet(new HashSet<>());

    public ObservableSet<T> getSelectedElements() {
        return selectedElements;
    }

    public CheckboxGroupController(final String label, final T[] elements) {

        this.setSpacing(5);

        getChildren().add(new Text(label));

        for (final T element : elements) {
            CheckBox checkBox = new CheckBox(element.toString());
            configureCheckbox(checkBox, element);
            getChildren().add(checkBox);
        }
    }

    private void configureCheckbox(final CheckBox checkbox, final T element) {

        checkbox.setSelected(true);
        selectedElements.add(element);

        checkbox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected)
                selectedElements.add(element);
            else
                selectedElements.remove(element);
        });
    }
}
