package vaf.app;

import javafx.scene.Node;

public class Button extends javafx.scene.control.Button {
    public Button() {
        setFocusTraversable(false);
    }

    public Button(String s) {
        super(s);
        setFocusTraversable(false);
    }

    public Button(String s, Node node) {
        super(s, node);
        setFocusTraversable(false);
    }
}