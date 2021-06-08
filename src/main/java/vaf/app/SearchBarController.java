package vaf.app;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
import vaf.Main;

import java.io.InputStream;
import java.util.function.BiConsumer;

public class SearchBarController extends HBox {

    private static final InputStream searchIcon = Main.class.getResourceAsStream("search_icon_64.png");

    public ObjectProperty<Boolean> disableProperty = new SimpleObjectProperty<>(false);

    private final TextField textField;
    private final Tooltip tooltip;
    private final Button searchButton;

    public Boolean getDisableProperty() {
        return disableProperty.get();
    }

    public SearchBarController(final String promptMessage, final String tooltipMessage,
                               final BiConsumer<SearchBarController, String> onSearch) {

        textField = new TextField();

        tooltip = new Tooltip(tooltipMessage);
        textField.setTooltip(tooltip);
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setFont(Font.font("", 14));
        tooltip.setOnShowing(s -> {
            Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
            textField.getTooltip().setX(bounds.getMinX() - tooltip.getWidth() / 2);
            textField.getTooltip().setY(bounds.getMaxY());
        });

        searchButton = new Button();
        if (searchIcon != null) {
            ImageView searchImage = new ImageView(new Image(searchIcon));
            searchImage.setPreserveRatio(true);
            searchImage.setFitWidth(20);
            searchImage.setFitHeight(20);
            searchButton.setGraphic(searchImage);
        }

        textField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER) && !textField.getText().isEmpty())
                onSearch.accept(this, textField.getText());
        });
        searchButton.setOnAction(event -> {
            if (!textField.getText().isEmpty())
                onSearch.accept(this, textField.getText());
        });

        textField.disableProperty().bind(this.disableProperty);
        searchButton.disableProperty().bind(this.disableProperty);

        textField.setPromptText(promptMessage);

        this.getChildren().addAll(textField, searchButton);

        Platform.runLater(this::requestFocus); // Un-focus textField

        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(10);
    }

    public void clearInput() {
        textField.clear();
    }
}
