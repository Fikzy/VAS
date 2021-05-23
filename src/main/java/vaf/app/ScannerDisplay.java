package vaf.app;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.text.Text;
import vaf.scrapper.ScannerInstance;

public class ScannerDisplay extends ToolBar {

    final ScannerInstance scannerInstance;

    final ToggleSwitch toggleSwitch;
    final Text vaccineName;
    final Text centerTitle;
    final Button deleteButton;

    public ScannerDisplay(final ScannerInstance scannerInstance) {

        this.scannerInstance = scannerInstance;

        this.toggleSwitch = new ToggleSwitch();
        this.toggleSwitch.switchOnProperty().set(true);
        this.vaccineName = new Text(scannerInstance.selection().vaccine().name());
        this.centerTitle = new Text(scannerInstance.centerTitle());
        this.deleteButton = new Button("Remove"); // ✕
        this.deleteButton.getStyleClass().add("remove-button");

        this.setPadding(new Insets(10));
        this.getItems().addAll(toggleSwitch, Utils.hSpacer(10), vaccineName, new Separator(), centerTitle, Utils.hSpacer(), deleteButton);
        this.getStyleClass().add("center-display");
        this.toggleSwitch.switchOnProperty().addListener((observableValue, aBoolean, t1) -> {
            this.pseudoClassStateChanged(PseudoClass.getPseudoClass("disabled"), aBoolean);
        });
    }
}
