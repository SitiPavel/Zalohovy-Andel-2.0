package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeBuilderUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeBuilderUtility.FolderNode;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RootFolderFilterController {
    private String rootPath;

    // od original task
    private List<String> originFilteredFolders;
    // od editable task
    private List<String> editFilteredFolders;
    private Map<String, String> foundedFolders;
    // list položek stromu pro zjištění odškrtnutých položek
    private final List<CheckBoxTreeItem<FolderNode>> treeItemList = new ArrayList<>();
    private FileScannerService fileScannerService = new FileScannerService();

    // cesty
    private String originalPath;
    private String editedPath;

    // callBack
    private Runnable isChanged;

    @FXML private Label lbRootPath;
    @FXML private TreeView<FolderNode> folderTreeView;

    @FXML
    public void setChanges(ActionEvent event) {
        editFilteredFolders.clear();
        // OPRAVENO: Procházíme položky stromu a nezaškrtnuté ukládáme do seznamu filtrovaných (vynechaných) složek
        for (CheckBoxTreeItem<FolderNode> item : treeItemList) {
            if (!item.isSelected()) {
                editFilteredFolders.add(item.getValue().getAbsolutePath());
            }
        }
        if (!originFilteredFolders.equals(editFilteredFolders) && isChanged != null) {
            isChanged.run();
        }

        WindowManager.findByActionAndClose(event);
    }

    public void setRootPathTree(String path) {
        this.rootPath = path;
        lbRootPath.setText(rootPath);
        lbRootPath.setTooltip(Utility.createToolTip_600ms(rootPath));

        // získání cest ke složkám
        foundedFolders = fileScannerService.loadDirectSubfolders(Path.of(rootPath));

        // Určení aktuálně filtrovaných složek s ohledem na shodu původní a editované cesty
        List<String> activeFilteredList = new ArrayList<>();
        if (originalPath != null && originalPath.equals(editedPath) && editFilteredFolders != null) {
            activeFilteredList = editFilteredFolders;
        }

        TreeBuilderUtility.buildChackBoxDirTree(
                folderTreeView,
                rootPath,
                foundedFolders,
                activeFilteredList,
                treeItemList
        );
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public void setEditedPath(String editedPath) {
        this.editedPath = editedPath;
    }

    public void setEditFilteredFolders(List<String> rootFilteredFilter) {
        this.editFilteredFolders = rootFilteredFilter;
    }

    public void setOriginFilteredFolders(List<String> originFilteredFolders) {
        this.originFilteredFolders = originFilteredFolders;
    }

    public void setIsChanged(Runnable isChanged) {
        this.isChanged = isChanged;
    }
}
