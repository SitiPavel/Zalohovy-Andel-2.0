package cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFilterService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;

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
        CompareOperations oper =  getOperationToSett(buttonId, isFolder);

        if (oper != null) {

            if (oper.toString().contains("FOLDER")) {
                // pokud uživatel nastaví stejnou operaci
                if (oper == treeItem.getValue().getOperation()) {
                } // true když se dokáže na všechny děti (soubory/složky) nastavit vybraná operace
                else if (setAllChildrenOfFolder(treeItem, oper)) {
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



    private boolean setAllChildrenOfFolder(TreeItem<ComparedItemOperItemRowModel> currentItem,
                                                 CompareOperations operationToSett) {

        CompareOperations basicOper = TreeTableUtility.getBaseOperation(operationToSett);

        if (!TreeTableFolderOperationMakerService.validateOperationForChildrenRecursively(currentItem, basicOper)) {
            return false;
        }

        TreeTableFolderOperationMakerService.applyOperationOnChyldrensRecursively(currentItem, basicOper);
        return true;
    }

    // podle id tlačítka nám tjistí kterou operaci chce uživatel nastavit
    private CompareOperations getOperationToSett(String btnId, boolean isFolder) {
        CompareOperations operd;
        // rozlišujeme abychom našli operaci ke složce nebo k souboru (oba mají jiný switch)
        // a zkontrolovali zda operace může být přidělena
        if (isFolder) {
            operd = getOperationByBtnID_Folder(btnId);
            // převedeme jen pro kontrolu smysluplnosti operace
            CompareOperations basicOperation = TreeTableUtility.getBaseOperation(operd);
            if (TreeTableUtility.cenBeOperationUsed(basicOperation,treeItem.getValue())) {
                return operd;
            } else {
                return null;
            }

        } else {
            operd = getOperationByBtnID_File(btnId);
            if (TreeTableUtility.cenBeOperationUsed(operd,treeItem.getValue()) && operd != null ) {
                return operd;
            } else {
                return null;
            }

        }
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
                CompareOperations folderOper = getOperationByBtnID_Folder(btn.getId());
                operationForButton = TreeTableUtility.getBaseOperation(folderOper);
            } else {
                operationForButton = getOperationByBtnID_File(btn.getId());
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
        if (treeItem.getValue().getOperation() != FOLDER &&
                treeItem.getValue().getOperation() !=FOLDER_UNEVEN ) {
            activeButton.getStyleClass().add("modificator_save");
        }

        disableInvalidButtons();
    }

    public void setFolder(Boolean folder) {
        isFolder = folder;
    }

    public CompareOperations getOperationByBtnID_File(String btnId) {
        return switch (btnId) {
            case "btnSetOperAddLeft" -> ADD_LEFT;
            case "btnSetOperAddRight" -> ADD_RIGHT;
            case "btnSetOperUpdateLeft" -> UPDATE_LEFT;
            case "btnSetOperUpdateRight" -> UPDATE_RIGHT;
            case "btnSetOperDeleteLeft" -> DELETE_LEFT;
            case "btnSetOperDeleteBoth" -> DELETE_BOTH;
            case "btnSetOperDeleteRight" -> DELETE_RIGHT;
            case "btnSetOperBlock" -> IGNORE;
            case "btnSetOperEqals" -> EQUALS;
            case "btnSetOperUnknow" -> UNKNOWN;
            default -> null;
        };
    }

    public CompareOperations getOperationByBtnID_Folder(String btnId) {
        return switch (btnId) {
            case "btnSetOperAddLeft" -> FOLDER_ADD_LEFT;
            case "btnSetOperAddRight" -> FOLDER_ADD_RIGHT;
            case "btnSetOperUpdateLeft" -> FOLDER_UPDATE_LEFT;
            case "btnSetOperUpdateRight" -> FOLDER_UPDATE_RIGHT;
            case "btnSetOperDeleteLeft" -> FOLDER_DELETE_LEFT;
            case "btnSetOperDeleteBoth" -> FOLDER_DELETE_BOTH;
            case "btnSetOperDeleteRight" -> FOLDER_DELETE_RIGHT;
            case "btnSetOperBlock" -> FOLDER_IGNORE;
            case "btnSetOperEqals" -> FOLDER_EQUALS;
            case "btnSetOperUnknow" -> FOLDER_UNKNOWN;
            default -> null;
        };
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
