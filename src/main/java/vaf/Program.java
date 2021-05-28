package vaf;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vaf.app.App;
import vaf.scrapper.Browser;

import java.io.InputStream;

public class Program extends Application {

    private static final InputStream appIcon = Main.class.getResourceAsStream("icon.png");

    public void main(String... args) {

        // Start UI
        Application.launch(args);
    }

    private Scene createLoadingScene() {
        BorderPane p = new BorderPane();
        VBox vBox = new VBox(new ProgressBar(), new Text("Loading web drivers..."));
        vBox.setAlignment(Pos.CENTER);
        p.setCenter(vBox);
        return new Scene(p, 300, 150);
    }

    @Override
    public void start(Stage stage) {

        Browser.selectBrowser();

        if (appIcon != null)
            stage.getIcons().add(new Image(appIcon));
        stage.setTitle("VAS");

        stage.setScene(createLoadingScene());
        stage.setOnCloseRequest(windowEvent -> VAF.INSTANCE.shutdown());
        stage.show();

        // To initiate driver loading
        new Thread(() -> {

            VAF.INSTANCE.setup();

            Platform.runLater(() -> {
                App.INSTANCE.start(stage);
                stage.show();
            });

        }).start();
    }
}
