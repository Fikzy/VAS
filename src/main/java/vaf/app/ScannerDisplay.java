package vaf.app;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToolBar;
import javafx.scene.text.Text;
import vaf.scrapper.ScannerProfile;

public class ScannerDisplay extends ToolBar {

    static final double height = 40;

    final ToggleSwitch toggleSwitch;
    final Text centerTitle;
    final Text vaccineName;
    final ProgressIndicator progressIndicator;
    final Button deleteButton;

    public ScannerDisplay(final ScannerProfile scannerProfile) {

        this.setMinHeight(height);
        this.setMaxHeight(height);
        this.setPrefHeight(height);

        this.toggleSwitch = new ToggleSwitch();
        this.toggleSwitch.switchOnProperty().set(true);

        this.progressIndicator = new ProgressIndicator(0);
        this.progressIndicator.setMaxSize(20, 20);
        this.progressIndicator.setVisible(false);

        this.centerTitle = new Text(scannerProfile.centerTitle());

        this.vaccineName = new Text(scannerProfile.selectedVaccine().name());

        this.deleteButton = new Button("Remove"); // ✕
        this.deleteButton.getStyleClass().add("remove-button");

        this.setPadding(new Insets(10));
        this.getItems().addAll(
                toggleSwitch, Utils.hSpacer(10), centerTitle, progressIndicator, Utils.hSpacer(), vaccineName, Utils.hSpacer(20), deleteButton)
        ;
        this.getStyleClass().add("scanner-display");
        this.toggleSwitch.switchOnProperty().addListener((observableValue, aBoolean, t1) -> {
            this.pseudoClassStateChanged(PseudoClass.getPseudoClass("disabled"), aBoolean);
        });
    }

    public void startScanning() {
        progressIndicator.setProgress(-1);
        progressIndicator.setVisible(true);
    }

    public void successfulScan() {
        progressIndicator.setVisible(false);
        pseudoClassStateChanged(PseudoClass.getPseudoClass("success"), true);
    }

    public void stopScanning() {
        progressIndicator.setVisible(false);
    }
}
