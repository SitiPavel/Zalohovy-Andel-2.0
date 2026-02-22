package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.ComparationStrategy;

import java.util.*;

public class ComparisonService {
    ComparedItemOperItemRowModel rowModel;
    ComparedDataAndStatisticsForCompereOverview dataModel;


    public ComparedDataAndStatisticsForCompereOverview compareFolders(
            Map<String, FileItemModel> leftFilesMap,
            Map<String, FileItemModel> rightFilesMap,
            ComparationStrategy strategy)
    {
        List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
        dataModel = new ComparedDataAndStatisticsForCompereOverview();

        // proměnné pro počítadla


        // Sjednotíme klíče z leva a zprava, odstraníme duplicity (objekty stále zůstávají, nic se nemaže)
        Set<String> allKeys = new HashSet<>(leftFilesMap.keySet());
        allKeys.addAll(rightFilesMap.keySet());

        // Seřazení podle abecedy = důležité pro UI
        List<String> sortedKeys =  new ArrayList<>(allKeys);
        Collections.sort(sortedKeys);


        // Průchod listu
        for (String relativePath : sortedKeys) {

            FileItemModel leftItem = leftFilesMap.get(relativePath);
            FileItemModel rightItem = rightFilesMap.get(relativePath);

            /**
            if (relativePath je v hashMap ignorovaných souborů pro tuto úlohu) {
                CompareOperations operations = CompareOperations.IGNORE;
            }
             * */

            /// Vratí nám operaci pro tento soubor
            /* ✪ */ CompareOperations operations = strategy.compare(leftItem,rightItem);

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

