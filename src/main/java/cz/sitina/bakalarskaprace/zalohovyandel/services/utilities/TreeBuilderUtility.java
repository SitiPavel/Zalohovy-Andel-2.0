package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.util.*;

public class TreeBuilderUtility {

    /**
     * Třída reprezentující položku v checkboxovém stromu složek.
     */
    public static class FolderNode {
        private final String folderName;
        private final String absolutePath;

        public FolderNode(String folderName, String absolutePath) {
            this.folderName = folderName;
            this.absolutePath = absolutePath;
        }

        public String getFolderName() {
            return folderName;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        @Override
        public String toString() {
            return folderName;
        }
    }

    /**
     * Sestaví strom podsložek s checkboxy do zadaného TreeView.
     *
     * @param treeView TreeView komponenta, do které se strom vloží
     * @param rootPath kořenová cesta
     * @param foundedFolders mapa nalezených složek (cesta -> název)
     * @param filteredFolders seznam cest složek, které jsou filtrovány (odškrtnuty)
     * @param registeredItems výstupní seznam všech vytvořených položek stromu pro snadné vyčtení stavu
     */
    public static void buildChackBoxDirTree(
            TreeView<FolderNode> treeView,
            String rootPath,
            Map<String, String> foundedFolders,
            List<String> filteredFolders,
            List<CheckBoxTreeItem<FolderNode>> registeredItems) {

        if (treeView == null || foundedFolders == null || foundedFolders.isEmpty()) {
            return;
        }

        registeredItems.clear();
        Set<String> filteredSet = new HashSet<>(filteredFolders != null ? filteredFolders : Collections.emptyList());

        // Kořenový uzel pro TreeView (skrytý, ale rozbalený aby byly vidět položky 1. úrovně)
        FolderNode rootNode = new FolderNode("ROOT", rootPath);
        CheckBoxTreeItem<FolderNode> rootItem = new CheckBoxTreeItem<>(rootNode);
        rootItem.setIndependent(true);
        rootItem.setExpanded(true);

        Map<Path, CheckBoxTreeItem<FolderNode>> pathItemMap = new HashMap<>();
        Path rootNioPath = Path.of(rootPath).toAbsolutePath().normalize();
        pathItemMap.put(rootNioPath, rootItem);

        // Seřadíme cesty podle počtu segmentů, aby byli rodiče zpracováni před potomky
        List<String> sortedPaths = new ArrayList<>(foundedFolders.keySet());
        sortedPaths.sort(Comparator.comparingInt(p -> Path.of(p).getNameCount()));

        for (String rawPath : sortedPaths) {
            Path currentPath = Path.of(rawPath).toAbsolutePath().normalize();
            if (currentPath.equals(rootNioPath)) {
                continue;
            }

            String folderName = foundedFolders.get(rawPath);
            if (folderName == null || folderName.isBlank()) {
                Path fn = currentPath.getFileName();
                folderName = fn != null ? fn.toString() : currentPath.toString();
            }

            FolderNode node = new FolderNode(folderName, rawPath);
            CheckBoxTreeItem<FolderNode> treeItem = new CheckBoxTreeItem<>(node);
            // OPRAVENO: Nezávislé chování uzlů, aby se výchozí JavaFX automatika nepokoušela zaškrtávat sourozence ani nezávislé větve
            treeItem.setIndependent(true);
            treeItem.setExpanded(false);

            // Nastavení zaškrtnutí: pokud je na seznamu filtrovaných, je odškrtnut (false)
            boolean isFiltered = filteredSet.contains(rawPath) || filteredSet.contains(currentPath.toString());
            treeItem.setSelected(!isFiltered);

            registeredItems.add(treeItem);

            // Najdeme nejbližšího rodiče v hierarchii
            Path parentPath = currentPath.getParent();
            CheckBoxTreeItem<FolderNode> parentItem = null;
            while (parentPath != null) {
                parentItem = pathItemMap.get(parentPath);
                if (parentItem != null) {
                    break;
                }
                parentPath = parentPath.getParent();
            }

            if (parentItem == null) {
                parentItem = rootItem;
            }

            parentItem.getChildren().add(treeItem);
            pathItemMap.put(currentPath, treeItem);
        }

        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        final IconManagerUtility iconManager = new IconManagerUtility();
        treeView.setCellFactory(tv -> new TreeCell<FolderNode>() {
            private final CheckBox checkBox = new CheckBox();
            private final ImageView folderIcon = new ImageView();
            private final Label folderLabel = new Label();
            private final HBox container = new HBox(6);
            private ObservableValue<Boolean> currentSelectedProperty;
            private ChangeListener<Boolean> selectionListener;

            {
                checkBox.getStyleClass().add("checkBox_styl2");

                folderIcon.setImage(iconManager.getFolder20x20());
                folderIcon.setFitWidth(18);
                folderIcon.setFitHeight(18);
                folderIcon.setPreserveRatio(true);

                folderLabel.getStyleClass().add("darkBoltText");

                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(checkBox, folderIcon, folderLabel);
            }

            @Override
            protected void updateItem(FolderNode item, boolean empty) {
                super.updateItem(item, empty);

                if (currentSelectedProperty != null && selectionListener != null) {
                    currentSelectedProperty.removeListener(selectionListener);
                    currentSelectedProperty = null;
                    selectionListener = null;
                }

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null);
                } else {
                    folderLabel.setText(item.getFolderName());

                    if (getTreeItem() instanceof CheckBoxTreeItem<FolderNode> cbItem) {
                        checkBox.setSelected(cbItem.isSelected());

                        currentSelectedProperty = cbItem.selectedProperty();
                        selectionListener = (obs, oldVal, newVal) -> checkBox.setSelected(newVal);
                        currentSelectedProperty.addListener(selectionListener);

                        // OPRAVENO: Při kliknutí uživatele na CheckBox:
                        // 1) Nastavíme stav vybraného uzlu
                        // 2) Pokud se zaškrtává (true): rekurzivně zaškrtneme všechny podsložky
                        // 3) Projdeme pouze přímou linii nadsložek až ke kořeni a zaškrtneme je (sourozenci nadsložek zůstanou beze změny vypnuté)
                        // 4) Pokud se odškrtává (false): rekurzivně odškrtneme podsložky
                        checkBox.setOnAction(e -> {
                            boolean targetState = checkBox.isSelected();
                            setSubtreeSelection(cbItem, targetState);

                            if (targetState) {
                                TreeItem<FolderNode> parent = cbItem.getParent();
                                while (parent instanceof CheckBoxTreeItem<?> cbParent && parent != rootItem) {
                                    ((CheckBoxTreeItem<FolderNode>) cbParent).setSelected(true);
                                    parent = parent.getParent();
                                }
                            }
                        });
                    }

                    setTooltip(Utility.createToolTip_600ms(item.getFolderName()));

                    setText(null);
                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Rekurzivně nastaví stav zaškrtnutí pro daný uzel a všechny jeho podsložky.
     */
    private static void setSubtreeSelection(CheckBoxTreeItem<FolderNode> item, boolean selected) {
        item.setSelected(selected);
        for (TreeItem<FolderNode> child : item.getChildren()) {
            if (child instanceof CheckBoxTreeItem<FolderNode> cbChild) {
                setSubtreeSelection(cbChild, selected);
            }
        }
    }
}
