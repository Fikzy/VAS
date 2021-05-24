package vaf.app;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToolBar;
import javafx.scene.text.Text;
import vaf.scrapper.ScannerInstance;

public class ScannerDisplay extends ToolBar {

    static final double height = 40;

    final ScannerInstance scannerInstance;

    final ToggleSwitch toggleSwitch;
    final Text centerTitle;
    final Text vaccineName;
    final ProgressIndicator progressIndicator;
    final Button deleteButton;

    public ScannerDisplay(final ScannerInstance scannerInstance) {

        this.setMinHeight(height);
        this.setMaxHeight(height);
        this.setPrefHeight(height);

        this.scannerInstance = scannerInstance;

        this.toggleSwitch = new ToggleSwitch();
        this.toggleSwitch.switchOnProperty().set(true);

        this.progressIndicator = new ProgressIndicator(-1);
        this.progressIndicator.setMaxSize(20, 20);

        this.centerTitle = new Text(scannerInstance.centerTitle());

        this.vaccineName = new Text(scannerInstance.selection().vaccine().name());

        this.deleteButton = new Button("Remove"); // ✕
        this.deleteButton.getStyleClass().add("remove-button");

        this.setPadding(new Insets(10));
        this.getItems().addAll(
                toggleSwitch, Utils.hSpacer(10), centerTitle, progressIndicator, Utils.hSpacer(), vaccineName, Utils.hSpacer(20), deleteButton)
        ;
        this.getStyleClass().add("center-display");
        this.toggleSwitch.switchOnProperty().addListener((observableValue, aBoolean, t1) -> {
            this.pseudoClassStateChanged(PseudoClass.getPseudoClass("disabled"), aBoolean);
        });
    }
}
