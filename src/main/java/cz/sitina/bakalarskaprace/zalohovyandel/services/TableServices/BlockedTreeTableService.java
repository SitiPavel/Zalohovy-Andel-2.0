package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import javafx.scene.control.TreeItem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BlockedTreeTableService {

    public static void buildTreeTableBlockedFiles(TreeItem<BlockedFileTreeRow> root, Set<String> blockedFilesList,
                                           String leftRootString, String rightRootString, boolean isBackup) {
        if (root == null || blockedFilesList == null || blockedFilesList.isEmpty()) {
            return;
        }

        HashMap<String, TreeItem<BlockedFileTreeRow>> blockedFileTreeMap = new HashMap<>();
        blockedFileTreeMap.put("", root);
        Path leftRootPath = (leftRootString != null && !leftRootString.isEmpty()) ? Path.of(leftRootString) : null;
        Path rightRootPath = (!isBackup && rightRootString != null && !rightRootString.isEmpty()) ? Path.of(rightRootString) : null;

        for (String blockedFile : blockedFilesList) {
            Path blockedPath = Paths.get(blockedFile);
            int pathLength = blockedPath.getNameCount();
            String currentDir = "";

            for (int i = 0; i < pathLength; i++) {
                String iFolderInPath = blockedPath.getName(i).toString();
                boolean isLeaf = (i == pathLength - 1);

                if (isLeaf) {
                    boolean isExist = false;
                    boolean isFolder = false;

                    // OPRAVENO: 1) Kontrola v levé kořenové složce
                    if (leftRootPath != null) {
                        Path exactLeftPath = leftRootPath.resolve(blockedFile);
                        if (Files.exists(exactLeftPath)) {
                            isExist = true;
                            isFolder = Files.isDirectory(exactLeftPath);
                        }
                    }

                    // OPRAVENO: 2) Pokud neexistuje a jde o synchronizaci, kontrola v pravé kořenové složce
                    if (!isExist && !isBackup && rightRootPath != null) {
                        Path exactRightPath = rightRootPath.resolve(blockedFile);
                        if (Files.exists(exactRightPath)) {
                            isExist = true;
                            isFolder = Files.isDirectory(exactRightPath);
                        }
                    }

                    // Pokud položka na disku vůbec neexistuje, odhad typu: pokud nemá příponu, považujeme ji za složku
                    if (!isExist) {
                        isFolder = !iFolderInPath.contains(".");
                    }

                    TreeItem<BlockedFileTreeRow> parentNode = blockedFileTreeMap.get(currentDir);
                    BlockedFileTreeRow row = new BlockedFileTreeRow(blockedFile, isFolder, isExist, false);
                    TreeItem<BlockedFileTreeRow> fileNode = new TreeItem<>(row);

                    if (parentNode != null) {
                        parentNode.getChildren().add(fileNode);
                    }

                } else {
                    String parentKey = currentDir;
                    currentDir = currentDir.isEmpty() ? iFolderInPath : currentDir + "/" + iFolderInPath;

                    if (!blockedFileTreeMap.containsKey(currentDir)) {
                        boolean isExist = false;

                        if (leftRootPath != null && Files.exists(leftRootPath.resolve(currentDir))) {
                            isExist = true;
                        }
                        if (!isExist && !isBackup && rightRootPath != null && Files.exists(rightRootPath.resolve(currentDir))) {
                            isExist = true;
                        }

                        BlockedFileTreeRow folderRow = new BlockedFileTreeRow(currentDir, true, isExist, true);
                        TreeItem<BlockedFileTreeRow> newFolderNode = new TreeItem<>(folderRow);
                        newFolderNode.setExpanded(true);

                        blockedFileTreeMap.put(currentDir, newFolderNode);

                        TreeItem<BlockedFileTreeRow> parentNode = blockedFileTreeMap.get(parentKey);
                        if (parentNode != null) {
                            parentNode.getChildren().add(newFolderNode);
                        }
                    }
                }
            }
        }
    }

    // Přetížení pro zpětnou kompatibilitu
    public static void buildTreeTableBlockedFiles(TreeItem<BlockedFileTreeRow> root, Set<String> blockedFilesList,
                                           String leftRootString, String rightRootString) {
        buildTreeTableBlockedFiles(root, blockedFilesList, leftRootString, rightRootString, false);
    }

    // Zjistí, zda je zadaná relativní cesta zanořena pod některou z již existujících blokovaných složek
    public static Optional<String> findParentBlockedFolder(String newPathStr, Set<String> blockedFilesList) {
        if (newPathStr == null || blockedFilesList == null || blockedFilesList.isEmpty()) {
            return Optional.empty();
        }
        Path newPath = Paths.get(newPathStr);
        for (String blockedItem : blockedFilesList) {
            Path blockedPath = Paths.get(blockedItem);
            // Je zanořena, pokud začíná blokovanou cestou a není s ní totožná
            if (newPath.startsWith(blockedPath) && !newPath.equals(blockedPath)) {
                return Optional.of(blockedItem);
            }
        }
        return Optional.empty();
    }

    // metoda nám pročistí duplicity a zbytečné itemy v listu, když se nově vloží složka /dokumenty tak
    // všechny potomci (/dokumenty/skola) se z listu odstraní a automaticky budou všechny zablokovány
    public static void cleanRedundantPaths(Set<String> blockPaths) {
        if (blockPaths == null || blockPaths.isEmpty()) {
            return;
        }
        Set<String> toRemove = new LinkedHashSet<>();

        for (String currentPathString : blockPaths) {
            Path currentPath = Path.of(currentPathString);

            for (String othersPathString : blockPaths) {
                if (currentPathString.equals(othersPathString)) {
                    continue;
                }

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
        blockSet.removeIf(pathStr -> Path.of(pathStr).startsWith(deletedPath));
    }
}
