package vaf.app;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ToggleSwitch extends StackPane {

    private final SimpleBooleanProperty switchedOn = new SimpleBooleanProperty(false);

    private final Rectangle back = new Rectangle(30, 15);
    private final Button button = new Button();
    private static final String buttonStyleOff = "-fx-background-color: #C81927;";
    private static final String buttonStyleOn = "-fx-background-color: #00893d;";

    public SimpleBooleanProperty switchOnProperty() {
        return switchedOn;
    }

    public ToggleSwitch() {
        getChildren().addAll(back, button);
        back.setArcHeight(back.getHeight());
        back.setArcWidth(back.getHeight());
        button.setShape(new Circle(back.getHeight() / 2));
        button.minHeightProperty().bind(back.heightProperty());
        button.minWidthProperty().bind(back.heightProperty());
        button.maxHeightProperty().bind(back.heightProperty());
        button.maxWidthProperty().bind(back.heightProperty());
        setAlignment(button, Pos.CENTER_LEFT);
        button.setStyle(buttonStyleOff);

        toggle(false);

        switchedOn.addListener((a, b, c) -> toggle(c));

        EventHandler<Event> click = e -> {
            toggle(switchedOn.get());
            switchedOn.set(!switchedOn.get());
        };

        setOnMouseClicked(click);
        button.setOnMouseClicked(click);

        button.setFocusTraversable(false);
    }

    private void toggle(final boolean state) {
        if (!state) {
            button.setStyle(buttonStyleOff);
            back.setFill(Color.valueOf("#ED6E78"));
            setAlignment(button, Pos.CENTER_LEFT);
        } else {
            button.setStyle(buttonStyleOn);
            back.setFill(Color.valueOf("#80C49E"));
            setAlignment(button, Pos.CENTER_RIGHT);
        }
    }
}