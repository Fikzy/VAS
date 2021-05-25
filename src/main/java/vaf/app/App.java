package vaf.app;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vaf.Main;
import vaf.VAF;
import vaf.scrapper.Browser;
import vaf.scrapper.ScannerProfile;

import java.io.InputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum App {
    INSTANCE();

    private static final InputStream appIcon = Main.class.getResourceAsStream("icon.png");
    private static final URL mainCss = Main.class.getResource("main.css");
    private static final InputStream browserIcon = Main.class.getResourceAsStream("internet_browser_icon_64.png");

    private final Map<ScannerProfile, ScannerDisplay> scannerDisplays = new HashMap<>();
    private final VBox scannerListView = new VBox();

    App() {

        VAF.INSTANCE.onScannerProfileAdd.subscribe(this::addScannerDisplay);
        VAF.INSTANCE.onScannerProfileRemove.subscribe(this::removeScannerDisplay);

        VAF.INSTANCE.onScannerStartScan.subscribe(this::scannerDisplayStartScan);
        VAF.INSTANCE.onScannerSuccessfulScan.subscribe(this::scannerDisplaySuccessfulScan);
        VAF.INSTANCE.onScannerStopScan.subscribe(this::scannerDisplayStopScan);
    }

    private Optional<ScannerDisplay> getScannerDisplay(final ScannerProfile scannerProfile) {
        ScannerDisplay display = scannerDisplays.get(scannerProfile);
        return display == null ? Optional.empty() : Optional.of(display);
    }

    public void addScannerDisplay(final ScannerProfile scannerProfile) {

        final ScannerDisplay scannerDisplay = new ScannerDisplay(scannerProfile);
        scannerDisplays.put(scannerProfile, scannerDisplay);

        scannerDisplay.deleteButton.setOnMouseClicked(mouseEvent -> {
            VAF.INSTANCE.removeScannerProfile(scannerProfile);
            scannerDisplay.deleteButton.setDisable(true);
        });

        Platform.runLater(() -> {
            scannerListView.getChildren().add(scannerDisplay);
        });
    }

    public void removeScannerDisplay(final ScannerProfile scannerProfile) {
        getScannerDisplay(scannerProfile).ifPresent(display -> {
            Platform.runLater(() -> scannerListView.getChildren().remove(display));
        });
    }

    public void scannerDisplayStartScan(final ScannerProfile scannerProfile) {
        getScannerDisplay(scannerProfile).ifPresent(display -> {
            Platform.runLater(display::startScanning);
        });
    }

    public void scannerDisplaySuccessfulScan(final ScannerProfile scannerProfile) {
        getScannerDisplay(scannerProfile).ifPresent(display -> {
            Platform.runLater(display::successfulScan);
        });
    }

    public void scannerDisplayStopScan(final ScannerProfile scannerProfile) {
        getScannerDisplay(scannerProfile).ifPresent(display -> {
            Platform.runLater(display::stopScanning);
        });
    }

    public static Browser browserSelection() {

        ChoiceDialog<Browser> browserChoiceDialog = new ChoiceDialog<>(Browser.Chrome, EnumSet.allOf(Browser.class));
        browserChoiceDialog.setTitle("Choix du navigateur");
        browserChoiceDialog.setHeaderText("Choissisez un navigateur present sur votre machine");
        browserChoiceDialog.setContentText("Navigateur :");
        if (browserIcon != null)
            browserChoiceDialog.setGraphic(new ImageView(new Image(browserIcon)));

        Alert invalidBrowserAlert = new Alert(Alert.AlertType.WARNING);
        invalidBrowserAlert.setHeaderText("Merci de selectionner un navigateur presentement installe sur votre machine.");

        Optional<Browser> selectedBrowser = Optional.empty();
        while (selectedBrowser.isEmpty()) {

            selectedBrowser = browserChoiceDialog.showAndWait();
            if (selectedBrowser.isEmpty())
                System.exit(0);

            Browser.setBrowser(selectedBrowser.get());
            try {
                Browser.getDriver(true).quit();
            } catch (RuntimeException e) {
                e.printStackTrace();
                selectedBrowser = Optional.empty();
                invalidBrowserAlert.showAndWait();
            }
        }

        return selectedBrowser.get();
    }

    public void start(Stage stage) {

        stage.setTitle("VAF");

        ToolBar toolBar = new ToolBar();
        Button addButton = new Button("Add");
        Button addFromLocation = new Button("Add centers from location");
        Button clearAllButton = new Button("Clear All");
        clearAllButton.getStyleClass().add("remove-button");
        addFromLocation.setVisible(false);
        toolBar.getItems().addAll(addButton, addFromLocation, Utils.hSpacer(), clearAllButton);

        ScrollPane scrollPane = new ScrollPane(scannerListView);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        VBox layout = new VBox(toolBar, scrollPane);
        scannerListView.prefWidthProperty().bind(layout.widthProperty());
        scannerListView.prefHeightProperty().bind(layout.heightProperty());

        Scene scene = new Scene(layout);
        scene.getRoot().getStylesheets().add(mainCss.toString());
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setScene(scene);

        if (appIcon != null)
            stage.getIcons().add(new Image(appIcon));

        addButton.setOnMouseClicked(mouseEvent -> {
//            addScannerDisplay(new ScannerInstance("Fake shit", new VaccineSelection(Vaccine.Pfizer, 0)));
        });

        clearAllButton.setOnMouseClicked(mouseEvent -> {
//            VAF.INSTANCE.clearScanners();
//            scannerListView.getChildren().clear();
        });

        scannerListView.getChildren().addListener(
                (ListChangeListener<Node>) change -> addFromLocation.setVisible(scannerListView.getChildren().isEmpty())
        );

        stage.show();
    }
}
