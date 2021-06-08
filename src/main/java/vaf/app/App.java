package vaf.app;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum App {
    INSTANCE();

//    private static final VAF.logger VAF.logger = LogManager.getVAF.logger(App.class);

    private static final URL mainCss = Main.class.getResource("main.css");
    private static final InputStream browserIcon = Main.class.getResourceAsStream("internet_browser_icon_64.png");
    private static final InputStream searchIcon = Main.class.getResourceAsStream("search_icon_64.png");

    private final Map<ScannerProfile, ScannerDisplay> scannerDisplays = new HashMap<>();
    private final VBox scannerListView = new VBox();

    private final Stage settingsStage = new Stage();

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

        VAF.logger.info("scannerDisplaySuccessfulScan");

        Platform.runLater(() -> {

            VAF.logger.info("scannerDisplaySuccessfulScan - runLater");

            getScannerDisplay(scannerProfile).ifPresent(ScannerDisplay::successfulScan);

            String content = """
                    Il ne vous reste plus qu'à vous connecter avec votre compte Doctolib pour récupérer le rendez-vous à votre nom !

                    Si le rendez vous ne vous convient pas ou vous souhaitez effectuer une nouvelle recherche, vous pouvez continuer :
                    """;

            ButtonType resumeType = new ButtonType("Continuer", ButtonBar.ButtonData.OK_DONE);
            ButtonType quitType = new ButtonType("Quitter", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert appointmentFoundPopup = new Alert(Alert.AlertType.INFORMATION, content, resumeType, quitType);
            appointmentFoundPopup.setTitle("Rendez vous trouvé !");
            appointmentFoundPopup.setHeaderText("Rendez vous trouvé !");

            Optional<ButtonType> result = appointmentFoundPopup.showAndWait();
            if (result.isEmpty() || result.get() == quitType) {
                VAF.INSTANCE.shutdown();
            } else if (result.get() == resumeType) {
                getScannerDisplay(scannerProfile).ifPresent(ScannerDisplay::resetSuccess);
                VAF.INSTANCE.startScanning();
            }
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
        browserChoiceDialog.setHeaderText("Choissisez un navigateur présent sur votre machine");
        browserChoiceDialog.setContentText("Navigateur :");
        if (browserIcon != null)
            browserChoiceDialog.setGraphic(new ImageView(new Image(browserIcon)));

        Alert invalidBrowserAlert = new Alert(Alert.AlertType.WARNING);
        invalidBrowserAlert.setHeaderText("Merci de sélectionner un navigateur présentement installé sur votre machine.");

        Optional<Browser> selectedBrowser = Optional.empty();
        while (selectedBrowser.isEmpty()) {

            selectedBrowser = browserChoiceDialog.showAndWait();
            if (selectedBrowser.isEmpty()) {
                VAF.INSTANCE.shutdown();
                return null;
            }

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

    public void setupSearchSettings(final Stage mainStage) {

        CheckboxGroupController<Vaccine> checkboxGroupController = new CheckboxGroupController<>(
                "Vaccins recherchés :",
                Vaccine.values()
        );

        checkboxGroupController.getSelectedElements().addListener((SetChangeListener<? super Vaccine>) change -> {
            VAF.INSTANCE.searchedVaccines.clear();
            VAF.INSTANCE.searchedVaccines.addAll(change.getSet());
            VAF.logger.info(change.getSet().toString());
            VAF.logger.info(VAF.INSTANCE.searchedVaccines.toString());
        });

        TimeRangeController timeRangeController = new TimeRangeController("Plage de recherche :",
                VAF.INSTANCE.searchFromTime, VAF.INSTANCE.searchToTime);
        timeRangeController.fromTimeProperty().addListener((observable, oldValue, newValue) -> {
            VAF.logger.info("Set From to: " + newValue);
            VAF.INSTANCE.searchFromTime = newValue;
        });
        timeRangeController.toTimeProperty().addListener((observable, oldValue, newValue) -> {
            VAF.logger.info("Set To to: " + newValue);
            VAF.INSTANCE.searchToTime = newValue;
        });

        VBox layout = new VBox(
                timeRangeController, new Separator(),
                checkboxGroupController, new Separator()
        );

        Scene settingsScene = new Scene(layout);

        settingsStage.setTitle("Paramètres de recherche");
        settingsStage.setWidth(500);
        settingsStage.setHeight(300);
        settingsStage.setMinWidth(settingsStage.getWidth());
        settingsStage.setMinHeight(settingsStage.getHeight());

        settingsStage.setScene(settingsScene);
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.initOwner(mainStage);
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

    public void submitLocationSearch(final TextField locationSearchBar, final Button searchButton) {

        if (locationSearchBar.getText().isEmpty())
            return;

        VAF.INSTANCE.service.submit(() -> {
            locationSearchBar.setDisable(true);
            searchButton.setDisable(true);
            VAF.INSTANCE.centerSearcher.submitSearch(locationSearchBar.getText());
            locationSearchBar.setDisable(false);
            searchButton.setDisable(false);
            locationSearchBar.clear();
        });
    }

    public void start(final Stage mainStage) {

        Button searchSettingsButton = new Button("Paramètres de recherche");
        searchSettingsButton.setOnMouseClicked(event -> {
            settingsStage.show();
        });

        SearchBarController searchBar = new SearchBarController(
                "lieu de recherche...", "Exemple: \"Renne\", \"Paris 75015\"",
                (sb, text) -> {
                    VAF.INSTANCE.service.submit(() -> {
                        sb.setDisable(true);
                        VAF.INSTANCE.centerSearcher.submitSearch(text);
                        sb.setDisable(false);
                        sb.clearInput();
                    });
                }
        );
        searchBar.setPadding(new Insets(5));

        Button addCenterButton = new Button("Ajouter un centre manuellement");
        Tooltip addCenterManuallyTooltip = new Tooltip("Des difficultés avec l'ajout par lieu ? Ajouter un centre depuis une URL");
        addCenterButton.setTooltip(addCenterManuallyTooltip);
        addCenterManuallyTooltip.setShowDelay(Duration.millis(100));
        addCenterManuallyTooltip.setFont(Font.font("", 14));
        addCenterManuallyTooltip.setOnShowing(s -> {
            Bounds bounds = addCenterButton.localToScreen(addCenterButton.getBoundsInLocal());
            addCenterButton.getTooltip().setX(bounds.getMinX() - addCenterButton.getWidth() / 1.5);
            addCenterButton.getTooltip().setY(bounds.getMaxY());
        });
        addCenterButton.setOnMouseClicked(event -> {
            addCenterDialog(mainStage);
        });

        ToolBar toolBar = new ToolBar(
                searchBar, Utils.hSpacer(), addCenterButton, new Separator(), searchSettingsButton
        );

        ScrollPane scrollPane = new ScrollPane(scannerListView);
        scrollPane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        HBox centerListTitle = new HBox(new Text("Centres en cours de recherche :"));
        centerListTitle.setPadding(new Insets(10, 10, 5, 10));
        centerListTitle.setVisible(false);
        centerListTitle.setManaged(false);
        centerListTitle.managedProperty().bind(centerListTitle.visibleProperty());

        VBox layout = new VBox(toolBar, centerListTitle, scrollPane);
        scannerListView.prefWidthProperty().bind(layout.widthProperty());
        scannerListView.prefHeightProperty().bind(layout.heightProperty());

        Text infoPanelTitle = new Text("Impossible de trouver un rendez vous de vaccination ?\nVaccine Appointment Snatcher est là pour vous !");
        infoPanelTitle.setFont(Font.font("", 18));
        Text infoPanelContent = new Text(
                """
                        Ce logiciel va vous aider à mettre la main sur un rendez-vous de vaccination en le bloquant pour vous.
                        Une fois un rendez-vous trouvé, vous aurez une dizaine de minutes pour le récuperer avec votre compte
                        Doctolib dans le navigateur qui s'affichera à l'écran.
                                                
                        Pour le bon fonctionnement du logiciel, veuillez ne pas fermer la fenêtre de navigation qui s'ouvre
                        au démarage, c'est là que toute la magie se passe !
                                                
                        Vous pouvez commencer par paramétrer vos préférences de recherche dans "Paramètres de recherche".
                                                
                        Il vous suffira ensuite d'ajouter des centres à l'aide de la fonction de recherche par lieu (bar de recherche en haut à gauche).
                        """
        );
        infoPanelContent.setFont(Font.font("", 14));
        VBox infoPanel = new VBox(infoPanelTitle, infoPanelContent);
        infoPanel.setPadding(new Insets(10));
        infoPanel.setSpacing(10);
        infoPanel.setManaged(false);
        infoPanel.managedProperty().bind(infoPanel.visibleProperty());

        scannerListView.getChildren().add(infoPanel);

        scannerListView.getChildren().addListener((ListChangeListener<? super Node>) observable -> {
            boolean noScanners = observable.getList().size() == 1;
            infoPanel.setVisible(noScanners);
            centerListTitle.setVisible(!noScanners);
        });

        Scene scene = new Scene(layout);
        if (mainCss != null)
            scene.getRoot().getStylesheets().add(mainCss.toString());

        mainStage.setWidth(800);
        mainStage.setHeight(500);
        mainStage.setMinWidth(mainStage.getWidth());
        mainStage.setMinHeight(mainStage.getHeight());
        mainStage.setScene(scene);

        setupSearchSettings(mainStage);
    }
}
