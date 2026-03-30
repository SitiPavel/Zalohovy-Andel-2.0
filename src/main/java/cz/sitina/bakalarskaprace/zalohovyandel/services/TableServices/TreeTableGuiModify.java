package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;



public class TreeTableGuiModify {
    private final IconManagerUtility iconManager = new IconManagerUtility();


    private static final List<String> listOfRowStyle = List.of(
            "row-equals", "row-ignore", "row-delete",
            "row-update", "row-add", "row-uknow", "row-FOLDER_UNEVEN",
            "row-FOLDER_DELETE","row-FOLDER_UPDATE","row-FOLDER_ADD",
            "row-FOLDER_UNKNOWN","row-FOLDER_IGNORE"
    );


    /**
     * PRO JEDNOTLIVÉ řádky
     *  metoda - setRowFactory
     * */

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

    /**
     * PRO JEDNOTLIVÉ BUŇKY
     * metoda - setCellFactory
     * */

    /// nastaví objekt pro ikonu
    public ImageView findAndSetForOperationIcon(TreeTableCell<?,?> cell) {
        ImageView imageView = new ImageView();
        //Inicializační blok, spustí se pouze jednou

        cell.getStyleClass().add("operationCell");
            // zachování poměrů a vyhlazení při změně velikosti
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // vycentrování
        cell.setAlignment(javafx.geometry.Pos.CENTER);
        cell.setStyle("-fx-padding: 0px;");
        // outomatická velikost podle šířky sloupce ařádků
        imageView.fitWidthProperty().bind(cell.widthProperty());
        imageView.fitHeightProperty().bind(cell.heightProperty());

        return imageView;
    }



    ///  vrátí správnou ikonu operace
    public Image getIconForOperation(CompareOperations operation) {
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



    /// --- Výpočet odsazení (Indentation) ---
    // Získáme index buňky
    public void setIndentByDeepForCell(int index, TreeTableView<?> treeTableView, TreeTableCell<?, ?> cell) {
        int currentIndex = index;

        // Ochrana indexu
        if (currentIndex >= 0 && currentIndex < treeTableView.getExpandedItemCount()) {
            TreeItem<ComparedItemOperItemRowModel> curentTreeItem =
                    (TreeItem<ComparedItemOperItemRowModel>) treeTableView.getTreeItem(currentIndex);

            if (curentTreeItem != null) {
                int level = treeTableView.getTreeItemLevel(curentTreeItem);

                // Pokud máš root skrytý, první viditelné položky jsou level 1.
                // Pokud chceš level 1 bez odsazení, použij (level - 1).
                // Pokud chceš odsazení už od začátku, nechej jen (level).
                double indentPerLevel = 15;

                // Math.max zajistí, že nepůjdeme do mínusu
                double indent = Math.max(0, (level - 1) * indentPerLevel);

                // Nastavíme padding
                cell.setPadding(new Insets(2, 2, 2, indent));
            }
        }
    }

    ///  vrací ikonu podle formátu souboru (zatím formáty neodlišujeme)
    public ImageView getIconByRowFormat(CompareOperations operation) {
        ImageView imageView = new ImageView();
        {
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);
            imageView.setPreserveRatio(true);
        }

        if (operation.toString().contains("FOLDER")) {
            imageView.setImage(iconManager.getFolder20x20());
        } else {
            imageView.setImage(iconManager.getFile20x20());
        }

        return imageView;

    }

    public ImageView getIconByIsFolder(boolean isFolder) {
        ImageView imageView = new ImageView();
        {
            imageView.setFitWidth(16);
            imageView.setFitHeight(16);
            imageView.setPreserveRatio(true);
        }

        if (isFolder) {
            imageView.setImage(iconManager.getFolder20x20());
        } else {
            imageView.setImage(iconManager.getFile20x20());
        }

        return imageView;

    }

    public CheckBox getCheckBox(boolean isSelected) {
        CheckBox checkBox = new CheckBox();
        {
            checkBox.setSelected(isSelected);
            checkBox.getStyleClass().addAll("checkBox_styl2", "modify_opacity1");
            checkBox.setDisable(true);
        }
        return checkBox;
    }
}
