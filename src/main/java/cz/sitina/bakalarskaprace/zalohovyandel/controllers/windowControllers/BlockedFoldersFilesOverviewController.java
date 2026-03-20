package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.BlockedTreeTableService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.BlockedTreeTableGuiService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BlockedFoldersFilesOverviewController {

    @FXML private TextField tfFolderPath;


    @FXML private TreeTableView<BlockedFileTreeRow> treeTableBlockedFiles;
    @FXML private TreeTableColumn<BlockedFileTreeRow, String> blockedFileName;
    @FXML private TreeTableColumn<BlockedFileTreeRow, Boolean> isExistPath;
    @FXML private TreeTableColumn<BlockedFileTreeRow, String> blockedFileClose;

    private TaskModel taskModel;
    private TreeItem<BlockedFileTreeRow> rootItem;
    private Set<String> editableBlockList;

    private BlockedTreeTableGuiService blockedTreeGui = new BlockedTreeTableGuiService();
    private TreeTableGuiModify treeGui = new TreeTableGuiModify();



    public void initialize() {
        treeTableBlockedFiles.setShowRoot(false);
        treeTableBlockedFiles.setTreeColumn(blockedFileName);
        blockedFileName.setCellValueFactory(celldata ->
                celldata.getValue().getValue().getFileNameProperty());
        isExistPath.setCellValueFactory(celldata ->
                celldata.getValue().getValue().getExistProperty());
        blockedFileClose.setCellValueFactory(celldata ->
                celldata.getValue().getValue().getFileNameProperty());

        Label emptyLabel = new Label("Seznam neobshuje blokované soubory");
        treeTableBlockedFiles.setPlaceholder(emptyLabel);

        blockedFileName.getStyleClass().add("left-column");
        isExistPath.getStyleClass().add("center-column");
        blockedFileClose.getStyleClass().add("center-column");

        blockedFileName.setCellFactory(celldata ->
                new TreeTableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            ImageView image = blockedTreeGui.getIcon_BlockRow(this.getTableRow().getItem());
                            setGraphic(image);
                        }
                    }
                }
        );

        isExistPath.setCellFactory(celldata -> new TreeTableCell<>() {
                protected void updateItem(Boolean item, boolean empty){
                    super.updateItem(item, empty);

                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(null);
                        CheckBox checkBox = treeGui.getCheckBox(this.getTableRow().getTreeItem().getValue().getExist());
                        setGraphic(checkBox);
                    }
                }
        });



        blockedFileClose.setCellFactory(celldata -> new TreeTableCell<>() {
                    Button button = new Button();

                    {
                        blockedTreeGui.getButtonDeleteListItem(button, rootItem,
                            taskModel.getLeftPath(), taskModel.getRightPath(),
                            this,
                            editableBlockList);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(null);
                            setGraphic(button);
                        }
                    }
                });

    }

    @FXML
    public void findFolderPath(ActionEvent event) {
        File findFolder = FileScannerService.findDirectory_InitPath(event, taskModel.getLeftPath());
        if (findFolder == null) {
            return;
        }

        Path path = Paths.get(findFolder.getAbsolutePath());

        String chosenRelativPath = check_PathContainOneOfRootAndCutRoot(path);
        if (chosenRelativPath != null) {
            tfFolderPath.setText(chosenRelativPath);
        } else {
            ErrorUtility.getInstance("Chyba cesty",
                    "Byla zadaná špatná složka!",
                    "Cesta ke složce musí obsahovat kořen složky na levé nebo pravé straně!" +
                            "\n Kořen levá: " + taskModel.getLeftPath() +
                            "\n Kořen pravá: " + taskModel.getRightPath());
        }
    }

    @FXML
    public void findFilePath(ActionEvent event) {
        File findFile = FileScannerService.findFile_InitPath(event, taskModel.getLeftPath());
        if (findFile == null) {
            return;
        }

        Path path = Paths.get(findFile.getAbsolutePath());

        String chosenRelativPath = check_PathContainOneOfRootAndCutRoot(path);
        if (chosenRelativPath != null) {
            tfFolderPath.setText(chosenRelativPath);
        } else {
            ErrorUtility.getInstance("Chyba cesty",
                    "Byl zadán špatný soubor!",
                    "Cesta k souboru musí obsahovat kořen složky na levé nebo pravé straně!" +
                            "\n Kořen levá: " + taskModel.getLeftPath() +
                            "\n Kořen pravá: " + taskModel.getRightPath());
        }
    }

    @FXML
    public void addPath(ActionEvent event) {
        String newBlockPath = tfFolderPath.getText();
        if (newBlockPath == null || newBlockPath.isEmpty()) {
            return;
        }

        Path absolutLeftPath = (Path.of(taskModel.getLeftPath()).resolve(newBlockPath));
        Path absolutRightPath = Path.of(taskModel.getRightPath()).resolve(newBlockPath);
        if (!Files.exists(absolutLeftPath) && !Files.exists(absolutRightPath)) {
            ErrorUtility.getInstance("Chyba cesty",
                    "Zadaná cesta neexistuje!",
                    "Cesta neexistuje v levé ani pravé složce!" );
            return;
        }

        if (editableBlockList.contains(newBlockPath)) {
            ErrorUtility.getInstance("Chyba cesty",
                    "Zadaná cesta již existuje!",
                    "Seznam blokovaných souborů už tento soubor/složku obsahuje!" );
            return;
        }

        editableBlockList.add(newBlockPath);
        BlockedTreeTableService.cleanRedundantPaths(editableBlockList);

        rootItem.getChildren().clear();
        BlockedTreeTableService.buildTreeTableBlockedFiles(rootItem, editableBlockList, taskModel.getLeftPath(), taskModel.getRightPath());

    }

    @FXML
    public void closeWindow_NoSave(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }

    @FXML
    public void closeWindow_Save(ActionEvent event) {
        taskModel.getBlockList().clear();
        taskModel.getBlockList().addAll(editableBlockList);
        WindowManager.findByActionAndClose(event);
    }

    @FXML
    public void deleteList(ActionEvent event) {
        editableBlockList.clear();
        rootItem.getChildren().clear();
    }

    public String check_PathContainOneOfRootAndCutRoot(Path path) {
        if (path == null) {
            return null;
        }
        Path leftPath = Path.of(taskModel.getLeftPath());
        Path rightPath = Path.of(taskModel.getRightPath());

        Path relativePathToSave = null;
        if  (path.startsWith(leftPath)) {
            relativePathToSave = leftPath.relativize(path);
            return relativePathToSave.toString();
        }
        if (path.startsWith(rightPath)) {
            relativePathToSave = rightPath.relativize(path);
            return relativePathToSave.toString();
        }
        return null;
    }



    public void setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        this.editableBlockList = new LinkedHashSet<>(taskModel.getBlockList());


        rootItem = new TreeItem<>(new BlockedFileTreeRow(taskModel.getLeftPath(), "root", true, true, true));
        BlockedTreeTableService.buildTreeTableBlockedFiles(rootItem, editableBlockList, taskModel.getLeftPath(), taskModel.getRightPath());
        treeTableBlockedFiles.setRoot(rootItem);

        rootItem.setExpanded(true);
    }
}
