package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.CompareProgresController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.BackupCompareServices.BackupCompareService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

import static cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager.loadFXML;

public class BackupTaskController implements TaskController {

    // třídy poskytující služby
    private IconManagerUtility iconManager = new IconManagerUtility();
    private JsonDataManagerService jsonManager = new JsonDataManagerService();
    private Utility utility = new Utility();
    private TreeTableGuiModify treeTableGuiModify = new TreeTableGuiModify();
    private ComparisonService comparisonService = new ComparisonService();
    private FileScannerService fileScannerService = new FileScannerService();
    private BackupCompareService backupCompareService = new BackupCompareService();


    // objekty
    private TaskRepository taskRepository;
    private BackupTask originBackupTask;
    private BackupTask editableBackupTask;
    private AppFrameController appFrameController;

    private BooleanProperty isDirtyFlag = new SimpleBooleanProperty(false);
    private CompareStrategy compareStrategy;

    // Nastavení sloupců v TreeTableView  a samotná tabulka
    @FXML private TreeTableView<ComparedItemOperItemRowModel> filesTreeTable;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColLastModify;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol;


    /// rostě info o root složkách
    @FXML private Label lbSourceFolderPath;
    @FXML private Label lbLeftUsedSpace;
    @FXML private Label lbLeftTotalSpace;
    @FXML private Label lbLeftFreeSpace;
    @FXML private ProgressBar pbLeftUsedSpace;


    @FXML private Label lbDestinationFolderPath;
    @FXML private Label lbRightUsedSpace;
    @FXML private Label lbRightTotalSpace;
    @FXML private Label lbRightFreeSpace;
    @FXML private ProgressBar pbRightUsedSpace;


    /// definocvané GUI objekty
    @FXML private Button btnCompareFolders;
    @FXML private Button btnSaveTask;


    /// definování ukazatelu a filtrování
    @FXML private Label lbIgnoreCount;
    @FXML private Label lbEqualsCount;
    @FXML private Label lbUnknownCount;
    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    // nutné deklarování pro ovládání rychlích filtrů

    @FXML private Button btnAddItem_right;
    @FXML private Button btnModified_right;
    @FXML private Button btnEqual;
    @FXML private Button btnBlock;
    @FXML private Button btnUnknown;


    @FXML private ImageView imgAddItem_right;
    @FXML private ImageView imgModified_right;
    @FXML private ImageView imgEqual;
    @FXML private ImageView imgBlock;
    @FXML private ImageView imgUnknown;


    // objekty v na levé straně symbolizující cíl

    @FXML private ImageView imgTypeOfBackup;
    @FXML private ImageView imgPieChar_Right;
    @FXML private Label lbPieChar_Right;
    @FXML private Label lbDestinationFolderPathContent;

    @FXML
    private void initialize() {
        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedName());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedSize());
        leftColLastModify.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedLastModified());

        operationCol.setCellValueFactory(cellData ->
                cellData.getValue().getValue().operationProperty());



        /// NASTAVENÍ SLOUPCŮ
        // Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModify.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");

        // nastaví jako hlavní zavírací strom sloupec leftColName
        filesTreeTable.setTreeColumn(leftColName);

        // nastavení levého sloupce názvů
        leftColName.setCellFactory(cellData ->
                new TreeTableCell<ComparedItemOperItemRowModel, String>() {

                    Tooltip tooltip = utility.createToolTip_600ms();
                    // nastavení obrázku pro oddělení složky a souboru

                    ImageView view = new ImageView();

                    @Override
                    protected void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setTooltip(null);
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                            tooltip.setText(item);
                            setTooltip(tooltip);

                            view = treeTableGuiModify.getIconByRowFormat(
                                    getTreeTableRow().getItem().getOperation(),
                                    ""
                            );
                            setGraphic(view);
                        }

                    }
                });

        operationCol.setCellFactory(col  -> {

            TreeTableCell<ComparedItemOperItemRowModel, CompareOperations> cell = new TreeTableCell<>() {

                private final ImageView imageView = treeTableGuiModify.findAndSetForOperationIcon(this);
                Image icon = null;

                @Override
                protected void updateItem(CompareOperations operation, boolean empty) {
                    super.updateItem(operation, empty);

                    if (empty || operation == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        icon = treeTableGuiModify.getIconForOperation(operation);
                        imageView.setImage(icon);

                        setGraphic(imageView);
                        setText(null);
                    }
                }
            };
            /*
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    ComparedItemOperItemRowModel rowItem = cell.getTreeTableRow().getItem();

                    if (rowItem != null) {
                        popupDialogService.showOperationPopupWindow(cell.getTreeTableRow().getTreeItem(),
                                ()-> readAndSetStatistic());
                    }

                    // Zastavení dalšího probublávání události
                    event.consume();
                }

            });
            */
            return cell;
        });


    }



    /// ==============================
    /// definování akcí tlačítek
    @FXML
        private void sourceRootFilter(ActionEvent event) {

    }

    @FXML // nastavení porovnání
    private void startSettingsOfComparing(ActionEvent event) {

    }

    @FXML // porovnání souborů
    private void startComparingFiles(ActionEvent event) {
        ComparedDataAndStatisticsForCompereOverview coparedDataAndStatistics =
                backupCompareService.executeBackupCompare(editableBackupTask);

    }

    @FXML //
    private void showTaskOverviewGUI(ActionEvent event) {
        appFrameController.showTasksOverview();
    }

    @FXML
    private void saveEditableTask(ActionEvent event) {
        saveChanges();
        isDirtyFlag.set(false);
        jsonManager.saveAllTask_SaveOldSave(taskRepository.getTaskObservableList());
    }

    @FXML
    private void startRecapitulationForCopy(ActionEvent event) {

    }

    @FXML
    private void showBlockList(ActionEvent event) {

    }

    @FXML
    private void showJournal(ActionEvent event) {

    }

    @FXML void setFastFilter(ActionEvent event) {

    }






    // nastaví podle true/false obrázek odpovídajcí vypnutému nebo zapnutému filtru
    private void setFastFiltersImg() {
        GuiOperationFilters_TaskData taskFilters = editableBackupTask.getGuiOperationFilters();
        imgAddItem_right.setImage(
                taskFilters.isActive_ADD_RIGHT() ? iconManager.getIcon_addItem_right() : iconManager.getIcon_addItem_right_OFF()
        );
        imgModified_right.setImage(
                taskFilters.isActive_UPDATE_RIGHT() ? iconManager.getIcon_modified_right() : iconManager.getIcon_modified_right_OFF()
        );
        imgEqual.setImage(
                taskFilters.isActive_EQUALS() ? iconManager.getIcon_equal() : iconManager.getIcon_equal_OFF()
        );
        imgBlock.setImage(
                taskFilters.isActive_IGNORE() ? iconManager.getIcon_block() : iconManager.getIcon_block_OFF()
        );
        imgUnknown.setImage(
                taskFilters.isActive_UNKNOWN() ? iconManager.getIcon_unknown() : iconManager.getIcon_unknown_OFF()
        );


    }



    public boolean isUnsavedChanges() {
        return isDirtyFlag.get();
    }

    public void saveChanges() {
        this.originBackupTask.updateTaskModel(editableBackupTask);
        isDirtyFlag.set(false);
    }

    // setry

    public void loadView() {
        lbSourceFolderPath.setText(originBackupTask.getLeftPath());
        lbDestinationFolderPath.setText(originBackupTask.getRightPath());
        lbDestinationFolderPathContent.setText(originBackupTask.getLeftPath());
    }


    public void setTaskModel(TaskModel task) {
        BackupTask backupTask = (BackupTask) task;

        this.originBackupTask = backupTask;
        this.editableBackupTask = new BackupTask(backupTask);

        setFastFiltersImg();
    }


    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }

}
