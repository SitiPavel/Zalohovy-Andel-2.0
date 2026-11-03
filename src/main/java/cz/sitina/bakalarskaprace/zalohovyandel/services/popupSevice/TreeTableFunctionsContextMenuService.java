package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.BlockedTreeTableService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice.TreeTableContextMenuService.ColumnSide;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.PopupUtility;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TreeTableFunctionsContextMenuService {

    // =========================================================================
    // ==================== SEKCE 1: FUNKCE PRO SINGLE SELECT ==================
    // =========================================================================

    /**
     * Otevře systémový průzkumník souborů (Windows Explorer).
     * - Pokud jde o složku, otevře přímo danou složku (zobrazí její obsah).
     * - Pokud jde o soubor, otevře složku, ve které se soubor nachází, a označí ho.
     */
    public void showInExplorer(ComparedItemOperItemRowModel item, ColumnSide clickedSide) {
        if (item == null) {
            return;
        }

        Path targetPath = resolveAbsolutePathForSide(item, clickedSide);
        if (targetPath == null || !Files.exists(targetPath)) {
            Platform.runLater(() -> ErrorUtility.getInstance(
                    "Položka nenalezena",
                    "Cesta k položce neexistuje",
                    "Zvolený soubor nebo složku se nepodařilo nalézt na disku:\n" + (targetPath != null ? targetPath : item.getNotNullPath())
            ));
            return;
        }

        Thread.ofVirtual().start(() -> {
            try {
                boolean isFolder = Files.isDirectory(targetPath);
                ProcessBuilder processBuilder;
                if (isFolder) {
                    // Otevře přímo obsah složky v průzkumníku
                    processBuilder = new ProcessBuilder("explorer.exe", targetPath.toAbsolutePath().toString());
                } else {
                    // Otevře nadřazenou složku a označí daný soubor
                    processBuilder = new ProcessBuilder("explorer.exe", "/select,", targetPath.toAbsolutePath().toString());
                }
                processBuilder.start();
            } catch (IOException e) {
                Platform.runLater(() -> ErrorUtility.getInstance(
                        "Chyba při otevírání průzkumníka",
                        "Průzkumník souborů se nepodařilo spustit",
                        e.getMessage()
                ));
            }
        });
    }

    /**
     * Otevře nebo spustí soubor pomocí výchozí systémové aplikace Windows.
     * Pokud soubor nemá asociovanou aplikaci, systém Windows nabídne dialog pro výběr programu.
     * Pokud je soubor spustitelný (např. .exe, .bat), spustí se.
     */
    public void openOrExecuteFile(ComparedItemOperItemRowModel item, ColumnSide clickedSide) {
        if (item == null) {
            return;
        }

        Path targetPath = resolveAbsolutePathForSide(item, clickedSide);
        if (targetPath == null || !Files.exists(targetPath)) {
            Platform.runLater(() -> ErrorUtility.getInstance(
                    "Soubor nenalezen",
                    "Soubor neexistuje",
                    "Zvolený soubor se nepodařilo nalézt na disku:\n" + (targetPath != null ? targetPath : item.getNotNullPath())
            ));
            return;
        }

        Thread.ofVirtual().start(() -> {
            try {
                // Volání cmd /c start "" "cesta" spolehlivě ve Windows otevře aplikaci, spustí program nebo vyvolá dialog asociace
                ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "start", "\"\"", targetPath.toAbsolutePath().toString());
                processBuilder.start();
            } catch (IOException e) {
                Platform.runLater(() -> ErrorUtility.getInstance(
                        "Chyba při spuštění souboru",
                        "Soubor se nepodařilo otevřít",
                        e.getMessage()
                ));
            }
        });
    }

    /**
     * Zkopíruje absolutní cestu položky do systémové schránky.
     * Prioritně volí stranu, na kterou uživatel kliknul; pokud na dané straně
     * soubor neexistuje, použije existující stranu jako záložní.
     */
    public void copyAbsolutePath(ComparedItemOperItemRowModel item, ColumnSide clickedSide) {
        if (item == null) {
            return;
        }

        Path absolutePath = resolveAbsolutePathForSide(item, clickedSide);
        if (absolutePath != null) {
            copyTextToClipboard(absolutePath.toString());
        }
    }

    /**
     * Zkopíruje relativní cestu položky do systémové schránky.
     */
    public void copyRelativePath(ComparedItemOperItemRowModel item) {
        if (item == null) {
            return;
        }

        Path relativePath = resolveRelativePath(item);
        if (relativePath != null) {
            copyTextToClipboard(relativePath.toString());
        }
    }

    /**
     * Rekurzivně rozbalí vybranou složku včetně všech jejích vnořených podsložek.
     */
    public void expandSingleFolder(TreeItem<ComparedItemOperItemRowModel> folderItem) {
        if (folderItem == null) {
            return;
        }
        setExpandedRecursively(folderItem, true);
    }

    /**
     * Rekurzivně sbalí vybranou složku včetně všech jejích vnořených podsložek.
     */
    public void collapseSingleFolder(TreeItem<ComparedItemOperItemRowModel> folderItem) {
        if (folderItem == null) {
            return;
        }
        setExpandedRecursively(folderItem, false);
    }

    /**
     * Trvale přidá vybranou položku (soubor nebo složku) do blokovaných souborů / ignorovaných složek tasku.
     * Aktualizuje operaci v tabulce na IGNORE / FOLDER_IGNORE, přepočítá statistiky a označí task jako změněný.
     */
    public void blockSingleItem(TreeItem<ComparedItemOperItemRowModel> treeItem,
                                TaskModel taskModel,
                                Runnable onBlockChanged,
                                Runnable refreshStatistics) {
        if (treeItem == null || treeItem.getValue() == null || taskModel == null) {
            return;
        }

        ComparedItemOperItemRowModel rowItem = treeItem.getValue();
        Path relativePath = resolveRelativePath(rowItem);
        if (relativePath == null) {
            return;
        }

        String cleanedPath = cleanRelativePath(relativePath.toString());
        if (cleanedPath.isEmpty()) {
            return;
        }

        Set<String> blockSet = taskModel.getBlockSet();

        // 1. Kontrola, zda už cesta v blockSet přímo existuje
        if (blockSet.contains(cleanedPath)) {
            showInfoDialog("Položka je blokována",
                    "Položka je již blokována!",
                    "Vybraná položka \"" + cleanedPath + "\" se již nachází v seznamu blokovaných.");
            return;
        }

        // 2. Kontrola, zda není zanořena pod již zablokovanou nadřazenou složkou
        Optional<String> parentBlockedFolder = BlockedTreeTableService.findParentBlockedFolder(cleanedPath, blockSet);
        if (parentBlockedFolder.isPresent()) {
            showInfoDialog("Položka je blokována",
                    "Položka je již blokována!",
                    "Vybraná položka je zanořena v již zablokované složce \"" + parentBlockedFolder.get() + "\".");
            return;
        }

        // 3. Přidání do blockSetu a pročištění redundancí
        blockSet.add(cleanedPath);
        BlockedTreeTableService.cleanRedundantPaths(blockSet);

        // 4. Změna operace v TreeTableView
        applyBlockOperationToTree(treeItem);

        // 5. Aktualizace předků
        TreeTableFolderOperationMakerService.updateOperationForParentFolders(treeItem);

        // 6. Callbacky na přepočet statistik a označení tasku jako dirty
        if (refreshStatistics != null) {
            refreshStatistics.run();
        }
        if (onBlockChanged != null) {
            onBlockChanged.run();
        }
    }

    // =========================================================================
    // =================== SEKCE 2: FUNKCE PRO MULTIPLE SELECT =================
    // =========================================================================

    /**
     * Zkopíruje absolutní cesty všech vybraných položek do schránky (každá na nový řádek).
     * Pro každou položku volí stranu, na kterou uživatel kliknul, s fallbackem na existující stranu.
     */
    public void copyMultipleAbsolutePaths(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems, ColumnSide clickedSide) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (TreeItem<ComparedItemOperItemRowModel> treeItem : selectedItems) {
            if (treeItem != null && treeItem.getValue() != null) {
                Path path = resolveAbsolutePathForSide(treeItem.getValue(), clickedSide);
                if (path != null) {
                    if (sb.length() > 0) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append(path.toString());
                }
            }
        }

        if (sb.length() > 0) {
            copyTextToClipboard(sb.toString());
        }
    }

    /**
     * Zkopíruje relativní cesty všech vybraných položek do schránky (každá na nový řádek).
     */
    public void copyMultipleRelativePaths(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (TreeItem<ComparedItemOperItemRowModel> treeItem : selectedItems) {
            if (treeItem != null && treeItem.getValue() != null) {
                Path path = resolveRelativePath(treeItem.getValue());
                if (path != null) {
                    if (sb.length() > 0) {
                        sb.append(System.lineSeparator());
                    }
                    sb.append(path.toString());
                }
            }
        }

        if (sb.length() > 0) {
            copyTextToClipboard(sb.toString());
        }
    }

    /**
     * Rekurzivně rozbalí všechny vybrané složky včetně všech jejich vnořených podsložek.
     */
    public void expandSelectedFolders(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        // OPRAVENO: Defenzivní kopie seznamu pro stabilní iteraci při změnách stromu
        List<TreeItem<ComparedItemOperItemRowModel>> safeCopy = new ArrayList<>(selectedItems);
        for (TreeItem<ComparedItemOperItemRowModel> item : safeCopy) {
            setExpandedRecursively(item, true);
        }
    }

    /**
     * Rekurzivně sbalí všechny vybrané složky včetně všech jejich vnořených podsložek.
     */
    public void collapseSelectedFolders(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        // OPRAVENO: Defenzivní kopie seznamu pro stabilní iteraci při změnách stromu
        List<TreeItem<ComparedItemOperItemRowModel>> safeCopy = new ArrayList<>(selectedItems);
        for (TreeItem<ComparedItemOperItemRowModel> item : safeCopy) {
            setExpandedRecursively(item, false);
        }
    }

    /**
     * Trvale přidá všechny vybrané položky do blokovaných souborů / ignorovaných složek tasku.
     * Aktualizuje operace v tabulce, přepočítá statistiky a označí task jako změněný.
     * Pokud jsou VŠECHNY vybrané položky již zablokovány, zobrazí souhrnné info okno.
     */
    public void blockMultipleItems(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems,
                                   TaskModel taskModel,
                                   Runnable onBlockChanged,
                                   Runnable refreshStatistics) {
        if (selectedItems == null || selectedItems.isEmpty() || taskModel == null) {
            return;
        }

        Set<String> blockSet = taskModel.getBlockSet();
        List<TreeItem<ComparedItemOperItemRowModel>> safeCopy = new ArrayList<>(selectedItems);

        int newlyBlockedCount = 0;
        int alreadyBlockedCount = 0;

        for (TreeItem<ComparedItemOperItemRowModel> treeItem : safeCopy) {
            if (treeItem == null || treeItem.getValue() == null) {
                continue;
            }

            Path relativePath = resolveRelativePath(treeItem.getValue());
            if (relativePath == null) {
                continue;
            }

            String cleanedPath = cleanRelativePath(relativePath.toString());
            if (cleanedPath.isEmpty()) {
                continue;
            }

            // Kontrola, zda už cesta přímo existuje nebo je pod zablokovanou složkou
            boolean isAlreadyBlocked = blockSet.contains(cleanedPath) ||
                    BlockedTreeTableService.findParentBlockedFolder(cleanedPath, blockSet).isPresent();

            if (isAlreadyBlocked) {
                alreadyBlockedCount++;
                continue;
            }

            blockSet.add(cleanedPath);
            applyBlockOperationToTree(treeItem);
            TreeTableFolderOperationMakerService.updateOperationForParentFolders(treeItem);
            newlyBlockedCount++;
        }

        // Pokud byly všechny vybrané položky již zablokovány, zobrazíme hlášku pro multiple select
        if (newlyBlockedCount == 0 && alreadyBlockedCount > 0) {
            showInfoDialog("Položky jsou blokovány",
                    "Položky jsou již blokovány!",
                    "Všechny vybrané položky se již nacházejí v seznamu blokovaných nebo jsou zanořeny v blokované složce.");
            return;
        }

        if (newlyBlockedCount > 0) {
            BlockedTreeTableService.cleanRedundantPaths(blockSet);

            if (refreshStatistics != null) {
                refreshStatistics.run();
            }
            if (onBlockChanged != null) {
                onBlockChanged.run();
            }
        }
    }

    // =========================================================================
    // ======================= POMOCNÉ INTERNÍ METODY ==========================
    // =========================================================================

    /**
     * Nastaví operaci daného uzlu na IGNORE / FOLDER_IGNORE. Pokud je to složka,
     * aplikuje operaci i na všechny její potomky.
     */
    private void applyBlockOperationToTree(TreeItem<ComparedItemOperItemRowModel> treeItem) {
        if (treeItem == null || treeItem.getValue() == null) {
            return;
        }

        if (treeItem.getValue().isDirectory()) {
            PopupUtility.setAllChildrenOfFolder(treeItem, CompareOperations.FOLDER_IGNORE);
            treeItem.getValue().setOperation(CompareOperations.FOLDER_IGNORE);
        } else {
            treeItem.getValue().setOperation(CompareOperations.IGNORE);
        }
    }

    /**
     * Normalizuje relativní cestu odstraněním počátečních a koncových lomítek.
     */
    private String cleanRelativePath(String pathStr) {
        if (pathStr == null) {
            return "";
        }
        pathStr = pathStr.trim();
        while (pathStr.startsWith("/") || pathStr.startsWith("\\")) {
            pathStr = pathStr.substring(1);
        }
        while (pathStr.endsWith("/") || pathStr.endsWith("\\")) {
            pathStr = pathStr.substring(0, pathStr.length() - 1);
        }
        return pathStr;
    }

    /**
     * Zobrazí informační dialog uživateli.
     */
    private void showInfoDialog(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Pomocná rekurzivní metoda pro nastavení stavu expanded pro uzel a všechny jeho poduzly.
     * Soubory (položky které nejsou složky nebo nemají potomky) přeskočí.
     */
    private void setExpandedRecursively(TreeItem<ComparedItemOperItemRowModel> item, boolean expanded) {
        if (item == null) {
            return;
        }

        boolean isFolder = (item.getValue() != null && item.getValue().isDirectory()) || !item.isLeaf();
        if (isFolder) {
            item.setExpanded(expanded);
            for (TreeItem<ComparedItemOperItemRowModel> child : item.getChildren()) {
                setExpandedRecursively(child, expanded);
            }
        }
    }

    /**
     * Určí absolutní cestu podle kliknuté strany s bezpečným fallbackem.
     */
    private Path resolveAbsolutePathForSide(ComparedItemOperItemRowModel item, ColumnSide clickedSide) {
        FileItemModel left = item.getLeftItem();
        FileItemModel right = item.getRightItem();

        boolean hasLeft = (left != null && left.getFileAbsolutePath() != null);
        boolean hasRight = (right != null && right.getFileAbsolutePath() != null);

        if (clickedSide == ColumnSide.LEFT) {
            if (hasLeft) return left.getFileAbsolutePath();
            if (hasRight) return right.getFileAbsolutePath();
        } else if (clickedSide == ColumnSide.RIGHT) {
            if (hasRight) return right.getFileAbsolutePath();
            if (hasLeft) return left.getFileAbsolutePath();
        } else {
            if (hasLeft) return left.getFileAbsolutePath();
            if (hasRight) return right.getFileAbsolutePath();
        }

        return item.getNotNullPath();
    }

    /**
     * Získá relativní cestu z levé nebo pravé strany modelu.
     */
    private Path resolveRelativePath(ComparedItemOperItemRowModel item) {
        if (item.getLeftItem() != null && item.getLeftItem().getRelativePath() != null) {
            return item.getLeftItem().getRelativePath();
        }
        if (item.getRightItem() != null && item.getRightItem().getRelativePath() != null) {
            return item.getRightItem().getRelativePath();
        }
        return null;
    }

    /**
     * Vloží textový řetězec do systémové schránky JavaFX Clipboard.
     */
    private void copyTextToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
}
