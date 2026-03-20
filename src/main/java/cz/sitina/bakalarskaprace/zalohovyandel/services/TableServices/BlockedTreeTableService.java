package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import javafx.scene.control.TreeItem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BlockedTreeTableService {

    public static void  buildTreeTableBlockedFiles(TreeItem<BlockedFileTreeRow> root, Set<String> blockedFilesList,
                                           String leftRootString, String rightRootString) {
        if (root == null || blockedFilesList == null || blockedFilesList.isEmpty()) {
            System.out.println("buildTreeTableBlockedFiles: root nebo blockedFilesList je null, popřípadě je " +
                    "blockedFilesList prázdný");
            return;
        }

        HashMap<String,TreeItem<BlockedFileTreeRow>> blockedFileTreeMap = new HashMap<>();
        blockedFileTreeMap.put("", root);
        Path leftRootPath = Path.of(leftRootString);
        Path rightRootPath = Path.of(rightRootString);


        for (String blockedFile : blockedFilesList) {
            Path blockedPath = Paths.get(blockedFile);
            int pathLength = blockedPath.getNameCount();

            String currentDir = "";


            for (int i = 0; i < pathLength; i++) {


                String iFolderInPath = blockedPath.getName(i).toString();

                boolean isFile = (i == pathLength - 1);

                if (isFile) {
                    boolean isExist = false;
                    boolean isFolder = false;
                    // zjištění zda ten soboru byl nalezen pokud jo tak zobrazíme jako nalezen
                    Path exactLeftPath = leftRootPath.resolve(blockedFile);
                    Path exactRightPath = rightRootPath.resolve(blockedFile);
                    if (Files.exists(exactLeftPath)) {
                        isExist= true;
                        isFolder = Files.isDirectory(exactLeftPath);

                    }
                    if (Files.exists(exactRightPath)) {
                        isExist = true;
                        isFolder = Files.isDirectory(exactRightPath);
                    }

                    // vytvoření a přidání itemu do stromu
                    TreeItem<BlockedFileTreeRow> parentNode = blockedFileTreeMap.get(currentDir);
                    BlockedFileTreeRow row = new BlockedFileTreeRow(blockedFile, isFolder, isExist, false);
                    TreeItem<BlockedFileTreeRow> fileNode = new TreeItem<>(row);

                    parentNode.getChildren().add(fileNode);

                } else {

                    /// uložení rodičovské cesty
                    String parentKey = currentDir;

                    if (currentDir.isEmpty()) {
                        currentDir = iFolderInPath;
                    } else {
                        currentDir = currentDir + "/" + iFolderInPath;
                    }

                    if (!blockedFileTreeMap.containsKey(currentDir)) {
                        boolean isExist = false;
                        // zjištění zda ten soboru byl nalezen pokud jo tak zobrazíme jako nalezen
                        Path exactLeftPath = leftRootPath.resolve(currentDir);
                        Path exactRightPath = rightRootPath.resolve(currentDir);
                        if (Files.exists(exactLeftPath) || Files.exists(exactRightPath)) {
                            isExist= true;
                        }

                        // vytvoření a přidání nového itemu složky do stromu
                        BlockedFileTreeRow fileNode = new BlockedFileTreeRow(currentDir, true, isExist, true);
                        TreeItem<BlockedFileTreeRow> newFileNode = new TreeItem<>(fileNode);
                        newFileNode.setExpanded(true);

                        blockedFileTreeMap.put(currentDir, newFileNode);

                        TreeItem<BlockedFileTreeRow> parentNode = blockedFileTreeMap.get(parentKey);
                        parentNode.getChildren().add(newFileNode);

                    }

                }

            }
        }
    }

    // metoda nám pročistí duplicity a zbytečné itemy v listu, když se nově vloží složka /dokumenty tak
    // všechny potomci (/dokumenty/skola) se z listu odstraní a automaticky budou všechny zablokovány
    public static void cleanRedundantPaths(Set<String> blockPaths) {
        if (blockPaths == null || blockPaths.isEmpty()) {
            return;
        }
        Set<String> toRemove = new LinkedHashSet<>();

        // aktuální cesta kterou porovnáváme
        for (String currentPathString : blockPaths) {
            Path currentPath = Path.of(currentPathString);

            // ostatní cesty které porovnáváme s tou currentPath
            for (String othersPathString : blockPaths) {
                if (currentPathString.equals(othersPathString)) {continue;} // nehceme porovnávat stejnou se stejnou cestou

                Path otherPath = Path.of(othersPathString);

                if (currentPath.startsWith(otherPath)) {
                    toRemove.add(currentPathString);
                    break;
                }
            }
        }
        blockPaths.removeAll(toRemove);
    }

    // odstraní ze String listu vše co začíná @deletedPath
    public static void removeItemAndChildren(String deletedPathStr, Set<String> blockSet) {
        Path deletedPath = Path.of(deletedPathStr);

        // FAKTA: Převedeme oba texty na Path a necháme Javu porovnat celé uzly cesty
        blockSet.removeIf(pathStr -> Path.of(pathStr).startsWith(deletedPath));
    }
}
