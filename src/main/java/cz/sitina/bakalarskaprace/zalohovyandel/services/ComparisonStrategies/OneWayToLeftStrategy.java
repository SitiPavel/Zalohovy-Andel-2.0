package cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;

public class OneWayToLeftStrategy implements ComparationStrategy {

    @Override
    public CompareOperations compare(FileItemModel leftItem, FileItemModel rightItem) {

        /// stejná cesta,
        if (leftItem != null && rightItem != null) {
            /// velikost a poslední modifikace = jsou identické
            if (leftItem.getLastModified() == rightItem.getLastModified() &&
                    leftItem.getFileSize() == rightItem.getFileSize()) {
                return CompareOperations.EQUALS;
            }
            /// Modifikace(long) nebo velikost v pravo je rozdílná = uktualizace do leva
            else if (leftItem.getLastModified() != rightItem.getLastModified() ||
                             leftItem.getFileSize() != rightItem.getFileSize() ) {
                return CompareOperations.UPDATE_LEFT;
            }

        /// cesty se neshodují,
        } else {
        /// v levo není objekt
            if (leftItem != null) {
                return CompareOperations.IGNORE;
            } else {
                return CompareOperations.ADD_LEFT;

            }
        }
        return CompareOperations.UNKNOWN; /// pokud b vznikl neočekávaný scénář
    }
}
