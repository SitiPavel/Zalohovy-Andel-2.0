package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.BlockedTreeTableService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.BlockedTreeTableGuiService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BlockedFoldersFilesOverviewController {

    // Pomocná třída pro volbu strany v ChoiceBoxu
    public static class RootPathChoice {
        private final int id;
        private final String label;

        public RootPathChoice(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML private TextField tfFolderPath;
    @FXML private ChoiceBox<RootPathChoice> RootPathChoiser;
    @FXML private ImageView rootPathImg;
    @FXML private VBox rootPathBox;

    @FXML private TreeTableView<BlockedFileTreeRow> treeTableBlockedFiles;
    @FXML private TreeTableColumn<BlockedFileTreeRow, String> blockedFileName;
    @FXML private TreeTableColumn<BlockedFileTreeRow, Boolean> isExistPath;
    @FXML private TreeTableColumn<BlockedFileTreeRow, String> blockedFileClose;

    @FXML private Button btnFindFolderPath;
    @FXML private Button btnFindFilePath;

    private TaskModel taskModel;
    private TreeItem<BlockedFileTreeRow> rootItem;
    private Set<String> editableBlockList;
    private Runnable isBlockSetChanged;

    private BlockedTreeTableGuiService blockedTreeGui = new BlockedTreeTableGuiService();
    private TreeTableGuiModify treeGui = new TreeTableGuiModify();

    public void initialize() {
        btnFindFolderPath.setTooltip(Utility.createToolTip_600ms("Vyhledat složku"));
        btnFindFilePath.setTooltip(Utility.createToolTip_600ms("Vyhledat soubor"));

        // PŘIDÁNO: Inicializace ChoiceBoxu pro výběr počáteční kořenové cesty
        RootPathChoice choiceLeft = new RootPathChoice(1, "Levá");
        RootPathChoice choiceRight = new RootPathChoice(2, "Pravá");
        RootPathChoiser.getItems().addAll(choiceLeft, choiceRight);
        RootPathChoiser.setValue(choiceLeft);

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

        // OPRAVENO: Přehledné zobrazení názvu pro netechnické uživatele (složky mají příponu '/') a tooltip s typem
        blockedFileName.setCellFactory(celldata ->
                new TreeTableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setTooltip(null);
                        } else {
                            BlockedFileTreeRow rowData = null;
                            if (this.getTableRow() != null) {
                                rowData = this.getTableRow().getItem();
                            }
                            if (rowData != null) {
                                boolean isFolder = rowData.isFolder() || rowData.isOrganizationFolder();
                                setText(item);
                                ImageView image = blockedTreeGui.getIcon_BlockRow(rowData);
                                setGraphic(image);

                                String tooltipText = isFolder
                                        ? "Složka (blokuje celý podsystém složky)"
                                        : "Soubor";
                                setTooltip(Utility.createToolTip_600ms(tooltipText));
                            } else {
                                setText(item);
                                setGraphic(null);
                                setTooltip(null);
                            }
                        }
                    }
                }
        );

        isExistPath.setCellFactory(celldata -> new TreeTableCell<>() {
                @Override
                protected void updateItem(Boolean item, boolean empty){
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(null);
                        CheckBox checkBox = treeGui.getCheckBox(item);
                        setGraphic(checkBox);
                    }
                }
        });

        blockedFileClose.setCellFactory(celldata -> new TreeTableCell<>() {
                    Button button = new Button();

                    {
                        button.setText("✘");
                        button.getStyleClass().addAll("WindowTitle_L1_Dark", "button_NonBackground", "buttonModify_backgroundRed");
                        button.setOnAction(event -> {
                            if (getTableRow() != null && getTableRow().getTreeItem() != null) {
                                String deletedPath = getTableRow().getTreeItem().getValue().getPath();
                                BlockedTreeTableService.removeItemAndChildren(deletedPath, editableBlockList);
                                rootItem.getChildren().clear();
                                BlockedTreeTableService.buildTreeTableBlockedFiles(rootItem, editableBlockList,
                                        taskModel.getLeftPath(), taskModel.getRightPath(), isBackupTask());
                            }
                        });
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

    private boolean isBackupTask() {
        return (taskModel instanceof BackupTask) || (taskModel instanceof EncryptedBackupTask);
    }

    private String getInitialChooserPath() {
        if (isBackupTask()) {
            return taskModel.getLeftPath();
        }
        RootPathChoice selected = RootPathChoiser.getValue();
        if (selected != null && selected.getId() == 2) {
            return taskModel.getRightPath();
        }
        return taskModel.getLeftPath();
    }

    @FXML
    public void findFolderPath(ActionEvent event) {
        String initPath = getInitialChooserPath();
        File findFolder = FileScannerService.findDirectory_InitPath(event, initPath);
        if (findFolder == null) {
            return;
        }

        Path path = Paths.get(findFolder.getAbsolutePath());
        String chosenRelativPath = check_PathContainOneOfRootAndCutRoot(path);
        if (chosenRelativPath != null) {
            tfFolderPath.setText(chosenRelativPath);
            addBlockedPathDirectly(chosenRelativPath);
        } else {
            if (isBackupTask()) {
                ErrorUtility.getInstance("Chyba cesty",
                        "Byla zadaná špatná složka!",
                        "Složka není uvnitř zdrojové složky!");
            } else {
                ErrorUtility.getInstance("Chyba cesty",
                        "Byla zadaná špatná složka!",
                        "Složka není uvnitř pravé nebo levé kořenové složky!");
            }
        }
    }

    @FXML
    public void findFilePath(ActionEvent event) {
        String initPath = getInitialChooserPath();
        File findFile = FileScannerService.findFile_InitPath(event, initPath);
        if (findFile == null) {
            return;
        }

        Path path = Paths.get(findFile.getAbsolutePath());
        String chosenRelativPath = check_PathContainOneOfRootAndCutRoot(path);
        if (chosenRelativPath != null) {
            tfFolderPath.setText(chosenRelativPath);
            addBlockedPathDirectly(chosenRelativPath);
        } else {
            if (isBackupTask()) {
                ErrorUtility.getInstance("Chyba cesty",
                        "Byl zadán špatný soubor!",
                        "Soubor není uvnitř zdrojové složky!");
            } else {
                ErrorUtility.getInstance("Chyba cesty",
                        "Byl zadán špatný soubor!",
                        "Soubor není uvnitř pravé nebo levé kořenové složky!");
            }
        }
    }

    private void addBlockedPathDirectly(String newBlockPath) {
        if (newBlockPath == null || newBlockPath.trim().isEmpty()) {
            return;
        }
        // OPRAVENO: Odstranění úvodních i koncových lomítek (/ i \), aby se uložila čistá relativní cesta
        newBlockPath = cleanRelativePath(newBlockPath);
        if (newBlockPath.isEmpty()) {
            return;
        }

        if (editableBlockList.contains(newBlockPath)) {
            ErrorUtility.getInstance("Chyba cesty",
                    "Zadaná cesta již existuje!",
                    "Seznam blokovaných souborů už tento soubor/složku obsahuje!");
            return;
        }

        // OPRAVENO: Kontrola, zda cesta není zanořena pod již blokovanou složkou
        Optional<String> parentBlockedFolder = BlockedTreeTableService.findParentBlockedFolder(newBlockPath, editableBlockList);
        if (parentBlockedFolder.isPresent()) {
            ErrorUtility.getInstance("Chyba cesty",
                    "Položka je již blokována!",
                    "Vybraná položka je zanořena v již zablokované složce \"" + parentBlockedFolder.get() + "\", proto ji nelze přidat.");
            return;
        }

        editableBlockList.add(newBlockPath);
        BlockedTreeTableService.cleanRedundantPaths(editableBlockList);

        rootItem.getChildren().clear();
        // OPRAVENO: Volání s parametrem isBackupTask() pro aktuální kontrolu existence na disku
        BlockedTreeTableService.buildTreeTableBlockedFiles(rootItem, editableBlockList,
                taskModel.getLeftPath(), taskModel.getRightPath(), isBackupTask());
    }

    // Pomocná metoda pro normalizaci a oříznutí koncových a úvodních lomítek
    private String cleanRelativePath(String pathStr) {
        if (pathStr == null) {
            return "";
        }
        pathStr = pathStr.trim();
        while (pathStr.startsWith("/") || pathStr.startsWith("\\")) {
            pathStr = pathStr.substring(1);
        }
        while (pathStr.endsWith("/") || pathStr.endsWith("\\")) {
            pathStr = pathStr.substring(0, pathStr.length() - 1);
        }
        return pathStr;
    }

    @FXML
    public void addPath(ActionEvent event) {
        String inputPath = tfFolderPath.getText();
        if (inputPath == null || inputPath.trim().isEmpty()) {
            return;
        }
        inputPath = inputPath.trim();

        String relativePathToBlock = inputPath;
        if (isAbsolutePath(inputPath)) {
            Path path = Paths.get(inputPath);
            relativePathToBlock = check_PathContainOneOfRootAndCutRoot(path);
            if (relativePathToBlock == null) {
                if (isBackupTask()) {
                    ErrorUtility.getInstance("Chyba cesty",
                            "Byla zadaná neplatná cesta!",
                            "Zadaná cesta není uvnitř zdrojové složky!");
                } else {
                    ErrorUtility.getInstance("Chyba cesty",
                            "Byla zadaná neplatná cesta!",
                            "Zadaná cesta není uvnitř pravé nebo levé kořenové složky!");
                }
                return;
            }
        }

        // Očištění koncových / nebo \ a aktualizace textového pole na čistou relativní cestu
        relativePathToBlock = cleanRelativePath(relativePathToBlock);
        tfFolderPath.setText(relativePathToBlock);

        addBlockedPathDirectly(relativePathToBlock);
    }

    private boolean isAbsolutePath(String pathStr) {
        if (pathStr == null || pathStr.length() < 2) {
            return false;
        }
        char drive = pathStr.charAt(0);
        return Character.isLetter(drive) && pathStr.charAt(1) == ':';
    }

    @FXML
    public void closeWindow_NoSave(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }

    @FXML
    public void closeWindow_Save(ActionEvent event) {
        if (!taskModel.getBlockSet().equals(editableBlockList)) {
            isBlockSetChanged.run();
            taskModel.getBlockSet().clear();
            taskModel.getBlockSet().addAll(editableBlockList);
        }

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

        if (isBackupTask()) {
            if (taskModel.getLeftPath() != null && !taskModel.getLeftPath().isEmpty()) {
                Path leftPath = Path.of(taskModel.getLeftPath());
                if (path.startsWith(leftPath)) {
                    return leftPath.relativize(path).toString();
                }
            }
            return null;
        }

        if (taskModel.getLeftPath() != null && !taskModel.getLeftPath().isEmpty()) {
            Path leftPath = Path.of(taskModel.getLeftPath());
            if (path.startsWith(leftPath)) {
                return leftPath.relativize(path).toString();
            }
        }
        if (taskModel.getRightPath() != null && !taskModel.getRightPath().isEmpty()) {
            Path rightPath = Path.of(taskModel.getRightPath());
            if (path.startsWith(rightPath)) {
                return rightPath.relativize(path).toString();
            }
        }
        return null;
    }

    public void setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        this.editableBlockList = new LinkedHashSet<>(taskModel.getBlockSet());

        if (isBackupTask()) {
            RootPathChoiser.setVisible(false);
            RootPathChoiser.setManaged(false);
        } else {
            RootPathChoiser.setVisible(true);
            RootPathChoiser.setManaged(true);
        }

        rootItem = new TreeItem<>(new BlockedFileTreeRow(taskModel.getLeftPath(), "root", true, true, true));
        // OPRAVENO: Při otevření okna (setTaskModel) dojde k prověření aktuálního stavu všech položek na disku
        BlockedTreeTableService.buildTreeTableBlockedFiles(rootItem, editableBlockList,
                taskModel.getLeftPath(), taskModel.getRightPath(), isBackupTask());
        treeTableBlockedFiles.setRoot(rootItem);

        rootItem.setExpanded(true);
    }

    public void setIsBlockSetChanged(Runnable isBlockSetChanged) {
        this.isBlockSetChanged = isBlockSetChanged;
    }
}
