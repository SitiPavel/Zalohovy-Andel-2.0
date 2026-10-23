package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.RightClickMenuController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.RightClickMenuItem;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.List;

public class RightClickMenuService {

    private static final String POPUP_PATH = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/popup/rightClickMenu.fxml";
    private static final double TOLERANCE_PX = 10.0;

    /**
     * Zobrazí univerzální popup menu pro pravé tlačítko myši.
     *
     * @param items   Položky menu
     * @param screenX Globální X souřadnice kurzoru
     * @param screenY Globální Y souřadnice kurzoru
     */
    public void showRightClickMenu(List<RightClickMenuItem> items, double screenX, double screenY) {
        if (items == null || items.isEmpty()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(POPUP_PATH));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);

            RightClickMenuController controller = loader.getController();
            controller.setMenuItems(items, stage);

            Scene scene = new Scene(root);
            stage.setScene(scene);

            // Zavření při ztrátě fokusu
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    stage.close();
                }
            });

            // Sledování pohybu myši pro toleranci 10px při opuštění okna
            // 1. Lokální kontrola přes MouseEvent
            scene.addEventFilter(MouseEvent.MOUSE_EXITED, event -> {
                checkMouseBoundsAndClose(stage);
            });

            scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
                double mouseX = event.getSceneX();
                double mouseY = event.getSceneY();
                double width = scene.getWidth();
                double height = scene.getHeight();

                if (mouseX < -TOLERANCE_PX || mouseX > width + TOLERANCE_PX
                        || mouseY < -TOLERANCE_PX || mouseY > height + TOLERANCE_PX) {
                    stage.close();
                }
            });

            // Zobrazení a nastavení přesné pozice:
            // "střed horního okraje se dotýká špičky kurzoru a 10px nad kurzorem"
            // -> X = screenX - width / 2
            // -> Y = screenY - 10
            stage.setOnShown(event -> {
                Platform.runLater(() -> {
                    double width = stage.getWidth();
                    stage.setX(screenX - (width / 2.0));
                    stage.setY(screenY - 10.0);
                });
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání menu",
                    "Nepodařilo se zobrazit kontextové menu.",
                    e.getMessage());
        }
    }

    private void checkMouseBoundsAndClose(Stage stage) {
        try {
            Point p = MouseInfo.getPointerInfo().getLocation();
            double cursorX = p.getX();
            double cursorY = p.getY();

            double minX = stage.getX() - TOLERANCE_PX;
            double maxX = stage.getX() + stage.getWidth() + TOLERANCE_PX;
            double minY = stage.getY() - TOLERANCE_PX;
            double maxY = stage.getY() + stage.getHeight() + TOLERANCE_PX;

            if (cursorX < minX || cursorX > maxX || cursorY < minY || cursorY > maxY) {
                stage.close();
            }
        } catch (Exception e) {
            stage.close();
        }
    }
}
