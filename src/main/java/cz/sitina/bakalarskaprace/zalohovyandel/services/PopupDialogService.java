package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFileOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFolderOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFolderOrFileOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class PopupDialogService {

    public void showOperationPopupWindow(TreeItem<ComparedItemOperItemRowModel> treeItem) {
        String path = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/popup/";
        String fxmlPath;
        boolean isFolder = false;

        if (treeItem.getValue().getOperation().toString().contains("FOLDER")) {
            fxmlPath = path + "changeFolderOperation.fxml";
            isFolder = true;
        } else {
            fxmlPath = path + "changeFileOperation.fxml";
        }

        openDialog(fxmlPath, treeItem, isFolder);
    }

    private void openDialog(String fxmlPath, TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isFolder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            ChangeFolderOrFileOperationController controller = loader.getController();
            controller.setFolder(isFolder);
            controller.setComparedItemOperItemRowModel(treeItem);


            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            // při zněně focus (na jiné okno) se toto okno zaře
            stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    stage.close();
                }
            });
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání okna",
                    "Nepodařilo se načíst obsah vyskakovacího okna.",
                    e.getMessage());
        }
    }
}
