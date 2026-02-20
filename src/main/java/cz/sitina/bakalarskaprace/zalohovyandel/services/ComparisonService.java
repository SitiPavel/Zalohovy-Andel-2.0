package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.ComparationStrategy;

import java.util.*;



public class ComparisonService {
    ComparedItemOperItemRowModel rowModel;


    public List<ComparedItemOperItemRowModel> compareFolders(
            Map<String, FileItemModel> leftFilesMap,
            Map<String, FileItemModel> rightFilesMap,
            ComparationStrategy strategy)
    {
        List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();

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

            if (leftItem == null && rightItem != null) {
                rowModel = new ComparedItemOperItemRowModel(new FileItemModel(), operations, rightItem);
            } else if (leftItem != null && rightItem == null) {
                rowModel = new ComparedItemOperItemRowModel(leftItem, operations, new FileItemModel());
            } else if (leftItem != null) {
                rowModel = new ComparedItemOperItemRowModel(leftItem, operations, rightItem);
            }

            resultComparedList.add(rowModel);
        }
        return resultComparedList;
    }
}

