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

                    Path folderRelativeSubPath = notNullPath.subpath(0, i + 1);

                    if (!treeFilesList.containsKey(currentDir)) {

                        // Složka dostane plnohodnotné modely s relativePath a odvozenou fileAbsolutePath
                        ComparedItemOperItemRowModel folderRow = new ComparedItemOperItemRowModel(
                                new FileItemModel(),
                                CompareOperations.FOLDER,
                                new FileItemModel()   // Pravá strana
                        );

                        // Nastavíme relativní cesty a názvy složky
                        folderRow.getLeftItem().setFileName(iFolderInPath);
                        folderRow.getLeftItem().setRelativePath(folderRelativeSubPath);
                        folderRow.getRightItem().setFileName(iFolderInPath);
                        folderRow.getRightItem().setRelativePath(folderRelativeSubPath);

                        // Pokud má levý soubor absolutní kořen, odvodíme z něj absolutní cestu pro složku vlevo
                        if (row.getLeftItem() != null && row.getLeftItem().getFileAbsolutePath() != null && row.getLeftItem().getRelativePath() != null) {
                            Path rootLeft = deriveRootPath(row.getLeftItem().getFileAbsolutePath(), row.getLeftItem().getRelativePath());
                            if (rootLeft != null) {
                                folderRow.getLeftItem().setFileAbsolutePath(rootLeft.resolve(folderRelativeSubPath));
                            }
                        }

                        // Pokud má pravý soubor absolutní kořen, odvodíme z něj absolutní cestu pro složku vpravo
                        if (row.getRightItem() != null && row.getRightItem().getFileAbsolutePath() != null && row.getRightItem().getRelativePath() != null) {
                            Path rootRight = deriveRootPath(row.getRightItem().getFileAbsolutePath(), row.getRightItem().getRelativePath());
                            if (rootRight != null) {
                                folderRow.getRightItem().setFileAbsolutePath(rootRight.resolve(folderRelativeSubPath));
                            }
                        }

                        // 3. Vytvoříme TreeItem a propojíme s rodičem
                        TreeItem<ComparedItemOperItemRowModel> newFolderNode = new TreeItem<>(folderRow);
                        newFolderNode.setExpanded(true); // Otevřít složku

                        TreeItem<ComparedItemOperItemRowModel> parentNode = treeFilesList.get(parentKey);
                        parentNode.getChildren().add(newFolderNode);

                        // 4. Uložíme do cache, abychom ji příště našli
                        treeFilesList.put(currentDir, newFolderNode);
                    } else {
                        // OPRAVENO: Pokud už uzel složky v cache existuje (např. z jiné větve), doplníme chybějící absolutní cestu druhé strany
                        TreeItem<ComparedItemOperItemRowModel> existingFolderNode = treeFilesList.get(currentDir);
                        ComparedItemOperItemRowModel existingModel = existingFolderNode.getValue();

                        if (existingModel.getLeftItem().getFileAbsolutePath() == null &&
                                row.getLeftItem() != null && row.getLeftItem().getFileAbsolutePath() != null && row.getLeftItem().getRelativePath() != null) {
                            Path rootLeft = deriveRootPath(row.getLeftItem().getFileAbsolutePath(), row.getLeftItem().getRelativePath());
                            if (rootLeft != null) {
                                existingModel.getLeftItem().setFileAbsolutePath(rootLeft.resolve(folderRelativeSubPath));
                            }
                        }

                        if (existingModel.getRightItem().getFileAbsolutePath() == null &&
                                row.getRightItem() != null && row.getRightItem().getFileAbsolutePath() != null && row.getRightItem().getRelativePath() != null) {
                            Path rootRight = deriveRootPath(row.getRightItem().getFileAbsolutePath(), row.getRightItem().getRelativePath());
                            if (rootRight != null) {
                                existingModel.getRightItem().setFileAbsolutePath(rootRight.resolve(folderRelativeSubPath));
                            }
                        }
                    }

                }
            }
        }

        // zde daný strom rekurzivně seřadíme aby složky a soubory byly u sebe
        sortChildrenRecursively(root);
        //Nastavení funkcí

    }

    // Pomocná metoda pro výpočet kořenového adresáře z absolutní a relativní cesty
    private Path deriveRootPath(Path absolutePath, Path relativePath) {
        if (absolutePath == null || relativePath == null) {
            return null;
        }
        int relCount = relativePath.getNameCount();
        int absCount = absolutePath.getNameCount();
        if (absCount >= relCount) {
            Path root = absolutePath;
            for (int k = 0; k < relCount; k++) {
                root = root.getParent();
            }
            return root;
        }
        return null;
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
