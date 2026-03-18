package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.time.temporal.TemporalField;

public class BlockedFoldersFilesOverviewController {

    @FXML private TemporalField tfFolderPath;

    @FXML private TreeTableView<BlockedFileTreeRow> treeTableBlockedFiles;
    @FXML private TreeTableColumn<BlockedFileTreeRow, String> blockedFileName;
    @FXML private TreeTableColumn<BlockedFileTreeRow, String> blockedFileClose;

    public void initialize() {
        Label emptyLabel = new Label("Nejsou blokované soubory");
        treeTableBlockedFiles.setPlaceholder(emptyLabel);
    }

    @FXML
    public void findPath(ActionEvent event) {

    }

    @FXML
    public void addPath(ActionEvent event) {

    }

    @FXML
    public void closeWindow(ActionEvent event) {

    }
}
