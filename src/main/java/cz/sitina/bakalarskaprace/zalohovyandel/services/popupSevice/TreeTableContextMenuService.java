package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.RightClickMenuItem;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ContextMenuEvent;

import java.util.ArrayList;
import java.util.List;

public class TreeTableContextMenuService {

    private final RightClickMenuService rightClickMenuService = new RightClickMenuService();
    private final IconManagerUtility iconManager = new IconManagerUtility();
    private final TreeTableFunctionsContextMenuService functionsService = new TreeTableFunctionsContextMenuService();

    public enum ColumnSide {
        LEFT,
        RIGHT,
        OPERATION,
        UNKNOWN
    }

    /**
     * Zjistí stranu sloupce (vlevo/vpravo/operace) bez závislosti na souřadnicích nebo pozici sloupce.
     */
    public ColumnSide detectColumnSide(ContextMenuEvent event,
                                      TreeTableColumn<?, ?> leftColName,
                                      TreeTableColumn<?, ?> leftColSize,
                                      TreeTableColumn<?, ?> leftColLastModify,
                                      TreeTableColumn<?, ?> operationCol,
                                      TreeTableColumn<?, ?> rightColName,
                                      TreeTableColumn<?, ?> rightColSize,
                                      TreeTableColumn<?, ?> rightColLastModify) {
        if (event.getTarget() instanceof Node targetNode) {
            Node current = targetNode;
            while (current != null) {
                if (current instanceof TreeTableCell<?, ?> cell) {
                    TreeTableColumn<?, ?> col = cell.getTableColumn();
                    if (col != null) {
                        if (col == leftColName || col == leftColSize || col == leftColLastModify) {
                            return ColumnSide.LEFT;
                        } else if (col == rightColName || col == rightColSize || col == rightColLastModify) {
                            return ColumnSide.RIGHT;
                        } else if (col == operationCol) {
                            return ColumnSide.OPERATION;
                        }
                    }
                    break;
                }
                current = current.getParent();
            }
        }
        return ColumnSide.UNKNOWN;
    }

    /**
     * Obslouží zobrazení kontextového menu pro TreeTableView.
     */
    public void handleContextMenu(ContextMenuEvent event,
                                  TreeTableRow<ComparedItemOperItemRowModel> row,
                                  TreeTableView<ComparedItemOperItemRowModel> treeTable,
                                  TreeTableColumn<?, ?> leftColName,
                                  TreeTableColumn<?, ?> leftColSize,
                                  TreeTableColumn<?, ?> leftColLastModify,
                                  TreeTableColumn<?, ?> operationCol,
                                  TreeTableColumn<?, ?> rightColName,
                                  TreeTableColumn<?, ?> rightColSize,
                                  TreeTableColumn<?, ?> rightColLastModify) {

        if (row == null || row.isEmpty() || row.getItem() == null) {
            return;
        }

        ColumnSide side = detectColumnSide(event, leftColName, leftColSize, leftColLastModify, operationCol, rightColName, rightColSize, rightColLastModify);
        // Pokud bylo kliknuto do sloupce operace, kontextové menu řádku se nezobrazí (má vlastní handler)
        if (side == ColumnSide.OPERATION) {
            return;
        }

        // Pokud řádek není součástí aktuálního výběru, nastavíme výběr na něj
        if (!treeTable.getSelectionModel().isSelected(row.getIndex())) {
            treeTable.getSelectionModel().clearSelection();
            treeTable.getSelectionModel().select(row.getIndex());
        }

        // OPRAVENO: Vytvoření defenzivní kopie seznamu vybraných prvků (prevence IndexOutOfBoundsException při změnách stromu)
        List<TreeItem<ComparedItemOperItemRowModel>> selectedItems =
                new ArrayList<>(treeTable.getSelectionModel().getSelectedItems());
        boolean isMultiSelect = selectedItems.size() > 1;

        List<RightClickMenuItem> menuItems;
        if (isMultiSelect) {
            menuItems = createMultiSelectionMenu(selectedItems, side);
        } else {
            menuItems = createSingleSelectionMenu(row.getItem(), side);
        }

        if (!menuItems.isEmpty()) {
            rightClickMenuService.showRightClickMenu(menuItems, event.getScreenX(), event.getScreenY());
        }
    }

    private List<RightClickMenuItem> createSingleSelectionMenu(ComparedItemOperItemRowModel item, ColumnSide clickedSide) {
        List<RightClickMenuItem> items = new ArrayList<>();

        FileItemModel leftFile = item.getLeftItem();
        FileItemModel rightFile = item.getRightItem();
        boolean hasLeft = (leftFile != null && leftFile.getFileAbsolutePath() != null);
        boolean hasRight = (rightFile != null && rightFile.getFileAbsolutePath() != null);
        boolean isFolder = item.isFolder();

        // 1. Zobrazit v průzkumníku souborů
        String explorerTitle = "Zobrazit v průzkumníku souborů";
        if (clickedSide == ColumnSide.LEFT) {
            if (hasLeft) {
                explorerTitle = "Zobrazit v průzkumníku (vlevo)";
            } else if (hasRight) {
                explorerTitle = "Zobrazit v průzkumníku (vpravo)";
            }
        } else if (clickedSide == ColumnSide.RIGHT) {
            if (hasRight) {
                explorerTitle = "Zobrazit v průzkumníku (vpravo)";
            } else if (hasLeft) {
                explorerTitle = "Zobrazit v průzkumníku (vlevo)";
            }
        } else {
            if (hasLeft) {
                explorerTitle = "Zobrazit v průzkumníku (vlevo)";
            } else if (hasRight) {
                explorerTitle = "Zobrazit v průzkumníku (vpravo)";
            }
        }
        items.add(new RightClickMenuItem(explorerTitle, iconManager.getFolder20x20(), () -> {
            // TODO: Bude implementováno v dalším kroku
            System.out.println("Akce: Zobrazit v průzkumníku pro " + item.getNotNullPath());
        }));

        // 2. Spustit / otevřít soubor (pouze pro soubory, ne pro složky)
        if (!isFolder) {
            String openTitle = "Otevřít soubor";
            if (clickedSide == ColumnSide.LEFT && hasLeft) {
                openTitle = "Otevřít soubor (vlevo)";
            } else if (clickedSide == ColumnSide.RIGHT && hasRight) {
                openTitle = "Otevřít soubor (vpravo)";
            } else if (hasLeft) {
                openTitle = "Otevřít soubor (vlevo)";
            } else if (hasRight) {
                openTitle = "Otevřít soubor (vpravo)";
            }

            items.add(new RightClickMenuItem(openTitle, iconManager.getEntryIcon(), () -> {
                // TODO: Bude implementováno v dalším kroku
                System.out.println("Akce: Otevřít soubor " + item.getNotNullPath());
            }));
        }

        // 3. Zkopírovat absolutní cestu do schránky
        items.add(new RightClickMenuItem("Zkopírovat absolutní cestu", iconManager.getCopyFile(), () -> {
            functionsService.copyAbsolutePath(item, clickedSide);
        }));

        // 4. Zkopírovat relativní cestu do schránky
        items.add(new RightClickMenuItem("Zkopírovat relativní cestu", iconManager.getCopyFile(), () -> {
            functionsService.copyRelativePath(item);
        }));

        // 5. Trvale přidat do blokovaných souborů / ignorovaných složek
        String blockTitle = isFolder ? "Trvale přidat do ignorovaných složek" : "Trvale přidat do blokovaných souborů";
        items.add(new RightClickMenuItem(blockTitle, iconManager.getBlockedFolder(), () -> {
            // TODO: Bude implementováno v dalším kroku (včetně kontroly redundance pod blokovanou složkou)
            System.out.println("Akce: Blokovat položku " + item.getNotNullPath());
        }));

        // 6. Vlastnosti / Detail porovnání
        items.add(new RightClickMenuItem("Vlastnosti porovnání", iconManager.getFileProperties(), () -> {
            // TODO: Bude implementováno v dalším kroku (otevření dvousloupcového okna)
            System.out.println("Akce: Otevřít detail porovnání");
        }));

        return items;
    }

    private List<RightClickMenuItem> createMultiSelectionMenu(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems, ColumnSide clickedSide) {
        List<RightClickMenuItem> items = new ArrayList<>();

        // 1. Trvale přidat do blokovaných souborů
        items.add(new RightClickMenuItem("Trvale přidat vybrané do blokovaných", iconManager.getBlockedFolder(), () -> {
            // TODO: Bude implementováno v dalším kroku
            System.out.println("Akce: Hromadné blokování " + selectedItems.size() + " položek");
        }));

        // 2. Zkopírovat absolutní cesty vybraných souborů
        items.add(new RightClickMenuItem("Zkopírovat absolutní cesty vybraných", iconManager.getCopyFile(), () -> {
            functionsService.copyMultipleAbsolutePaths(selectedItems, clickedSide);
        }));

        // 3. Zkopírovat relativní cesty vybraných souborů
        items.add(new RightClickMenuItem("Zkopírovat relativní cesty vybraných", iconManager.getCopyFile(), () -> {
            functionsService.copyMultipleRelativePaths(selectedItems);
        }));

        // 4. Sbalit / Rozbalit vše ve vybraných složkách
        // OPRAVENO: Předání stabilní kopie seznamu
        items.add(new RightClickMenuItem("Rozbalit vybrané složky", iconManager.getFolderOpened(), () -> {
            functionsService.expandSelectedFolders(selectedItems);
        }));

        items.add(new RightClickMenuItem("Sbalit vybrané složky", iconManager.getFolderClosed(), () -> {
            functionsService.collapseSelectedFolders(selectedItems);
        }));

        // 5. Statistika výběru
        items.add(new RightClickMenuItem("Statistika výběru", iconManager.getIcon_modified_left(), () -> {
            // TODO: Bude implementováno v dalším kroku
            System.out.println("Akce: Statistika výběru");
        }));

        return items;
    }
}
