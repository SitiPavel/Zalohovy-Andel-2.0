package cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;

import java.util.*;

public class BiDirectionalStrategy implements ComparationStrategy {


    @Override
    public CompareOperations compare(FileItemModel leftItem, FileItemModel rightItem) {


        if (leftItem != null && rightItem != null) {/// stejná cesta,

            /// velikost a poslední modifikace = jsou identické
            if (leftItem.getLastModified() == rightItem.getLastModified() &&
                    leftItem.getFileSize() == rightItem.getFileSize()) {
                return CompareOperations.EQUALS;
            }
            /// Modifikace(long) v levo je větší = uktualizace do prava
            else if (leftItem.getLastModified() > rightItem.getLastModified()) {
                return CompareOperations.UPDATE_RIGHT;
            }
            /// Modifikace(long) v pravo je větší = uktualizace do leva
            else if (leftItem.getLastModified() < rightItem.getLastModified()) {
                return CompareOperations.UPDATE_LEFT;
            }
            /// velikosti jsou odličné ale modifikace stejná = nevíme
            else if (leftItem.getFileSize() != rightItem.getFileSize() &&
                    leftItem.getLastModified() == rightItem.getLastModified()) {
                return CompareOperations.UNKNOWN;
            }

        } else { /// cesty se neshodují,
            /// v levo není objekt
            if (leftItem == null && rightItem != null) {
                return CompareOperations.ADD_LEFT;
            }
            else if (leftItem != null) { /// v pravo není objekt
                return CompareOperations.ADD_RIGHT;
            }
        }
        return CompareOperations.UNKNOWN; /// pokud b vznikl neočekávaný scénář
    }
}
