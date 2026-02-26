package cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;

public class MirroringToRightStrategy implements ComparationStrategy {


    @Override
    public CompareOperations compare(FileItemModel leftItem, FileItemModel rightItem) {
        /// stejná cesta,
        if (leftItem != null && rightItem != null) {
            /// velikost a poslední modifikace = jsou identické
            if (leftItem.getLastModified() == rightItem.getLastModified() &&
                    leftItem.getFileSize() == rightItem.getFileSize()) {
                return CompareOperations.EQUALS;
            }
            /// Modifikace(long) nebo velikost v levo je rozdílná = uktualizace do prava
            else if (leftItem.getLastModified() != rightItem.getLastModified() ||
                    leftItem.getFileSize() != rightItem.getFileSize() ) {
                return CompareOperations.UPDATE_RIGHT;
            }

            /// cesty se neshodují,
        } else {
            // v pravo není objekt
            if (leftItem != null) {
                return CompareOperations.ADD_RIGHT;
            } else  {// v levo není objekt
                return CompareOperations.DELETE_RIGHT;
            }
        }
        return CompareOperations.UNKNOWN; /// pokud b vznikl neočekávaný scénář
    }
}
