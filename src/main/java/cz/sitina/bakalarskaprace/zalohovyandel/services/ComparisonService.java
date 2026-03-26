package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;

public class ComparisonService {
    private ComparedItemOperItemRowModel rowModel;
    private ComparedDataAndStatisticsForCompereOverview dataModel;

    private final BiDirectionalStrategy biDirectionalStrategy = new BiDirectionalStrategy();
    private final OneWayToRightStrategy oneWayToRightStrategy = new OneWayToRightStrategy();
    private final OneWayToLeftStrategy oneWayToLeftStrategy = new OneWayToLeftStrategy();
    private final MirroringToRightStrategy mirroringToRightStrategy = new MirroringToRightStrategy();
    private final MirroringToLeftStrategy mirroringToLeftStrategy = new MirroringToLeftStrategy();


    public ComparedDataAndStatisticsForCompereOverview compareFolders(
            Map<String, FileItemModel> leftFilesMap,
            Map<String, FileItemModel> rightFilesMap,
            TaskModel taskModel,
            BiConsumer<Long, Long> progressCallback,
            Supplier<Boolean> cancelCheck)
    {
        List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
        dataModel = new ComparedDataAndStatisticsForCompereOverview();

        // Sjednotíme klíče z leva a zprava, odstraníme duplicity (objekty stále zůstávají, nic se nemaže)
        Set<String> allKeys = new HashSet<>(leftFilesMap.keySet());
        allKeys.addAll(rightFilesMap.keySet());

        // Seřazení podle abecedy = důležité pro UI
        List<String> sortedKeys =  new ArrayList<>(allKeys);
        Collections.sort(sortedKeys);

        CompareOperations operation;

        // proměná pro počítadlo
        long totalFiles = allKeys.size();
        long comparedFiles = 0;

        // Průchod listu
        for (String relativePath : sortedKeys) {

            // zda byl proces zrušen
            if (cancelCheck != null  &&cancelCheck.get()) {
                System.out.println("Porovnávání přerušeno uživatelem.");
                break;
            }

            comparedFiles++; // venku aby počítal i pro automatické zálohy: možné log soubory do hystorie
            if (progressCallback != null) {
                progressCallback.accept(comparedFiles, totalFiles);
            }

            FileItemModel leftItem = leftFilesMap.get(relativePath);
            FileItemModel rightItem = rightFilesMap.get(relativePath);

            Path relativePathObject = Paths.get(relativePath);
            // zjisštění zda je v seznamu blokovaných = nutno porovnat cesty aby nebyly naopak lomítka / a \
            Boolean isBlocked = taskModel.getBlockSet().stream()
                    .anyMatch(blockedStr -> relativePathObject.startsWith(Path.of(blockedStr)));

            if (isBlocked) {
                operation = CompareOperations.IGNORE;

            /// porovná jaká strategie se použije a vratí nám operaci pro tento řídek souboru
            }else if (taskModel instanceof ManualSyncTask MSC) {
                switch (MSC.getCompareStrategy()) {
                    case BIDIRECTIONAL_SYNC ->
                        operation = biDirectionalStrategy.compare(leftItem,rightItem);
                    case BACKUP_TO_RIGHT ->
                        operation = oneWayToRightStrategy.compare(leftItem,rightItem);
                    case BACKUP_TO_LEFT ->
                        operation = oneWayToLeftStrategy.compare(leftItem, rightItem);
                    case MIRRORING_to_RIGHT ->
                        operation = mirroringToRightStrategy.compare(leftItem,rightItem);
                    case MIRRORING_TO_lEFT ->
                        operation = mirroringToLeftStrategy.compare(leftItem, rightItem);
                    case null, default -> operation = UNKNOWN;
                }
            } else if (taskModel instanceof BackupTask BC) {
                operation = mirroringToRightStrategy.compare(leftItem,rightItem);
            }else {
                operation = UNKNOWN;
            }
            /* ✪ */

            // zaznamenává počet jednotlivích operací pro statistiky do GUI
            TreeTableUtility.countFiltersAndSizePie( new ComparedItemOperItemRowModel(leftItem,operation,rightItem)
                                                    , dataModel);

            // podle operace a kde je null vytvoříme řádek
            if (leftItem == null && rightItem != null) {
                rowModel = new ComparedItemOperItemRowModel(new FileItemModel(), operation, rightItem);
            } else if (leftItem != null && rightItem == null) {
                rowModel = new ComparedItemOperItemRowModel(leftItem, operation, new FileItemModel());
            } else if (leftItem != null) {
                rowModel = new ComparedItemOperItemRowModel(leftItem, operation, rightItem);
            }

            resultComparedList.add(rowModel);
        }

        dataModel.setComparedFilesList(resultComparedList);
        return dataModel;
    }
}

