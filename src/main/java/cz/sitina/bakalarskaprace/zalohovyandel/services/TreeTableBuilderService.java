package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class TreeTableBuilderService {

    FileItemModel notNullItem;
    byte whereIsntNullPath; // říká kde byla není nalezena null cesta

    short iconSize = 20;

    // načítání obrázku operací do paměti
    private final String BASE_PATH = "/cz/sitina/bakalarskaprace/zalohovyandel/images/iconsSmall/";

    private final Image iconEquals       = new Image(getClass().getResourceAsStream(BASE_PATH + "EQUALS.png"));
    private final Image iconIgnore       = new Image(getClass().getResourceAsStream(BASE_PATH + "IGNORE.png"));

    private final Image iconAddLeft      = new Image(getClass().getResourceAsStream(BASE_PATH + "ADD_LEFT.png"));
    private final Image iconAddRight     = new Image(getClass().getResourceAsStream(BASE_PATH + "ADD_RIGHT.png"));

    private final Image iconDeleteBoth   = new Image(getClass().getResourceAsStream(BASE_PATH + "DELETE_BOTH.png"));
    private final Image iconDeleteLeft   = new Image(getClass().getResourceAsStream(BASE_PATH + "DELETE_LEFT.png"));
    private final Image iconDeleteRight  = new Image(getClass().getResourceAsStream(BASE_PATH + "DELETE_RIGHT.png"));

    // Zvaž přejmenování souboru z UNKNOW na UNKNOWN.png
    private final Image iconUnknown      = new Image(getClass().getResourceAsStream(BASE_PATH + "UNKNOWN.png"));

    private final Image iconUpdateLeft   = new Image(getClass().getResourceAsStream(BASE_PATH + "UPDATE_LEFT.png"));
    private final Image iconUpdateRight  = new Image(getClass().getResourceAsStream(BASE_PATH + "UPDATE_RIGHT.png"));



    public void setTreeTable(TreeItem<ComparedItemOperItemRowModel> root,
                                                             List<ComparedItemOperItemRowModel> comparedList,
                                                             TreeTableView<ComparedItemOperItemRowModel> treeTable,
                                                             TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol ) {
        // načtení do TreeTableView
        loadComparedFilesToGUI(root, comparedList);
        // Obarvení řádků podle operace
        rowFormatingByOperation(treeTable);
        //  vloží ikony do sloupce operace
        setOperationColumnIcon(operationCol);
    }

    public void loadComparedFilesToGUI(TreeItem<ComparedItemOperItemRowModel> root, List<ComparedItemOperItemRowModel> comparedFilesList) {
        // pro uložení všech větví v TreeTable
        HashMap<String ,TreeItem<ComparedItemOperItemRowModel>> treeFilesList = new HashMap<>();
        treeFilesList.put("", root);

        for (ComparedItemOperItemRowModel row : comparedFilesList) {

            // musíne dostat cestu z nenullového objektu a zaznamenat kde je nenulová cesta
            if (row.getLeftItem().getRelativePath() != null &&
                    row.getRightItem().getRelativePath() == null) {
                whereIsntNullPath = 01;
                notNullItem = row.getLeftItem();
            } else if (row.getLeftItem().getRelativePath() == null &&
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
                                CompareOperations.UNKNOWN,  // Operace u složky nedává smysl (nebo dej SAME)
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


    /// nastaví barvu řádku podle operace
    public void rowFormatingByOperation(TreeTableView<ComparedItemOperItemRowModel> treeTable) {

        treeTable.setRowFactory(tv -> new TreeTableRow<ComparedItemOperItemRowModel>() {
            @Override
            protected void updateItem(ComparedItemOperItemRowModel item, boolean empty) {
                super.updateItem(item, empty);

                // 1. ZÁSADNÍ KROK: Než cokoliv uděláme, vymažeme VŠECHNY naše vlastní třídy.
                // Tím zajistíme, že zrecyklovaný řádek je úplně čistý.
                getStyleClass().removeAll(
                        "row-equals", "row-ignore", "row-delete",
                        "row-update", "row-add", "row-uknow"
                );

                // 2. Ošetření prázdného řádku nebo složky
                if (item == null || empty || item.getOperation() == null) {
                    return; // Řádek je teď čistý a prázdný, končíme.
                }


                // 3. Přidání správné třídy podle operace
                switch (item.getOperation()) {
                    case EQUALS, NO_EQUALS -> getStyleClass().add("row-equals");
                    case IGNORE -> getStyleClass().add("row-ignore");
                    case DELETE_LEFT, DELETE_RIGHT, DELETE_BOTH -> getStyleClass().add("row-delete");
                    case UPDATE_LEFT, UPDATE_RIGHT -> getStyleClass().add("row-update");
                    case ADD_LEFT, ADD_RIGHT -> getStyleClass().add("row-add");
                    case UNKNOWN -> getStyleClass().add("row-uknow");
                }
            }
        });
    }


    /// nastaví ikonu operace každému řádku
    public void setOperationColumnIcon(TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol) {
        operationCol.setCellFactory(column ->
            new TreeTableCell<ComparedItemOperItemRowModel, CompareOperations>() {

            private final ImageView imageView = new ImageView();

                { //Inicializační blok, spustí se pouze jednou

                    getStyleClass().add("operationCell");


                }

                @Override
                protected void updateItem(CompareOperations item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        return;
                    }
                    Image icon = getIconForOperation(item);

                    if (icon != null) {
                        imageView.setImage(icon);
                        imageView.setFitWidth(iconSize/0.6);
                        imageView.setFitHeight(iconSize);
                        setGraphic(imageView);
                        setText(null);
                    }else {
                        setGraphic(null);
                        setText(item.toString());
                    }

                }

        });
    }

    ///  vrátí správnou ikonu operace
    private Image getIconForOperation(CompareOperations operation) {
        switch (operation) {
            case EQUALS -> {return iconEquals;
            } case NO_EQUALS ->{return null;
            } case IGNORE -> {return iconIgnore;
            }case DELETE_LEFT ->{return iconDeleteLeft;
            } case DELETE_RIGHT ->{return iconDeleteRight;
            } case DELETE_BOTH ->{return iconDeleteBoth;
            } case UPDATE_LEFT -> {return iconUpdateLeft;
            } case UPDATE_RIGHT -> {return iconUpdateRight;
            } case ADD_LEFT -> {return iconAddLeft;
            } case ADD_RIGHT -> {return iconAddRight;
            } case UNKNOWN -> {return iconUnknown;
            }
        }
        return null;
    }

}