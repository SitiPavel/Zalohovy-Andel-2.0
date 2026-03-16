package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;
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
                CompareOperations basicChildRowOperation = TreeTableUtility.getBaseOperation(childRowOperation);

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
            finalOperation = TreeTableUtility.convertBasicOnFolderOperation(folderOperation);
        }

        node.getValue().setOperation(finalOperation);
        return finalOperation;

    }

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

            CompareOperations currentChildBasicOper =
                    TreeTableUtility.getBaseOperation(child.getValue().getOperation());

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
            newParentOperation = TreeTableUtility.convertBasicOnFolderOperation(foundChildOper);
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
        if (!TreeTableUtility.cenBeOperationUsed(operationToSett, treeItem.getValue())) {
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
            currentItem.setOperation(TreeTableUtility.convertBasicOnFolderOperation(basicOper));
        } else {
            currentItem.setOperation(basicOper);
        }
        for (TreeItem<ComparedItemOperItemRowModel> child : treeItem.getChildren()) {
            applyOperationOnChyldrensRecursively(child, basicOper);
        }

    }



}
