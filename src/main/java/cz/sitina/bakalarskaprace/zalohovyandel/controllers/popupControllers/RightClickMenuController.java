package cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.RightClickMenuItem;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class RightClickMenuController {

    @FXML
    private VBox menuItemKonteiner;

    public void setMenuItems(List<RightClickMenuItem> items, Stage stage) {
        menuItemKonteiner.getChildren().clear();
        menuItemKonteiner.setSpacing(4);

        for (RightClickMenuItem item : items) {
            Button btn = new Button(item.getText());
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.getStyleClass().add("rightClickMenuButton");

            if (item.getIcon() != null) {
                ImageView iconView = new ImageView(item.getIcon());
                iconView.setFitWidth(18);
                iconView.setFitHeight(18);
                iconView.setPreserveRatio(true);
                btn.setGraphic(iconView);
                btn.setGraphicTextGap(10);
            }

            btn.disableProperty().bind(item.disabledProperty());

            btn.setOnAction(e -> {
                stage.close();
                if (item.getAction() != null) {
                    item.getAction().run();
                }
            });

            menuItemKonteiner.getChildren().add(btn);
        }
    }
}
