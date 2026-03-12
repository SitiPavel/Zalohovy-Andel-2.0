package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class TreeTableBuilderService {

    FileItemModel notNullItem;
    byte whereIsntNullPath; // říká kde byla není nalezena null cesta
    private final IconManagerUtility iconManager = new IconManagerUtility();




    public void setTreeTable(TreeItem<ComparedItemOperItemRowModel> root,
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
                                CompareOperations.FOLDER,
                                new FileItemModel()   // Pravá strana
                        );

                        folderRow.getLeftItem().setFileName(iFolderInPath);
                        folderRow.getRightItem().setFileName(iFolderInPath);


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

    private static final List<String> listOfRowStyle = List.of(
            "row-equals", "row-ignore", "row-delete",
            "row-update", "row-add", "row-uknow", "row-FOLDER_UNEVEN",
            "row-FOLDER_DELETE","row-FOLDER_UPDATE","row-FOLDER_ADD",
            "row-FOLDER_UNKNOWN","row-FOLDER_IGNORE"
    );

    /// nastaví barvu řádku podle operace
    public void rowFormatingByOperation(TreeTableView<ComparedItemOperItemRowModel> treeTable) {

        treeTable.setRowFactory(tv -> new TreeTableRow<ComparedItemOperItemRowModel>() {
            private String currentCssClass = null;

            @Override
            protected void updateItem(ComparedItemOperItemRowModel item, boolean empty) {
                super.updateItem(item, empty);

                // 1. ZÁSADNÍ KROK: Než cokoliv uděláme, vymažeme VŠECHNY naše vlastní třídy.
                // Tím zajistíme, že zrecyklovaný řádek je úplně čistý.
                getStyleClass().removeAll(listOfRowStyle);

                // 2. Ošetření prázdného řádku nebo složky
                if (item == null || empty || item.getOperation() == null) {
                    return; // Řádek je teď čistý a prázdný, končíme.
                }


                // 3. Přidání správné třídy podle operace
                switch (item.getOperation()) {
                    case EQUALS -> getStyleClass().add("row-equals");
                    case IGNORE -> getStyleClass().add("row-ignore");
                    case DELETE_LEFT, DELETE_RIGHT, DELETE_BOTH -> getStyleClass().add("row-delete");
                    case UPDATE_LEFT, UPDATE_RIGHT -> getStyleClass().add("row-update");
                    case ADD_LEFT, ADD_RIGHT -> getStyleClass().add("row-add");
                    case UNKNOWN -> getStyleClass().add("row-uknow");

                    case FOLDER_UNEVEN -> getStyleClass().add("row-FOLDER_UNEVEN");
                    case FOLDER_DELETE_LEFT, FOLDER_DELETE_RIGHT, FOLDER_DELETE_BOTH ->
                        getStyleClass().add("row-FOLDER_DELETE");
                    case FOLDER_UPDATE_LEFT, FOLDER_UPDATE_RIGHT -> getStyleClass().add("row-FOLDER_UPDATE");
                    case FOLDER_ADD_LEFT, FOLDER_ADD_RIGHT -> getStyleClass().add("row-FOLDER_ADD");
                    case FOLDER_UNKNOWN -> getStyleClass().add("row-FOLDER_UNKNOWN");
                    case FOLDER_IGNORE -> getStyleClass().add("row-FOLDER_IGNORE");
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
                    // zachování poměrů a vyhlazení při změně velikosti
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    // vycentrování
                    setAlignment(javafx.geometry.Pos.CENTER);
                    setStyle("-fx-padding: 0px;");
                    // outomatická velikost podle šířky sloupce ařádků
                    imageView.fitWidthProperty().bind(widthProperty());
                    imageView.fitHeightProperty().bind(heightProperty());
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
            case EQUALS ->      {return  iconManager.getIconEquals();}
            case IGNORE ->      {return  iconManager.getIconIgnore();}
            case DELETE_LEFT -> {return  iconManager.getIconDeleteLeft();}
            case DELETE_RIGHT ->{return  iconManager.getIconDeleteRight();}
            case DELETE_BOTH -> {return  iconManager.getIconDeleteBoth();}
            case UPDATE_LEFT -> {return  iconManager.getIconUpdateLeft();}
            case UPDATE_RIGHT ->{return  iconManager.getIconUpdateRight();}
            case ADD_LEFT ->    {return  iconManager.getIconAddLeft();}
            case ADD_RIGHT ->   {return  iconManager.getIconAddRight();}
            case UNKNOWN ->     {return  iconManager.getIconUnknown();}
            case FOLDER ->      {return  iconManager.getIconFolder();}
            case FOLDER_UNEVEN ->       {return  iconManager.getIconFOLDER_UNEVEN();}
            case FOLDER_EQUALS ->       {return  iconManager.getIconFOLDER_EQUALS();}
            case FOLDER_DELETE_LEFT ->  {return  iconManager.getIconFOLDER_DELETE_LEFT();}
            case FOLDER_DELETE_RIGHT -> {return  iconManager.getIconFOLDER_DELETE_RIGHT();}
            case FOLDER_DELETE_BOTH ->  {return  iconManager.getIconFOLDER_DELETE_BOTH();}
            case FOLDER_UPDATE_LEFT ->  {return  iconManager.getIconFOLDER_UPDATE_LEFT();}
            case FOLDER_UPDATE_RIGHT -> {return  iconManager.getIconFOLDER_UPDATE_RIGHT();}
            case FOLDER_ADD_LEFT ->     {return  iconManager.getIconFOLDER_ADD_LEFT();}
            case FOLDER_ADD_RIGHT ->    {return  iconManager.getIconFOLDER_ADD_RIGHT();}
            case FOLDER_UNKNOWN ->      {return  iconManager.getIconFOLDER_UNKNOWN();}
            case FOLDER_IGNORE ->       {return  iconManager.getIconFOLDER_IGNORE();}
        }
        return null;
    }


    //*
    // důležité protože po sestavení stromu | řazení během sestavování stromu by seřazoval algoritmus celí strom při
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


    public void setColumnIindentLevelAndToolTips(TreeTableColumn col) {
        col.setCellFactory(column -> {
            return new TreeTableCell<ComparedItemOperItemRowModel, String>() {


                // 1. Vytvoříme Tooltip pouze JEDNOU pro životní cyklus buňky
                private final Tooltip myTooltip = new Tooltip();


                private ImageView imageView = new ImageView();
                {
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    // Reset pro prázdné buňky
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setPadding(Insets.EMPTY);
                        setTooltip(null); // Důležité: odebereme Tooltip, aby nestrašil na prázdném řádku
                        return;
                    }

                    // nastaví ikonu podle typu položky
                    CompareOperations operation =  getTreeTableRow().getItem().getOperation();
                    if (operation.toString().contains("FOLDER")) {
                        imageView.setImage(iconManager.getFolder20x20());
                    } else {
                        imageView.setImage(iconManager.getFile20x20());
                    }
                    setGraphic(imageView);

                    setText(item);
                    myTooltip.setText(item);
                    setTooltip(myTooltip);

                    // --- Výpočet odsazení (Indentation) ---
                    // Získáme index buňky
                    int currentIndex = getIndex();

                    // Ochrana indexu
                    if (currentIndex >= 0 && currentIndex < getTreeTableView().getExpandedItemCount()) {
                        TreeItem<ComparedItemOperItemRowModel> curentTreeItem =
                                getTreeTableView().getTreeItem(currentIndex);

                        if (curentTreeItem != null) {
                            int level = getTreeTableView().getTreeItemLevel(curentTreeItem);

                            // Pokud máš root skrytý, první viditelné položky jsou level 1.
                            // Pokud chceš level 1 bez odsazení, použij (level - 1).
                            // Pokud chceš odsazení už od začátku, nechej jen (level).
                            double indentPerLevel = 15;

                            // Math.max zajistí, že nepůjdeme do mínusu
                            double indent = Math.max(0, (level - 1) * indentPerLevel);

                            // Nastavíme padding
                            setPadding(new Insets(2, 2, 2, indent));
                        }
                    }
                }
            };
        });
    }

}