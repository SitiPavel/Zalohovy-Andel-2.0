package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
import javafx.scene.control.TreeItem;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;
import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.FOLDER_UNKNOWN;

public class PopupUtility {

    // podle id tlačítka nám tjistí kterou operaci chce uživatel nastavit
    public static CompareOperations getOperationToSett(String btnId, boolean isFolder,
                                                       ComparedItemOperItemRowModel item) {
        CompareOperations operd;
        // rozlišujeme abychom našli operaci ke složce nebo k souboru (oba mají jiný switch)
        // a zkontrolovali zda operace může být přidělena
        if (isFolder) {
            operd = getOperationByBtnID_Folder(btnId);
            // převedeme jen pro kontrolu smysluplnosti operace
            CompareOperations basicOperation = TreeTableUtility.getBaseOperation(operd);
            if (TreeTableUtility.cenBeOperationUsed(basicOperation,item)) {
                return operd;
            } else {
                return null;
            }

        } else {
            operd = getOperationByBtnID_File(btnId);
            if (TreeTableUtility.cenBeOperationUsed(operd,item) && operd != null ) {
                return operd;
            } else {
                return null;
            }

        }
    }

    public static boolean setAllChildrenOfFolder(TreeItem<ComparedItemOperItemRowModel> currentItem,
                                           CompareOperations operationToSett) {

        CompareOperations basicOper = TreeTableUtility.getBaseOperation(operationToSett);

        if (!TreeTableFolderOperationMakerService.validateOperationForChildrenRecursively(currentItem, basicOper)) {
            return false;
        }

        TreeTableFolderOperationMakerService.applyOperationOnChyldrensRecursively(currentItem, basicOper);
        return true;
    }

    public static CompareOperations getOperationByBtnID_File(String btnId) {
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

    public static CompareOperations getOperationByBtnID_Folder(String btnId) {
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
}
