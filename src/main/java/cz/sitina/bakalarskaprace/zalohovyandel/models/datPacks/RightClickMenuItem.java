package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class RightClickMenuItem {
    private final StringProperty text = new SimpleStringProperty();
    private Image icon;
    private Runnable action;
    private final BooleanProperty disabled = new SimpleBooleanProperty(false);

    public RightClickMenuItem(String text, Image icon, Runnable action) {
        this.text.set(text);
        this.icon = icon;
        this.action = action;
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public Runnable getAction() {
        return action;
    }

    public void setAction(Runnable action) {
        this.action = action;
    }

    public boolean isDisabled() {
        return disabled.get();
    }

    public BooleanProperty disabledProperty() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled.set(disabled);
    }
}
