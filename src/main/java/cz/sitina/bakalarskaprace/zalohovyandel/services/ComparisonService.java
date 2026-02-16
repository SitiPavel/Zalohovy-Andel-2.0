package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.ComparationStrategy;

import java.util.*;

public class ComparisonService {



    public List<ComparedItemOperItemRowModel> compareFolders(
            Map<String, FileItemModel> leftFileMap,
            Map<String, FileItemModel> rightFileMap,
            ComparationStrategy strategy)
    {
        List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();

        /// Sjednotíme klíče z leva a zprava, odstraníme duplicity (objekty stále zůstávají, nic se nemaže)
        Set<String> allKeys = new HashSet<>(leftFileMap.keySet());
        allKeys.addAll(rightFileMap.keySet());

        /// Seřazení podle abecedy = důležité pro UI
        List<String> sortedKeys =  new ArrayList<>(allKeys);
        Collections.sort(sortedKeys);

        /// Průchod listu
        for (String relativePath : sortedKeys) {

            FileItemModel leftItem = leftFileMap.get(relativePath);
            FileItemModel rightItem = rightFileMap.get(relativePath);

            /// Vratí nám operaci pro tento soubor
            CompareOperations operations = strategy.compare(leftItem,rightItem);

            ComparedItemOperItemRowModel rowModel = new ComparedItemOperItemRowModel(leftItem, operations, rightItem);
            resultComparedList.add(rowModel);
        }
        return resultComparedList;
    }
}
