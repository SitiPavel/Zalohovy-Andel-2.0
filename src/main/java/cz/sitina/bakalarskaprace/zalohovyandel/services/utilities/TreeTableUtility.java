package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.scene.control.TreeItem;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;
import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.FOLDER_UNKNOWN;

public class TreeTableUtility {

    /** po aktualizaci operace nějakého souboru (rekurze)
     * najde mi rodičovský adresář záská @originalOperation
     * porovná potomky a když je více jak 1 operace tak složka bude FOLDER_UNEVEN
     * pokud budou všechny operace stejné pouze (jedna operace = EQUALS) -> skožka dostane tuto operaci (FOLDER_EQUALS)
     * po vyhodnocení techto potomků se porovná nová operace se starou
        - pokud se rovná tak je rekurze je zastavena
        - pokud se nerovná tka tak se na předka zavolá rekurze kde vzve předPředka a projde děti a přidělí ji operaci
     */
    public static void updateOperationForParentFolders(
            TreeItem<ComparedItemOperItemRowModel> currentItem) {
        if (currentItem == null) {return;}

        // nastavení rodiče
        TreeItem<ComparedItemOperItemRowModel> parent = currentItem.getParent();
        if (parent == null || parent.getValue() == null) {
            return;
        }


        CompareOperations parentOriginalOperation = parent.getValue().getOperation();

        boolean isUneven = false;
        CompareOperations foundChildOper = null;

        // projití všech potomků
        for (TreeItem<ComparedItemOperItemRowModel> child : parent.getChildren()) {
            if (child.getValue() == null) continue;

            CompareOperations currentChildBasicOper = getBaseOperation(child.getValue().getOperation());

            if (foundChildOper == null) {
                foundChildOper = currentChildBasicOper;
            }else if (foundChildOper != currentChildBasicOper) {
                isUneven = true;
                break;
            }
        }

        /// vyhodnocení našeho procházení
        CompareOperations newParentOperation;
        //kdyby rodič neměl potomky nebo by byly potomci prázdný
        if (foundChildOper == null) {
            return;
        } else if (isUneven) { // více jak jedena operace
            newParentOperation = FOLDER_UNEVEN;
        } else { // všechy operace jsou stejné
            newParentOperation = convertBasicOnFolderOperation(foundChildOper);
        }

        // pokud se operace nerovnají je potřeba nastavit předpředka
        if (newParentOperation != parentOriginalOperation) {
            parent.getValue().setOperation(newParentOperation);
            updateOperationForParentFolders(parent);
        }
    }

    /** pro nastavení operace všech potomků
     * získáme prvek stromu a operaci kterou mu chceme přidělit potomkům
     * postupně procházíme děti a zkontrolujeme pomocí cenBeOperationUsed
     * pokud true tak chceme nastavit tomuto potomkovy aperaci
     * když ne tak bych chtěl dát vědět že to nejde volající metodě (toto je takové divné, v této rekurzi to asi nesedí)
     * když se objeví potomek s dalšími potomky, zavolám na něj rekurzi
     *
     * cílem je všem potomkům(složkám i souborum) nastavit operaci kterou obdržíme na  vstupu metody
     *  přidělená operace musí odpovídat tomu prvku: složka = operace pro složku, soubor = operace pro soubor
     *  pokud by někde cenBeOperationUsed vyšlo jako false tak to celé musí vyhodit chybu a nic nesmí být změněno
     *  proto mě napadlo že udělám kopii currentItem a budu upravovat kopii a pokud bude uspěch celé rekurze tak se
     *  currentItem větev nahradí tou kopií aby bylo zapsáno
     *  myslím že je to celé kostrbaté
     */

    // kontrola zda je možné aplikovat operaci na všechny potomky
    public static boolean validateOperationForChildrenRecursively(TreeItem<ComparedItemOperItemRowModel> treeItem,
                                          CompareOperations operationToSett){
        // nalezne že soubor nemůže tuto operaci použít
        if (!cenBeOperationUsed(operationToSett, treeItem.getValue())) {
            return false;
        }

        for (TreeItem<ComparedItemOperItemRowModel> child : treeItem.getChildren()) {
            if (!validateOperationForChildrenRecursively(child, operationToSett)) {
                return false;
            }
        }
        return true;
    }
    // aplikuje operaci na všechny potomky
    public static void applyOperationOnChyldrensRecursively(TreeItem<ComparedItemOperItemRowModel> treeItem,
                                                      CompareOperations basicOper) {
        ComparedItemOperItemRowModel currentItem = treeItem.getValue();

        boolean isFolder = currentItem.getOperation().toString().contains("FOLDER");

        if (isFolder) {
            currentItem.setOperation(convertBasicOnFolderOperation(basicOper));
        } else {
            currentItem.setOperation(basicOper);
        }
        for (TreeItem<ComparedItemOperItemRowModel> child : treeItem.getChildren()) {
            applyOperationOnChyldrensRecursively(child, basicOper);
        }

    }



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
}
