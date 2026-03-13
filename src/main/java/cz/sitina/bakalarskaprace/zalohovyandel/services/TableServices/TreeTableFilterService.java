package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.scene.control.TreeItem;

public class TreeTableFilterService {

    public static TreeItem<ComparedItemOperItemRowModel> createFiltredTree(
            TreeItem<ComparedItemOperItemRowModel> originalNode,
            GuiOperationFilters_TaskData taskFilters) {

        if (originalNode == null) {return null;}

        //Vytvoření nové kopie uzlu
        ComparedItemOperItemRowModel data = originalNode.getValue();
        TreeItem<ComparedItemOperItemRowModel> filtredNode = new TreeItem<>(data);
        filtredNode.setExpanded(originalNode.isExpanded());

        boolean hasVisibleChildren = false;

        // využití rekurze pro projití potomklů
        for (TreeItem<ComparedItemOperItemRowModel> child : originalNode.getChildren()) {

            // na každého potomka aplikujeme filtrování
            TreeItem<ComparedItemOperItemRowModel> filteredChild = createFiltredTree(child, taskFilters);

            if (filteredChild != null) {
                filtredNode.getChildren().add(filteredChild);
                hasVisibleChildren = true;
            }
        }

        // rozhodovací metoda
        if (data == null) {
            return filtredNode;
        }

        // Identifikace složky
        boolean isFolder = data.getOperation()== CompareOperations.FOLDER || !originalNode.getChildren().isEmpty();
        if (isFolder) {
            // pokud složka obsahuje nějaký obsah tak jí zobrazí, když je obsah vyfiltrován tak bude neviditelná
            return hasVisibleChildren ? filtredNode : null;
        } else {
            boolean passedFilters = evaluateFilters(data.getOperation(), taskFilters);
            return passedFilters ? filtredNode : null;
        }
    }

    private static boolean evaluateFilters(CompareOperations operation, GuiOperationFilters_TaskData taskFilters ) {
        if (operation == null) {
            return true;
        }

        return switch (operation) {
            case EQUALS -> taskFilters.isActive_EQUALS();
            case IGNORE -> taskFilters.isActive_IGNORE();
            case DELETE_LEFT -> taskFilters.isActive_DELETE_LEFT();
            case DELETE_RIGHT -> taskFilters.isActive_DELETE_RIGHT();
            // vrátí true když isActive_DELETE_LEFT nebo isActive_DELETE_RIGHT je true
            case DELETE_BOTH -> taskFilters.isActive_DELETE_LEFT() || taskFilters.isActive_DELETE_RIGHT();
            case UPDATE_LEFT -> taskFilters.isActive_UPDATE_LEFT();
            case UPDATE_RIGHT -> taskFilters.isActive_UPDATE_RIGHT();
            case ADD_LEFT -> taskFilters.isActive_ADD_LEFT();
            case ADD_RIGHT -> taskFilters.isActive_ADD_RIGHT();
            case UNKNOWN -> taskFilters.isActive_UNKNOWN();
            default -> true;
        };

    }
}
