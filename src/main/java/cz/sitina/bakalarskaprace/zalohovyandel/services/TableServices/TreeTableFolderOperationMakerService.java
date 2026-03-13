package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.scene.control.TreeItem;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;

public class TreeTableFolderOperationMakerService {

    // rekurzivní metoda pro vyhodnocení operací potomků složky, podle schod operací potomků nastavíme operaci
    // pro složku
    public static CompareOperations getFolderOperation(TreeItem<ComparedItemOperItemRowModel> node) {

        CompareOperations finalOperation = null;

        // pokud nemá operaci obsahující FOLDER tak je to soubor a vracíme operaci
        if (!node.getValue().getOperation().name().contains("FOLDER")) {
            return node.getValue().getOperation();
        }

        CompareOperations folderOperation = null;
        boolean isUneven = false;


        // procházíme všechny děti složky a nacházíme stejné operace
        for (TreeItem<ComparedItemOperItemRowModel> child : node.getChildren()) {

            CompareOperations childRowOperation = getFolderOperation(child);


            if (!isUneven) {
                CompareOperations basicChildRowOperation = getBaseOperation(childRowOperation);

                if (basicChildRowOperation == CompareOperations.FOLDER_UNEVEN) {
                    isUneven = true;
                }
                if (folderOperation == null) {
                    folderOperation = basicChildRowOperation ;
                } else if (basicChildRowOperation !=  folderOperation) {
                    isUneven = true;
                }
            }
        }

        // rozhodnutí
        if (isUneven) {
            finalOperation = CompareOperations.FOLDER_UNEVEN;
        } else if (folderOperation == null) {
            finalOperation = FOLDER_UNKNOWN;
        } else  {
            finalOperation = convertBasicOnFolderOperation(folderOperation);
        }

        node.getValue().setOperation(finalOperation);
        return finalOperation;

    }

    private static CompareOperations convertBasicOnFolderOperation(CompareOperations operation) {
        return switch (operation) {
            case EQUALS -> FOLDER_EQUALS;
            case IGNORE -> FOLDER_IGNORE;
            case DELETE_LEFT -> FOLDER_DELETE_LEFT;
            case DELETE_RIGHT -> FOLDER_DELETE_RIGHT;
            case DELETE_BOTH -> FOLDER_DELETE_BOTH;
            case UPDATE_LEFT -> FOLDER_UPDATE_LEFT;
            case UPDATE_RIGHT -> FOLDER_UPDATE_RIGHT;
            case ADD_LEFT -> FOLDER_ADD_LEFT;
            case ADD_RIGHT -> FOLDER_ADD_RIGHT;
            case UNKNOWN -> FOLDER_UNKNOWN;
            default -> operation;
        };
    }

    // záskáme základní operaci ze složek abychom pohly správně porovnávat
    private static CompareOperations getBaseOperation(CompareOperations op) {
        return switch (op) {
            case FOLDER_EQUALS -> CompareOperations.EQUALS;
            case FOLDER_DELETE_LEFT -> CompareOperations.DELETE_LEFT;
            case FOLDER_DELETE_RIGHT -> CompareOperations.DELETE_RIGHT;
            case FOLDER_DELETE_BOTH -> CompareOperations.DELETE_BOTH;
            case FOLDER_UPDATE_LEFT -> CompareOperations.UPDATE_LEFT;
            case FOLDER_UPDATE_RIGHT -> CompareOperations.UPDATE_RIGHT;
            case FOLDER_ADD_LEFT -> CompareOperations.ADD_LEFT;
            case FOLDER_ADD_RIGHT -> CompareOperations.ADD_RIGHT;
            case FOLDER_UNKNOWN -> CompareOperations.UNKNOWN;
            case FOLDER_IGNORE -> CompareOperations.IGNORE;
            default -> op;
        };
    }

}
