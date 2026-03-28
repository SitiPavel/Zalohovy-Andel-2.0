package cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFilterService;
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
import javafx.stage.Stage;


public class ChangeFolderOrFileOperationController {
    // Proměnná pro uložení příkazu k přepořítání statistik z venčí
    private Runnable onUpdateSuccess;


    private Boolean isFolder= false;
    private ComparedItemOperItemRowModel rowItem;
    private TreeItem<ComparedItemOperItemRowModel> treeItem;

    @FXML private VBox leftButtonPanel;

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



    @FXML
    public void initialize() {

    }
    /// callBack
    public void setOnUpdateSuccess(Runnable onUpdateSuccess) {
        this.onUpdateSuccess = onUpdateSuccess;
    }

    // univerzální pro všechny tlačítka
    @FXML
    public void setOperByBtnId(ActionEvent event) {
        String buttonId = ((Node) event.getSource()).getId();
        CompareOperations oper =   PopupUtility.getOperationToSett(buttonId, isFolder, treeItem.getValue());

        if (oper != null) {

            if (oper.toString().contains("FOLDER")) {
                // pokud uživatel nastaví stejnou operaci
                if (oper == treeItem.getValue().getOperation()) {
                } // true když se dokáže na všechny děti (soubory/složky) nastavit vybraná operace
                else if (PopupUtility.setAllChildrenOfFolder(treeItem, oper)) {
                    // má novou operaci proto je nutno nastavit všechny předky
                    TreeTableFolderOperationMakerService.updateOperationForParentFolders(treeItem);
                } else {
                    ErrorUtility.showError_pointlessCompareOperation("Složka obsahuje soubor, " +
                            "na který nelze aplikovat tuto operaci! ");
                }


            } else {
                rowItem.setOperation(oper);
                TreeTableFolderOperationMakerService.updateOperationForParentFolders(treeItem);
            }
            // callback do kontroleru pro obnovu statistik
            if (onUpdateSuccess != null) {
                onUpdateSuccess.run();
            }

        } else {
            ErrorUtility.showError_pointlessCompareOperation("Zadaná operace nemůže být použita, " +
                "jestliže je na nějaké straně prázdný řádek. " +
                "(Na disku neexistuje soubor se stejným názvem)");
        }
        close(event);
    }




    private void disableInvalidButtons() {
        Button[] allButtons = {
                btnSetOperAddLeft, btnSetOperAddRight,
                btnSetOperUpdateLeft, btnSetOperUpdateRight,
                btnSetOperDeleteLeft, btnSetOperDeleteBoth, btnSetOperDeleteRight,
                btnSetOperBlock, btnSetOperEqals, btnSetOperUnknow
        };

        for (Button btn : allButtons) {
            // Přeskočíme tlačítka, která už mohla být skryta kvůli isBackupTask
            if (btn == null || !btn.isVisible()) {
                continue;
            }

            // Zjistíme, jakou operaci toto konkrétní tlačítko reprezentuje
            CompareOperations operationForButton;
            if (isFolder) {
                CompareOperations folderOper = PopupUtility.getOperationByBtnID_Folder(btn.getId());
                operationForButton = TreeTableUtility.getBaseOperation(folderOper);
            } else {
                operationForButton =  PopupUtility.getOperationByBtnID_File(btn.getId());
            }

            // Pokud metodu nelze spárovat s tlačítkem (nemělo by nastat), přeskočíme
            if (operationForButton == null) continue;

            // ZAVOLÁME TVOU METODU PRO KONTROLU LOGIKY
            boolean canBeUsed = TreeTableUtility.cenBeOperationUsed(operationForButton, rowItem);

            // Pokud operace nedává smysl, tlačítko deaktivujeme (zprůhlední se a nepůjde kliknout)
            if (!canBeUsed) {
                btn.setDisable(true);
            }
        }
    }




    public void setComparedItemOperItemRowModel(TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isBackupTask) {
        this.rowItem = treeItem.getValue();
        this.treeItem = treeItem;

        if (isBackupTask) {
            leftButtonPanel.setVisible(false);
            leftButtonPanel.setManaged(false);

            btnSetOperDeleteBoth.setVisible(false);
            btnSetOperDeleteBoth.setManaged(false);

            btnSetOperDeleteRight.setVisible(false);
            btnSetOperDeleteRight.setManaged(false);
        }

        // nastavení aktivní operace do GUI
        Button activeButton = getActiveButton(rowItem.getOperation());
        if (treeItem.getValue().getOperation() != CompareOperations.FOLDER &&
                treeItem.getValue().getOperation() != CompareOperations.FOLDER_UNEVEN ) {
            activeButton.getStyleClass().add("modificator_save");
        }

        disableInvalidButtons();
    }

    public void setFolder(Boolean folder) {
        isFolder = folder;
    }



    private Button getActiveButton(CompareOperations oper)  {
        return switch (oper) {
            case ADD_LEFT, FOLDER_ADD_LEFT-> btnSetOperAddLeft;
            case ADD_RIGHT, FOLDER_ADD_RIGHT-> btnSetOperAddRight;
            case UPDATE_LEFT, FOLDER_UPDATE_LEFT-> btnSetOperUpdateLeft;
            case UPDATE_RIGHT, FOLDER_UPDATE_RIGHT-> btnSetOperUpdateRight;
            case DELETE_LEFT, FOLDER_DELETE_LEFT-> btnSetOperDeleteLeft;
            case DELETE_BOTH, FOLDER_DELETE_BOTH -> btnSetOperDeleteBoth;
            case DELETE_RIGHT, FOLDER_DELETE_RIGHT-> btnSetOperDeleteRight;
            case IGNORE, FOLDER_IGNORE ->btnSetOperBlock;
            case EQUALS, FOLDER_EQUALS -> btnSetOperEqals;
            case UNKNOWN, FOLDER_UNKNOWN-> btnSetOperUnknow;
            default -> null;
        };
    }

    private void close(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
