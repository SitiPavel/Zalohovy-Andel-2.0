package cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations.*;

public class ChangeFolderOrFileOperationController {

    private Boolean isFolder= false;
    private ComparedItemOperItemRowModel rowItem;

    @FXML private Button btnSetOperAddLeft;
    @FXML private Button btnSetOperAddRight;
    @FXML private Button btnSetOperUpdateLeft;
    @FXML private Button btnSetOperUpdateRight;
    @FXML private Button btnSetOperDeleteLeft;
    @FXML private Button btnSetOperDeleteBoth;
    @FXML private Button btnSetOperDeleteRight;
    @FXML private Button btnSetOperBlock;
    @FXML private Button btnSetOperEqals;
    @FXML private Button btnSetOperUnknow;



    @FXML
    public void initialize() {

    }
    ///  Nastavení tlačítek
    @FXML public void setOperAddLeft(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_ADD_LEFT);
        } else {
            rowItem.setOperation(ADD_LEFT);
        }
        close(event);
    }
    @FXML public void setOperAddRight(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_ADD_RIGHT);
        } else {
            rowItem.setOperation(ADD_RIGHT);
        }
        close(event);
    }
    @FXML public void setOperUpdateLeft(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_UPDATE_LEFT);
        } else {
            rowItem.setOperation(UPDATE_LEFT);
        }
        close(event);
    }
    @FXML public void setOperUpdateRight(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_UPDATE_RIGHT);
        } else {
            rowItem.setOperation(UPDATE_RIGHT);
        }
        close(event);
    }
    @FXML public void setOperDeleteLeft(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_DELETE_LEFT);
        } else {
            rowItem.setOperation(DELETE_LEFT);
        }
        close(event);
    }
    @FXML public void setOperDeleteBoth(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_DELETE_BOTH);
        } else {
            rowItem.setOperation(DELETE_BOTH);
        }
        close(event);
    }
    @FXML public void setOperDeleteRight(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_DELETE_RIGHT);
        } else {
            rowItem.setOperation(DELETE_RIGHT);
        }
        close(event);
    }
    @FXML public void setOperBlock(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_IGNORE);
        } else {
            rowItem.setOperation(IGNORE);
        }
        close(event);
    }
    @FXML public void setOperEqals(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_EQUALS);
        } else {
            rowItem.setOperation(EQUALS);
        }
        close(event);
    }
    @FXML public void setOperUnknow(ActionEvent event) {
        if (isFolder) {
            rowItem.setOperation(FOLDER_UNKNOWN);
        } else {
            rowItem.setOperation(UNKNOWN);
        }
        close(event);
    }


    ///  pomocné metody
    public void setFolder(Boolean folder) {
        isFolder = folder;
    }

    public void setComparedItemOperItemRowModel(ComparedItemOperItemRowModel comparedItemOperItemRowModel) {
        this.rowItem = comparedItemOperItemRowModel;
        // nastavení aktivní operace do GUI
        Button activeButton = getActiveButton(comparedItemOperItemRowModel.getOperation());
        activeButton.getStyleClass().add("modificator_save");
    }

    private Button getActiveButton(CompareOperations oper)  {
        return switch (oper) {
            case ADD_LEFT, FOLDER_ADD_LEFT-> btnSetOperAddLeft;
            case ADD_RIGHT, FOLDER_ADD_RIGHT-> btnSetOperAddRight;
            case UPDATE_LEFT, FOLDER_UPDATE_LEFT-> btnSetOperUpdateLeft;
            case UPDATE_RIGHT, FOLDER_UPDATE_RIGHT-> btnSetOperUpdateRight;
            case DELETE_LEFT, FOLDER_DELETE_LEFT-> btnSetOperDeleteLeft;
            case DELETE_BOTH, FOLDER_DELETE_BOTH -> btnSetOperDeleteBoth;
            case DELETE_RIGHT, FOLDER_DELETE_RIGHT-> btnSetOperDeleteRight;
            case IGNORE, FOLDER_IGNORE ->btnSetOperBlock;
            case EQUALS, FOLDER_EQUALS -> btnSetOperEqals;
            case UNKNOWN, FOLDER_UNKNOWN-> btnSetOperUnknow;
            default -> null;
        };
    }

    private void close(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
