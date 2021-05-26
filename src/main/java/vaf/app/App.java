package vaf.app;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import vaf.Main;
import vaf.VAF;
import vaf.scrapper.Browser;
import vaf.scrapper.ScannerProfile;
import vaf.scrapper.Vaccine;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalTime;
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

    public void openSettings(final Stage mainStage) {

        CheckboxGroupController<Vaccine> checkboxGroupController = new CheckboxGroupController<>(
                "Vaccins recherch\u00E9s :",
                Vaccine.values()
        );

//        checkboxGroupController.getSelectedElements().addListener((SetChangeListener<? super Vaccine>) change -> {
//            System.out.println(change.getSet());
//        });

        TimeRangeController timeRangeController = new TimeRangeController("Plage de recherche :",
                LocalTime.of(8, 0), LocalTime.of(22, 0));
//
//        timeRangeController.fromTimeProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("From: " + newValue);
//        });
//        timeRangeController.toTimeProperty().addListener((observable, oldValue, newValue) -> {
//            System.out.println("To: " + newValue);
//        });

        VBox layout = new VBox(
                timeRangeController, new Separator(),
                checkboxGroupController, new Separator()
        );

        Scene settingsScene = new Scene(layout);

        Stage settingsStage = new Stage();
        settingsStage.setTitle("Search Settings");
        settingsStage.setWidth(500);
        settingsStage.setHeight(300);
        settingsStage.setMinWidth(settingsStage.getWidth());
        settingsStage.setMinHeight(settingsStage.getHeight());

        settingsStage.setScene(settingsScene);
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initOwner(mainStage);

        settingsStage.show();
    }

    public void addCenterDialog(final Stage mainStage) {

        TextInputDialog textInputDialog = new TextInputDialog("https://www.doctolib.fr/vaccination-covid-19/paris/centre-de-vaccination-covid-centre-de-sante-bauchat-nation?highlight%5Bspeciality_ids%5D%5B%5D=5494");

        textInputDialog.setTitle("Ajouter un centre manuellement");
        textInputDialog.setHeaderText("Veuillez entrez l'URL du centre de vaccination\n" +
                "Exemple d'url : https://www.doctolib.fr/vaccination-covid-19/paris/centre-de-vaccination-covid-centre-de-sante-bauchat-nation?highlight%5Bspeciality_ids%5D%5B%5D=5494");
        textInputDialog.setContentText("URL :");

        textInputDialog.initModality(Modality.APPLICATION_MODAL);
        textInputDialog.initOwner(mainStage);

        textInputDialog.resultProperty().addListener((observable, oldValue, newValue) -> {
//            VAF.INSTANCE.profileFactory.submitUrl(newValue);
            if (VAF.INSTANCE.profileFactory.generateProfiles(newValue))
                VAF.INSTANCE.startScanning();
            else
                invalidUrlWarning();
        });

        textInputDialog.show();
    }

    public void invalidLocationSearchWarning() {
        Alert invalidLocationSearch = new Alert(Alert.AlertType.WARNING);
        invalidLocationSearch.setHeaderText("Le lieu de recherche fournie est invalide.");
        invalidLocationSearch.setContentText("Exemples valides: \"Renne\", \"Paris 75015\"");
        invalidLocationSearch.show();
    }

    public void invalidUrlWarning() {
        Alert invalidUrlWarning = new Alert(Alert.AlertType.WARNING);
        invalidUrlWarning.setHeaderText("L'url fournie est invalide.");
    }

    public void start(final Stage mainStage) {

        mainStage.setTitle("VAS");

        Button searchSettingsButton = new Button("Paramètres de recherche");
        searchSettingsButton.setOnMouseClicked(event -> {
            openSettings(mainStage);
        });


        Button addCenterButton = new Button("Ajouter un centre manuellement");
        addCenterButton.setOnMouseClicked(event -> {
            addCenterDialog(mainStage);
        });

        TextField locationSearchBar = new TextField();
        Text locationText = new Text("Lieu de recherche : ");
        Tooltip locationTooltip = new Tooltip("Exemple: \"Renne\", \"Paris 75015\"");
        locationSearchBar.setTooltip(locationTooltip);
        locationTooltip.setShowDelay(Duration.seconds(0));
        locationTooltip.setFont(Font.font("", 14));
        locationTooltip.setOnShowing(s -> {
            Bounds bounds = locationSearchBar.localToScreen(locationSearchBar.getBoundsInLocal());
            locationSearchBar.getTooltip().setX(bounds.getMinX() - locationTooltip.getWidth() / 2);
            locationSearchBar.getTooltip().setY(bounds.getMaxY());
        });

        locationSearchBar.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                VAF.INSTANCE.service.submit(() -> {
                    locationSearchBar.setDisable(true);
                    VAF.INSTANCE.centerSearcher.submitSearch(locationSearchBar.getText());
//                    if (!VAF.INSTANCE.centerSearcher.search(locationSearch.getText()))
//                        invalidLocationName.show();
                    locationSearchBar.setDisable(false);
                });
            }
        });

        ToolBar toolBar = new ToolBar(
                searchSettingsButton, new Separator(), addCenterButton, Utils.hSpacer(), locationText, locationSearchBar
        );

        ScrollPane scrollPane = new ScrollPane(scannerListView);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        VBox layout = new VBox(toolBar, scrollPane);
        scannerListView.prefWidthProperty().bind(layout.widthProperty());
        scannerListView.prefHeightProperty().bind(layout.heightProperty());

        Scene scene = new Scene(layout);
        scene.getRoot().getStylesheets().add(mainCss.toString());

        mainStage.setWidth(800);
        mainStage.setHeight(500);
        mainStage.setMinWidth(mainStage.getWidth());
        mainStage.setMinHeight(mainStage.getHeight());
        mainStage.setScene(scene);
        if (appIcon != null)
            mainStage.getIcons().add(new Image(appIcon));

        mainStage.show();
    }
}
