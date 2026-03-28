package cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.PopupUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChangeGroupOperationController {
    @FXML
    private VBox leftButtonPanel;

    @FXML private Button btnSetOperAddLeft;
    @FXML private Button btnSetOperAddRight;
    @FXML private Button btnSetOperUpdateLeft;
    @FXML private Button btnSetOperUpdateRight;
    @FXML private Button btnSetOperDeleteLeft;
    @FXML private Button btnSetOperDeleteBoth;
    @FXML private Button btnSetOperDeleteRight;
    @FXML private Button btnSetOperBlock;
    @FXML private Button btnSetOperEqals;
    @FXML private Button btnSetOperUnknow;

    private List<TreeItem<ComparedItemOperItemRowModel>> selectedItems;
    private Runnable isAllOperChanged;
    private boolean isBackupTask;

    @FXML
    public void initialize() {

    }

    @FXML
    private void setOperByBtnId(ActionEvent event) {

        String buttonId = ((Node) event.getSource()).getId();
        CompareOperations oper = PopupUtility.getOperationByBtnID_Folder(buttonId);
        boolean isFileError = false;
        boolean isFolderError = false;
        String errorMassage ="";

        for (TreeItem<ComparedItemOperItemRowModel> treeItem : selectedItems) {
            if (oper.toString().contains("FOLDER")) {
                // pokud uživatel nastaví stejnou operaci
                if (oper == treeItem.getValue().getOperation()) {
                } // true když se dokáže na všechny děti (soubory/složky) nastavit vybraná operace
                else if (PopupUtility.setAllChildrenOfFolder(treeItem, oper)) {
                    // má novou operaci proto je nutno nastavit všechny předky
                    TreeTableFolderOperationMakerService.updateOperationForParentFolders(treeItem);
                } else {
                    isFolderError = true;
                }


            } else {
                if (TreeTableUtility.cenBeOperationUsed(oper, treeItem.getValue())) {
                    treeItem.getValue().setOperation(oper);
                } else {
                    isFileError = true;
                }
            }
        }
        if (isFileError) {
            errorMassage = errorMassage +"Některý/některé nebylo možné použít zvolenou operaci! \n";
        } if (isFolderError) {
            errorMassage = errorMassage +"Na některou/některé složky nebylo možné použít zvolenou operaci, " +
                    "nejspíše obsahuje soubory které nemohou mít tuto operaci. \n";
        }
        if (isFileError || isFolderError) {
            ErrorUtility.showError_pointlessCompareOperation("errorMassage");
        }
        isAllOperChanged.run();

    }

    public void setIsBackupTask(boolean backupTask) {
        isBackupTask = backupTask;

        if (isBackupTask) {
            leftButtonPanel.setVisible(false);
            leftButtonPanel.setManaged(false);

            btnSetOperDeleteBoth.setVisible(false);
            btnSetOperDeleteBoth.setManaged(false);

            btnSetOperDeleteRight.setVisible(false);
            btnSetOperDeleteRight.setManaged(false);
        }
    }

    public void setIsAllOperChanged(Runnable isAllOperChanged) {
        this.isAllOperChanged = isAllOperChanged;
    }

    public void setSelectedItems(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems) {
        this.selectedItems = selectedItems;
    }
}
