package vaf.app;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vaf.Main;
import vaf.VAF;
import vaf.scrapper.ScannerInstance;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public enum App {
    INSTANCE();

    private final InputStream appIcon = Main.class.getResourceAsStream("images/icon1.png");
    private final URL mainCss = Main.class.getResource("main.css");

    private final Map<ScannerInstance, ScannerDisplay> scanners = new HashMap<>();
    private final VBox scannerListView = new VBox();

    public void addScannerDisplay(final ScannerInstance scannerInstance) {

        final ScannerDisplay scannerDisplay = new ScannerDisplay(scannerInstance);
        scanners.put(scannerInstance, scannerDisplay);

        scannerDisplay.deleteButton.setOnMouseClicked(mouseEvent -> {
            removeScanner(scannerInstance);
            scannerDisplay.deleteButton.setDisable(true);
        });

        Platform.runLater(() -> {
            scannerListView.getChildren().add(scannerDisplay);
        });
    }

    public void removeScanner(final ScannerInstance scannerInstance) {

        ScannerDisplay display = scanners.get(scannerInstance);
        if (display == null)
            return;

        VAF.INSTANCE.removeScanner(scannerInstance);

        Platform.runLater(() -> {
            scannerListView.getChildren().remove(display);
        });
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
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(500);

        if (appIcon != null)
            stage.getIcons().add(new Image(appIcon));

        addButton.setOnMouseClicked(mouseEvent -> {
//            addScannerDisplay(new ScannerInstance("Fake shit", new VaccineSelection(Vaccine.Pfizer, 0)));
        });

        clearAllButton.setOnMouseClicked(mouseEvent -> {
            VAF.INSTANCE.clearScanners();
            scannerListView.getChildren().clear();
        });

        scannerListView.getChildren().addListener(
                (ListChangeListener<Node>) change -> addFromLocation.setVisible(scannerListView.getChildren().isEmpty())
        );

        stage.show();
    }
}
