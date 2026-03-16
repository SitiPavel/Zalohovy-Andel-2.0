package cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;

public class ChangeFolderOrFileOperationController {

    private Boolean isFolder= false;
    private ComparedItemOperItemRowModel rowItem;
    private TreeItem<ComparedItemOperItemRowModel> treeItem;

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
    ///  Nastavení tlačítek
    @FXML public void setOperAddLeft(ActionEvent event) {
        String buttonId = ((Node) event.getSource()).getId();
        if (isFolder) {
            rowItem.setOperation(FOLDER_ADD_LEFT);
        } else {
            rowItem.setOperation(ADD_LEFT);
        }
        close(event);
    }
    @FXML public void setOperAddRight(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_ADD_RIGHT);
        } else {
            rowItem.setOperation(ADD_RIGHT);
        }
        close(event);
    }
    @FXML public void setOperUpdateLeft(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_UPDATE_LEFT);
        } else {
            rowItem.setOperation(UPDATE_LEFT);
        }
        close(event);
    }
    @FXML public void setOperUpdateRight(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_UPDATE_RIGHT);
        } else {
            rowItem.setOperation(UPDATE_RIGHT);
        }
        close(event);
    }
    @FXML public void setOperDeleteLeft(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_DELETE_LEFT);
        } else {
            rowItem.setOperation(DELETE_LEFT);
        }
        close(event);
    }
    @FXML public void setOperDeleteBoth(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_DELETE_BOTH);
        } else {
            rowItem.setOperation(DELETE_BOTH);
        }
        close(event);
    }
    @FXML public void setOperDeleteRight(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_DELETE_RIGHT);
        } else {
            rowItem.setOperation(DELETE_RIGHT);
        }
        close(event);
    }
    @FXML public void setOperBlock(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_IGNORE);
        } else {
            rowItem.setOperation(IGNORE);
        }
        close(event);
    }
    @FXML public void setOperEqals(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_EQUALS);
        } else {
            rowItem.setOperation(EQUALS);
        }
        close(event);
    }
    @FXML public void setOperUnknow(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_UNKNOWN);
        } else {
            rowItem.setOperation(UNKNOWN);
        }
        close(event);
    }


    // univerzální pro všechny tlačítka
    public void setOperByBtnId(ActionEvent event) {
        String buttonId = ((Node) event.getSource()).getId();
        CompareOperations oper =  getOperation(buttonId, isFolder);

        if (oper != null) {

            if (oper.toString().contains("FOLDER")) {
                // pokud uživatel nastaví stejnou operaci
                if (oper == treeItem.getValue().getOperation()) {
                } // true když se dokáže na všechny děti (soubory/složky) nastavit vybraná operace
                else if (setAllChildrenOfFolder(treeItem, oper)) {
                    // má novou operaci proto je nutno nastavit všechny předky
                    TreeTableUtility.updateOperationForParentFolders(treeItem);
                } else {
                    ErrorUtility.showError_pointlessCompareOperation("Složka obsahuje soubor, " +
                            "na který nelze aplikovat tuto operaci! ");
                }
            } else {
                rowItem.setOperation(oper);
                TreeTableUtility.updateOperationForParentFolders(treeItem);
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

        if (!TreeTableUtility.validateOperationForChildrenRecursively(currentItem, basicOper)) {
            return false;
        }

        TreeTableUtility.applyOperationOnChyldrensRecursively(currentItem, basicOper);
        return true;
    }

    // podle id tlačítka nám tjistí kterou operaci chce uživatel nastavit
    private CompareOperations getOperation(String btnId, boolean isFolder) {
        CompareOperations operd;
        // rozlišujeme abychom našli operaci ke složce nebo k souboru (oba mají jiný switch)
        // a zkontrolovali zda operace může být přidělena
        if (isFolder) {
            operd = getOperationByBtnID_Folder(btnId);
            // převedeme jen pro kontrolu smysluplnosti operace
            CompareOperations basicOperation = TreeTableUtility.getBaseOperation(operd);
            if (TreeTableUtility.cenBeOperationUsed(operd,treeItem.getValue()) && operd != null) {
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

    ///  pomocné metody
    public void setFolder(Boolean folder) {
        isFolder = folder;
    }

    public void setComparedItemOperItemRowModel(TreeItem<ComparedItemOperItemRowModel> treeItem) {
        this.rowItem = treeItem.getValue();
        this.treeItem = treeItem;
        // nastavení aktivní operace do GUI
        Button activeButton = getActiveButton(rowItem.getOperation());
        activeButton.getStyleClass().add("modificator_save");
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
