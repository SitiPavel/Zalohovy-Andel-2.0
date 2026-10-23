package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.RightClickMenuController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.RightClickMenuItem;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

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

            // OPRAVENO: Aktivní periodické sledování kurzoru myši přes Timeline (řeší selhání MOUSE_EXITED při toleranci 10px)
            Timeline mouseTracker = new Timeline(new KeyFrame(Duration.millis(50), event -> {
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
            }));
            mouseTracker.setCycleCount(Animation.INDEFINITE);

            // OPRAVENO: Spuštění trackeru až po korektním napozicování okna
            stage.setOnShown(event -> {
                Platform.runLater(() -> {
                    double width = stage.getWidth();
                    stage.setX(screenX - (width / 2.0));
                    stage.setY(screenY - 10.0);
                    mouseTracker.play();
                });
            });

            // OPRAVENO: Zastavení trackeru při zavření okna nebo ztrátě fokusu (prevence memory leaků)
            stage.setOnHidden(event -> mouseTracker.stop());

            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    mouseTracker.stop();
                    stage.close();
                }
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání menu",
                    "Nepodařilo se zobrazit kontextové menu.",
                    e.getMessage());
        }
    }
}
