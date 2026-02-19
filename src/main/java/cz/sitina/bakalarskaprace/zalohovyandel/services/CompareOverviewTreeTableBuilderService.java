package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.scene.control.TreeItem;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class CompareOverviewTreeTableBuilderService {

    FileItemModel notNullItem;
    byte whereIsntNullPath; /// říká kde byla není nalezena null cesta

    public void loadComparedFilesToGUI(TreeItem root, List<ComparedItemOperItemRowModel> comparedFilesList) {
        /// pro uložení všech větví v TreeTable
        HashMap<String ,TreeItem<ComparedItemOperItemRowModel>> treeFilesList = new HashMap<>();
        treeFilesList.put("", root);

        for (ComparedItemOperItemRowModel row : comparedFilesList) {

            /// musíne dostat cestu z nenullového objektu a zaznamenat kde je nenulová cesta
            if (row.getRightItem().getRelativePath() == null) {
                    whereIsntNullPath = 01;
                notNullItem = row.getLeftItem();
            } if (row.getLeftItem().getRelativePath() == null) {
                whereIsntNullPath = 10;
                notNullItem = row.getRightItem();
            } else {
                whereIsntNullPath = 11;
                notNullItem = row.getLeftItem();
            }

            Path notNullPath = notNullItem.getRelativePath();

            int pathLength = notNullPath.getNameCount();

            /// pro postupné budování větví v TreeTable
            String currentDir = "";
            /// uchovává předchozí složku


            for (int i = 0; pathLength > i ; i++) {
                String iFolderInPath = notNullPath.getName(i).toString(); //
                TreeItem<ComparedItemOperItemRowModel> findedItem;

                boolean isFile = (i == pathLength - 1);

                if (isFile) {/// zjistíme zda je program na konci relativní cesty (na souboru)
                    TreeItem<ComparedItemOperItemRowModel> parentNode = treeFilesList.get(currentDir);

                    TreeItem<ComparedItemOperItemRowModel> fileNode = new TreeItem<>(row);
                    parentNode.getChildren().add(fileNode);

                } else {
                    /// uložení rodičovské cesty
                    String parentKey = currentDir;

                    // Vytvoříme klíč pro aktuální složku s přidáním lomítka
                    if (currentDir.isEmpty()) {
                        currentDir = iFolderInPath;
                    } else {
                        currentDir = currentDir + "/" + iFolderInPath;
                    }

                    if (!treeFilesList.containsKey(currentDir)) {

                        // 1. Vytvoříme model pro složku (jen název, zbytek null)
                        FileItemModel folderInfo = new FileItemModel();
                        folderInfo.setFileName(iFolderInPath);

                        ComparedItemOperItemRowModel folderRow = new ComparedItemOperItemRowModel(
                                new FileItemModel(),
                                CompareOperations.UNKNOW,  // Operace u složky nedává smysl (nebo dej SAME)
                                new FileItemModel()   // Pravá strana
                        );

                        // 2. Zabalíme to do tvého RowModelu
                        if (whereIsntNullPath == 01) {
                            folderRow.getLeftItem().setFileName(iFolderInPath);

                        } else if (whereIsntNullPath == 10) {
                            folderRow.getRightItem().setFileName(iFolderInPath);
                        }
                        else if (whereIsntNullPath == 11) {
                            folderRow.getLeftItem().setFileName(iFolderInPath);
                            folderRow.getRightItem().setFileName(iFolderInPath);
                        }


                        // 3. Vytvoříme TreeItem a propojíme s rodičem
                        TreeItem<ComparedItemOperItemRowModel> newFolderNode = new TreeItem<>(folderRow);
                        newFolderNode.setExpanded(true); // Otevřít složku

                        TreeItem<ComparedItemOperItemRowModel> parentNode = treeFilesList.get(parentKey);
                        parentNode.getChildren().add(newFolderNode);

                        // 4. Uložíme do cache, abychom ji příště našli
                        treeFilesList.put(currentDir, newFolderNode);
                    }

                }
            }
        }
    }


    private short operationSimbol(CompareOperations compareOperation) {
        switch (compareOperation) {
            case EQUALS -> {
                return 1;
            } case NO_EQUALS ->{
                return 2;
            } case IGNORE -> {
                return 3;
            }case DELETE_LEFT ->{
                return 4;
            } case DELETE_RIGHT ->{
                return 5;
            } case DELETE_BOTH ->{
                return 6;
            } case UPDATE_LEFT -> {
                return 7;
            } case UPDATE_RIGHT -> {
                return 8;
            } case ADD_LEFT -> {
                return 9;
            } case ADD_RIGHT -> {
                return 10;
            } case UNKNOW -> {
                return 11;
            }
        }
        return -1;
    }
}
