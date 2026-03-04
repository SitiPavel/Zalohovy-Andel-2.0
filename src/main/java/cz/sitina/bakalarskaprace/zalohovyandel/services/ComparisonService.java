package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;


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

        CompareOperations operations;

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

            /**
            if (relativePath je v hashMap ignorovaných souborů pro tuto úlohu) {
                CompareOperations operations = CompareOperations.IGNORE;
            }
             * */

            /// porovná jaká strategie se použije a vratí nám operaci pro tento řídek souboru
            if (taskModel instanceof ManualSyncTask MSC) {
                switch (MSC.getCompareStrategy()) {
                    case BIDIRECTIONAL_SYNC ->
                        operations = biDirectionalStrategy.compare(leftItem,rightItem);
                    case BACKUP_TO_RIGHT ->
                        operations = oneWayToRightStrategy.compare(leftItem,rightItem);
                    case BACKUP_TO_LEFT ->
                        operations = oneWayToLeftStrategy.compare(leftItem, rightItem);
                    case MIRRORING_to_RIGHT ->
                        operations = mirroringToRightStrategy.compare(leftItem,rightItem);
                    case MIRRORING_TO_lEFT ->
                        operations = mirroringToLeftStrategy.compare(leftItem, rightItem);
                    case null, default -> operations = UNKNOWN;
                }
            } else {
                operations = UNKNOWN;
            }
            /* ✪ */

            // zaznamenává počet jednotlivích operací pro statistiky do GUI
            switch (operations) {
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
                    dataModel.attributeDataTransportSize(rightItem.getFileSize());
                }
                case UPDATE_RIGHT -> {
                    dataModel.attributeRightUpdateCount(1);
                    dataModel.attributeDataTransportSize(leftItem.getFileSize());
                }
                case ADD_LEFT -> {
                    dataModel.attributeLeftAddCount(1);
                    dataModel.attributeDataTransportSize(rightItem.getFileSize());
                }
                case ADD_RIGHT -> {
                    dataModel.attributeRightAddCount(1);
                    dataModel.attributeDataTransportSize(leftItem.getFileSize());
                }
                case UNKNOWN -> dataModel.attributeUnknownCount(1);
            }

            // podle operace a kde je null vytvoříme řádek
            if (leftItem == null && rightItem != null) {
                rowModel = new ComparedItemOperItemRowModel(new FileItemModel(), operations, rightItem);
            } else if (leftItem != null && rightItem == null) {
                rowModel = new ComparedItemOperItemRowModel(leftItem, operations, new FileItemModel());
            } else if (leftItem != null) {
                rowModel = new ComparedItemOperItemRowModel(leftItem, operations, rightItem);
            }

            resultComparedList.add(rowModel);
            dataModel.setComparedFilesList(resultComparedList);
        }
        return dataModel;
    }
}

