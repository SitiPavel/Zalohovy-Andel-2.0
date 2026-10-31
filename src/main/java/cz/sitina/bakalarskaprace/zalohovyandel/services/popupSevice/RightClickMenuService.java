package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.RightClickMenuController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.RightClickMenuItem;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class RightClickMenuService {

    private static final String POPUP_PATH = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/popup/rightClickMenu.fxml";

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

            // OPRAVENO: Použití sjednoceného mechanismu pro ořez dle monitoru, pozicování a sledování kurzoru
            PopupDialogService.setupPopupStage(stage, screenX, screenY);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání menu",
                    "Nepodařilo se zobrazit kontextové menu.",
                    e.getMessage());
        }
    }
}
