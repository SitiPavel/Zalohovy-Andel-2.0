package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFolderOrFileOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeGroupOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class PopupDialogService {
    String popupPath = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/popup/";

    public void showOperationPopupWindow(TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isBackupTask,
                                         Runnable onSuccess) {

        String fxmlPath;
        boolean isFolder = false;



        if (treeItem.getValue().getOperation().toString().contains("FOLDER")) {
            fxmlPath = popupPath + "changeFolderOperation.fxml";
            isFolder = true;
        } else {
            fxmlPath = popupPath + "changeFileOperation.fxml";
        }

        openDialog(fxmlPath, treeItem, isFolder,isBackupTask, onSuccess);
    }

    public void showOperationPopupWindow_group(List<TreeItem<ComparedItemOperItemRowModel>>selectedItems,
                                               boolean isBackupTask, Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(popupPath+"changeGroupOperation.fxml"));
            Parent root = loader.load();
            ChangeGroupOperationController controller = loader.getController();
            controller.setSelectedItems(selectedItems);
            controller.setIsAllOperChanged(onSuccess);
            controller.setIsBackupTask(isBackupTask);


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

    private void openDialog(String fxmlPath, TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isFolder,
                            boolean isBackupTask,
                            Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            ChangeFolderOrFileOperationController controller = loader.getController();
            controller.setFolder(isFolder);
            controller.setComparedItemOperItemRowModel(treeItem, isBackupTask);
            controller.setOnUpdateSuccess(onSuccess);


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
