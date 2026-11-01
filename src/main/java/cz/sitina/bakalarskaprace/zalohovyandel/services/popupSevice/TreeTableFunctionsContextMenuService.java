package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice.TreeTableContextMenuService.ColumnSide;
import javafx.scene.control.TreeItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.nio.file.Path;
import java.util.List;

public class TreeTableFunctionsContextMenuService {

    // =========================================================================
    // ==================== SEKCE 1: FUNKCE PRO SINGLE SELECT ==================
    // =========================================================================

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

    // =========================================================================
    // ======================= POMOCNÉ INTERNÍ METODY ==========================
    // =========================================================================

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
