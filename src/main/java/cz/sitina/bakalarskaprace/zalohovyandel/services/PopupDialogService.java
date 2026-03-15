package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFileOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFolderOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFolderOrFileOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class PopupDialogService {

    public void showOperationPopupWindow(ComparedItemOperItemRowModel rowItem) {
        String path = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/popup/";
        String fxmlPath;
        boolean isFolder = false;

        if (rowItem.getOperation().toString().contains("FOLDER")) {
            fxmlPath = path + "changeFolderOperation.fxml";
            isFolder = true;
        } else {
            fxmlPath = path + "changeFileOperation.fxml";
        }

        openDialog(fxmlPath, rowItem, isFolder);
    }

    private void openDialog(String fxmlPath, ComparedItemOperItemRowModel rowItem, boolean isFolder) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            ChangeFolderOrFileOperationController controller = loader.getController();
            controller.setFolder(isFolder);
            controller.setComparedItemOperItemRowModel(rowItem);


            Stage stage = new Stage();
            stage.setTitle("Nastavení operace");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání okna",
                    "Nepodařilo se načíst obsah vyskakovacího okna.",
                    e.getMessage());
        }
    }
}
