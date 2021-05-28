package vaf.app;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class Utils {

    public static Pane vSpacer() {
        final Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.SOMETIMES);
        return spacer;
    }

    public static Pane hSpacer() {
        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        return spacer;
    }

    public static Pane spacer(int space) {
        final Pane spacer = new Pane();
        spacer.setMinWidth(space);
        return spacer;
    }
}
