package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import javafx.scene.control.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class TreeTableBuilderService {

    FileItemModel notNullItem;
    byte whereIsntNullPath; // říká kde byla není nalezena null cesta
    private final IconManagerUtility iconManager = new IconManagerUtility();




    public void buildTreeTableItemOperItem(TreeItem<ComparedItemOperItemRowModel> root,
                                           List<ComparedItemOperItemRowModel> comparedList
                            ){

        // načtení do TreeTableView
        loadComparedFilesToRoot(root, comparedList);

    }

    public void loadComparedFilesToRoot(TreeItem<ComparedItemOperItemRowModel> root,
                                        List<ComparedItemOperItemRowModel> comparedFilesList) {
        // pro uložení všech větví v TreeTable
        HashMap<String ,TreeItem<ComparedItemOperItemRowModel>> treeFilesList = new HashMap<>();
        treeFilesList.put("", root);

        for (ComparedItemOperItemRowModel row : comparedFilesList) {

            // musíne dostat cestu z nenullového objektu a zaznamenat kde je nenulová cesta
            if (row.getLeftItem().getRelativePath() != null &&
                    row.getRightItem() ==null || row.getRightItem().getRelativePath() == null) {
                whereIsntNullPath = 01;
                notNullItem = row.getLeftItem();
            } else if (row.getLeftItem() == null ||row.getLeftItem().getRelativePath() == null &&
                    row.getRightItem().getRelativePath() != null) {
                whereIsntNullPath = 10;
                notNullItem = row.getRightItem();
            } else if (row.getLeftItem().getRelativePath() != null &&
                    row.getRightItem().getRelativePath() != null) {
                whereIsntNullPath = 11;
                notNullItem = row.getLeftItem();
            }

            Path notNullPath = notNullItem.getRelativePath();

            int pathLength = notNullPath.getNameCount();

            // pro postupné budování větví v TreeTable
            String currentDir = "";
            // uchovává předchozí složku


            for (int i = 0; pathLength > i ; i++) {
                String iFolderInPath = notNullPath.getName(i).toString(); //

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
                                CompareOperations.FOLDER,
                                new FileItemModel()   // Pravá strana
                        );

                        if (whereIsntNullPath == 01 ) {
                            folderRow.getLeftItem().setFileName(iFolderInPath);
                        } else if (whereIsntNullPath == 10) {
                            folderRow.getRightItem().setFileName(iFolderInPath);
                        } else {
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

        // zde daný strom rekurzivně seřadíme aby složky a soubory byly u sebe
        sortChildrenRecursively(root);
        //Nastavení funkcí

    }


    //*
    // důležité po sestavení stromu | řazení během sestavování stromu by seřazoval algoritmus celí strom při
    // každém novém prvku
    private void sortChildrenRecursively(TreeItem<ComparedItemOperItemRowModel> item) {

        if (item == null || item.getChildren().isEmpty()) return;

        item.getChildren().sort((node1, node2) -> {
            boolean isFolder1 = node1.getValue().isFolder();
            boolean isFolder2 = node2.getValue().isFolder();

            if (isFolder1 && !isFolder2) return -1;
            if (!isFolder1 && isFolder2) return 1;

            String name1 = getNotNullName(node1);
            String name2 = getNotNullName(node2);

            return name1.compareToIgnoreCase(name2); // porovnat name1 a name2 abecedně
        });

        //Rekurrzí zavoláme na ostatní děti
        for (TreeItem<ComparedItemOperItemRowModel> child : item.getChildren()) {
            sortChildrenRecursively(child);
        }
    }



    // získáme z ComparedItemOperItemRowModel název toho soubooru, zaručuje že název nebude null
    private String getNotNullName(TreeItem<ComparedItemOperItemRowModel> item) {
        if (item == null ) return "";

        if (item.getValue().getLeftItem() != null &&
                item.getValue().getRightItem() != null ) {
            return item.getValue().getLeftItem().getFileName();

        } else if (item.getValue().getLeftItem() == null &&
                   item.getValue().getRightItem() != null ) {
            return item.getValue().getRightItem().getFileName();

        } else if (item.getValue().getLeftItem() != null &&
                   item.getValue().getRightItem() == null ) {
            return item.getValue().getLeftItem().getFileName();
        } else {
            return "";
        }

    }



}