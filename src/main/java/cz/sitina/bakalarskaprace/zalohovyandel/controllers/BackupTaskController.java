package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.CompareProgresController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;
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
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableBuilderService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
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


    private BackupCompareService backupCompareService = new BackupCompareService();

    private TreeTableGuiModify treeTableGuiModify = new TreeTableGuiModify();
    private TreeTableBuilderService treeBuilder = new TreeTableBuilderService();


    // objekty
    private TaskRepository taskRepository;
    private BackupTask originBackupTask;
    private BackupTask editableBackupTask;
    private AppFrameController appFrameController;
    private TreeItem<ComparedItemOperItemRowModel> masterRoot;


    private BooleanProperty isDirtyFlag = new SimpleBooleanProperty(false);
    private boolean isSourceOperationSizeSmallerThenDestinationFolder;

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


        Label tablePlaceholder = new Label("Není porovnaná zdrojová složka s cílovou, nutno porovnat!");
        tablePlaceholder.getStyleClass().add("DarkBoltHeader1");
        filesTreeTable.setPlaceholder(tablePlaceholder);

        /// NASTAVENÍ SLOUPCŮ
        // Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModify.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");

        // nastaví jako hlavní zavírací strom sloupec leftColName
        filesTreeTable.setTreeColumn(leftColName);

        // nastavení ROOT
        filesTreeTable.setShowRoot(false);
        masterRoot = new TreeItem<>(new ComparedItemOperItemRowModel(
                new FileItemModel("/"),
                CompareOperations.FOLDER_UNEVEN,
                new FileItemModel("/")) );
        filesTreeTable.setRoot(masterRoot);
        masterRoot.setExpanded(true);


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

        // nastavení generování Obarvených řádků podle operace
        treeTableGuiModify.rowFormatingByOperation(filesTreeTable);

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
        ComparedDataAndStatisticsForCompereOverview comparedDataAndStatistics =
                backupCompareService.executeBackupCompare(editableBackupTask);

        treeBuilder.buildTreeTableItemOperItem(masterRoot, comparedDataAndStatistics.getComparedFilesList());

        loadFinishedDataToGui(comparedDataAndStatistics, lbSourceFolderPath.getText(), lbSourceFolderPath.getText());





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




    private void loadFinishedDataToGui(ComparedDataAndStatisticsForCompereOverview data, String sourceFolder,
                                       String destinationFolder) {

        // nastavení statistik o disku v GUI
        DiskSpaceStatisticData leftFolderStatistic = utility.getDiskSpaceStatistics(sourceFolder);
        lbLeftUsedSpace.setText(utility.getFormatedSize(leftFolderStatistic.getUsedSpace()));
        lbLeftTotalSpace.setText(utility.getFormatedSize(leftFolderStatistic.getTotalSpace()));
        lbLeftFreeSpace.setText(utility.getFormatedSize(leftFolderStatistic.getFreeSpace()));
        pbLeftUsedSpace.setProgress((double) leftFolderStatistic.getUsedSpace() /leftFolderStatistic.getTotalSpace());
        DiskSpaceStatisticData rightFolderStatistic = utility.getDiskSpaceStatistics(destinationFolder);
        lbRightUsedSpace.setText(utility.getFormatedSize(rightFolderStatistic.getUsedSpace()));
        lbRightTotalSpace.setText(utility.getFormatedSize(rightFolderStatistic.getTotalSpace()));
        lbRightFreeSpace.setText(utility.getFormatedSize(rightFolderStatistic.getFreeSpace()));
        pbRightUsedSpace.setProgress((double) rightFolderStatistic.getUsedSpace() /rightFolderStatistic.getTotalSpace());


        loadCompateDataAfterChange(data, destinationFolder);

        // nastavíme operace pro složky
        TreeTableFolderOperationMakerService.getFolderOperation(masterRoot);

    }

    private void loadCompateDataAfterChange(ComparedDataAndStatisticsForCompereOverview data, String destinationFolder) {
        lbRightAddCount.setText(String.valueOf(data.getRightAddCount()));
        lbRightUpdateCount.setText(String.valueOf(data.getRightUpdateCount()));
        lbEqualsCount.setText(String.valueOf(data.getEqualsCount()));
        lbIgnoreCount.setText(String.valueOf(data.getIgnoreCount()));
        lbUnknownCount.setText(String.valueOf(data.getUnknownCount()));

        if (utility.isSazeSmalerThenFreeSpace(destinationFolder,
                data.getDataTransportSize_right())) {
            imgPieChar_Right.setImage(iconManager.getPieChartYellow_ToRight()) ;
            isSourceOperationSizeSmallerThenDestinationFolder =true;
        } else {
            imgPieChar_Right.setImage(iconManager.getPieChartRed_ToRight());
            isSourceOperationSizeSmallerThenDestinationFolder = false;
        }

        lbPieChar_Right.setText(String.valueOf(
                utility.getFormatedSize(
                        data.getDataTransportSize_right()
                )
        ));
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
        lbDestinationFolderPathContent.setText(originBackupTask.getRightPath());
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
