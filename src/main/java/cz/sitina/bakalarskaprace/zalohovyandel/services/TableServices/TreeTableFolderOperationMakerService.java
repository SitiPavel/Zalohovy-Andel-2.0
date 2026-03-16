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



}
