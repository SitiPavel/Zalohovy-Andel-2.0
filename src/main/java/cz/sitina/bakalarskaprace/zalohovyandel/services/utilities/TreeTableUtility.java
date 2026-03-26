package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.scene.control.TreeItem;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;
import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.FOLDER_UNKNOWN;

public class TreeTableUtility {





    // kontroluje zda operace může být použita na řádek souboru
    public static boolean cenBeOperationUsed(CompareOperations oper, ComparedItemOperItemRowModel rowItem) {

        boolean existLeft =  !rowItem.getLeftItem().getFileName().isEmpty();
        boolean existRight =  !rowItem.getRightItem().getFileName().isEmpty();

        // oba dva existují    1 <-> 1
        if (existLeft && existRight) {
            return true;
        }

        // existuje jen v pravo  0 -> 1
        else if (!existLeft && existRight) {
            if ( oper.equals(ADD_LEFT) ||
                    oper.equals(DELETE_RIGHT) ||
                    oper.equals(IGNORE) ||
                    oper.equals(EQUALS) ||
                    oper.equals(UNKNOWN)) {
                return true;
            }
            return false;

        }
        // existuje jen v levo  1 <- 0
        else if (existLeft && !existRight) {
            if (oper.equals(ADD_RIGHT) ||
                    oper.equals(DELETE_LEFT) ||
                    oper.equals(IGNORE) ||
                    oper.equals(EQUALS) ||
                    oper.equals(UNKNOWN)) {
                return true;
            }
            return false;
        }
        //neexistuje ani jeden 0 - 0    nebo jiný případ
        else {
            return false;
        }
    }
    public static CompareOperations convertBasicOnFolderOperation(CompareOperations operation) {
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
    public static CompareOperations getBaseOperation(CompareOperations op) {
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

    public static void readStatisticForOperationBackupSize(TreeItem<ComparedItemOperItemRowModel> treeItem,
                                        ComparedDataAndStatisticsForCompereOverview dataModel) {

        if (treeItem == null || treeItem.getValue() == null) {
            return;
        }
        ComparedItemOperItemRowModel rowItem = treeItem.getValue();
        boolean isFolder = rowItem.getOperation().toString().contains("FOLDER");

        if (!isFolder) {
            countFiltersAndSizePie(rowItem, dataModel);
        }

        if (!treeItem.isLeaf()) {
            for (TreeItem<ComparedItemOperItemRowModel> child : treeItem.getChildren()) {
                readStatisticForOperationBackupSize(child, dataModel);
            }
        }
    }

    // do existujícího objektu Data jen upraví počty operaci a upraví velikost zálohy na daný disk
    public static void countFiltersAndSizePie(ComparedItemOperItemRowModel rowItem,
                                                ComparedDataAndStatisticsForCompereOverview dataModel) {
        switch (rowItem.getOperation()) {
            case EQUALS -> dataModel.attributeEqualsCount(1);
            case IGNORE -> dataModel.attributeIgnoreCount(1);
            case DELETE_LEFT -> dataModel.attributeLeftDeleteCount(1);
            case DELETE_RIGHT -> dataModel.attributeRightDeleteCount(1);
            case DELETE_BOTH -> {
                dataModel.attributeLeftDeleteCount(1);
                dataModel.attributeRightDeleteCount(1);
            }
            case UPDATE_LEFT -> {
                dataModel.attributeLeftUpdateCount(1);
                dataModel.attributeDataTransportSize_left(rowItem.getRightItem().getFileSize());
            }
            case UPDATE_RIGHT -> {
                dataModel.attributeRightUpdateCount(1);
                dataModel.attributeDataTransportSize_right(rowItem.getLeftItem().getFileSize());
            }
            case ADD_LEFT -> {
                dataModel.attributeLeftAddCount(1);
                dataModel.attributeDataTransportSize_left(rowItem.getRightItem().getFileSize());
            }
            case ADD_RIGHT -> {
                dataModel.attributeRightAddCount(1);
                dataModel.attributeDataTransportSize_right(rowItem.getLeftItem().getFileSize());
            }
            case UNKNOWN -> dataModel.attributeUnknownCount(1);
        }
    }
}
