package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.CompareProgresController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.ManualCompareSettingsController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.RecapitulationBeforeCopyFilesController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableBuilderService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFilterService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


import java.util.List;
import java.util.Map;

import static cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager.loadFXML;

public class ManualSyncController implements TaskController {

    // Nastavení sloupců v TreeTableView  a samotná tabulka
    @FXML private TreeTableView<ComparedItemOperItemRowModel> filesTreeTable;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColLastModify;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColLastModify;

    // daůší definované třídy atd..

    private final FileScannerService fileScannerService = new FileScannerService();
    private TreeTableBuilderService treeTableBuilder = new TreeTableBuilderService();
    private final IconManagerUtility imageManager = new IconManagerUtility();
    private final PopupDialogService popupDialogService = new PopupDialogService();


    private final ComparisonService comparisonService = new ComparisonService();
    private final CompareOverviewService compareOverviewService = new CompareOverviewService();
    private final TreeTableGuiModify treeTableGuiModify = new TreeTableGuiModify();
    private final JsonDataManagerService jsonManager = new JsonDataManagerService();
    private final Utility utility = new Utility();
    private AppFrameController appFrameController;




    // definované důležité proměnné
    private List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
    private TreeItem<ComparedItemOperItemRowModel> masterRoot;

    private final ManualCompareSettingsController compareSetting = new ManualCompareSettingsController();
    private CompareStrategy strategyForRecapitulate;
    private boolean isLeftOperationSizeSmalerThenLeftFolder;
    private boolean isRightOperationSizeSmalerThenRightFolder;

    //Tásk objekt a uložení
    TaskRepository taskRepository;
    private ManualSyncTask originalManualTask;
    private ManualSyncTask editableManualTask ;

    //private boolean dirtyFlag = false;
    private BooleanProperty isDirtyFlag = new SimpleBooleanProperty(false);

    // definované datové sady
    private ComparedDataAndStatisticsForCompereOverview statisticsAndComparedData;

    ///======================================
    /// definocvané GUI objekty
    @FXML private Button btnLeftFileChoiser;
    @FXML private TextField tfLeftFolderPath;
    @FXML private Button btnRightFileChoiser;
    @FXML private TextField tfRightFolderPath;
    @FXML private Button btnCompareFolders;
    @FXML private Button btnSaveTask;


    // definování ukazatelu
    @FXML private Label lbLeftAddCount;
    @FXML private Label lbLeftUpdateCount;
    @FXML private Label lbLeftDeleteCount;
    @FXML private Label lbIgnoreCount;
    @FXML private Label lbEqualsCount;
    @FXML private Label lbUnknownCount;
    @FXML private Label lbRightDeleteCount;
    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    // nutné deklarování pro ovládání rychlích filtrů

    /*  =======^=======^=======^=======^=======*/
    @FXML private Button btnAddItem_Left;
    @FXML private Button btnModified_left;
    @FXML private Button btnDelete_left;
    @FXML private Button btnBlock;
    @FXML private Button btnEqual;
    @FXML private Button btnUnknown;
    @FXML private Button btnDelete_right;
    @FXML private Button btnModified_right;
    @FXML private Button btnAddItem_right;
    /**/

    @FXML private ImageView imgAddItem_Left;
    @FXML private ImageView imgModified_left;
    @FXML private ImageView imgDelete_left;
    @FXML private ImageView imgBlock;
    @FXML private ImageView imgEqual;
    @FXML private ImageView imgUnknown;
    @FXML private ImageView imgDelete_right;
    @FXML private ImageView imgModified_right;
    @FXML private ImageView imgAddItem_right;





    // ukazatelé statistik disků
    @FXML private Label lbLeftUsedSpace;
    @FXML private Label lbLeftTotalSpace;
    @FXML private Label lbLeftFreeSpace;
    @FXML private ProgressBar pbLeftUsedSpace;

    @FXML private Label lbRightUsedSpace;
    @FXML private Label lbRightTotalSpace;
    @FXML private Label lbRightFreeSpace;
    @FXML private ProgressBar pbRightUsedSpace;

    @FXML private ImageView imgPieChar_Left;
    @FXML private Label lbPieChar_Left;

    @FXML private ImageView imgPieChar_Right;
    @FXML private Label lbPieChar_Right;



    public ManualSyncController(){}

    @FXML
    public void initialize() {
        //Definování obsahu sloupců ve filesTreeTable
        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedName());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedSize());
        leftColLastModify.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedLastModified());

        operationCol.setCellValueFactory(cellData ->
                cellData.getValue().getValue().operationProperty());

        rightColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedName());
        rightColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedSize());
        rightColLastModify.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedLastModified());


        /// NASTAVENÍ SLOUPCŮ
        // Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModify.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");
        rightColSize.getStyleClass().add("center-column");
        rightColLastModify.getStyleClass().add("center-column");



        // nastaví jako hlavní zavírací strom sloupec leftColName
        filesTreeTable.setTreeColumn(leftColName);
        // nastavení levého sloupce názvů
        leftColName.setCellFactory(cellData ->
                new TreeTableCell<ComparedItemOperItemRowModel, String>() {

            Tooltip tooltip = utility.createToolTip_600ms();
            // nastavení obrázku pro oddělení složky a souboru

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

                    ImageView view = treeTableGuiModify.getIconByRowFormat(
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

                @Override
                protected void updateItem(CompareOperations operation, boolean empty) {
                    super.updateItem(operation, empty);

                    if (empty || operation == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        Image icon = treeTableGuiModify.getIconForOperation(operation);
                        imageView.setImage(icon);

                        setGraphic(imageView);
                        setText(null);
                    }
                }
            };
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
            return cell;
        });

        // nastavení pravého sloupce názvů
        rightColName.setCellFactory(cellData ->
            new TreeTableCell<ComparedItemOperItemRowModel, String>() {
                // 1. Vytvoříme Tooltip pouze JEDNOU pro životní cyklus buňky
                private final Tooltip myTooltip = new Tooltip();


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
                    setText(item);

                    // Nastavení ikony podle formátu
                    myTooltip.setText(item);
                    setTooltip(myTooltip);
                    ImageView view = treeTableGuiModify.getIconByRowFormat(
                            getTreeTableRow().getItem().getOperation(),
                            ""
                    );
                    setGraphic(view);

                    //nastavení odsazení podle zanoření v TreeTable
                    treeTableGuiModify.setIndentByDeepForCell(getIndex(), getTreeTableView(), this);

                }
            });



        filesTreeTable.setShowRoot(false);

        // nastavení generování Obarvených řádků podle operace
        treeTableGuiModify.rowFormatingByOperation(filesTreeTable);

        setIsDirtyFlagListener();
    }

    public void readAndSetStatistic() {
        ComparedDataAndStatisticsForCompereOverview statistics = new ComparedDataAndStatisticsForCompereOverview();
        TreeTableUtility.readStatisticForOperationBackupSize(masterRoot, statistics);
        loadCompateDataAfterChange(statistics ,tfLeftFolderPath.getText(), tfRightFolderPath.getText());

    }

    public boolean isUnsavedChanges() {
        return isDirtyFlag.get();
    }

    public void saveChanges() {
        this.originalManualTask.updateTaskModel(editableManualTask);
        isDirtyFlag.set(false);
    }


    ///===================
    /// Metody reagující na uživatele

    @FXML
    public void choseLeftFolder(ActionEvent event) {
        File directory = compareOverviewService.findDirectory(event);
        if (directory != null) {
            tfLeftFolderPath.setText(directory.getAbsolutePath());
        }

    }

    @FXML
    public void startSettingsOfComparing(ActionEvent event){
        try {
            FXMLLoader loader = loadFXML("manualCompareSetings.fxml");
            ManualCompareSettingsController controller = loader.getController();
            controller.setTask(editableManualTask);
            controller.setCallbackDirtyFlag(() -> {
                markAsDirty();
            });

            WindowManager.openNewWindow( loader,
                    "Nastavení porovnání souborů",
                    720,
                    470);

        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading(e);
        }
    }

    @FXML
    public void startComparingFiles(ActionEvent event) {
        strategyForRecapitulate = editableManualTask.getCompareStrategy();
        try {
            FXMLLoader loader = loadFXML("compareProgresWindow.fxml");
            CompareProgresController progressWindowController = loader.getController();
            Stage progresStage = WindowManager.openRunningWindow(loader, "Průběh porovnání", 420, 240);

            // do comparisonTask nám vrátí data v objektu ComparedDataAndStatisticsForCompereOverview
            Task<ComparedDataAndStatisticsForCompereOverview> comparisonTask = new Task<>() {
                @Override
                protected ComparedDataAndStatisticsForCompereOverview call() throws Exception {

                    Map<String, FileItemModel> mapLeft = fileScannerService.scanFolder(tfLeftFolderPath.getText());
                    Map<String, FileItemModel> mapRight = fileScannerService.scanFolder(tfRightFolderPath.getText());

                    Platform.runLater(()->{
                        progressWindowController.setLbHeader("Probíhá porovnání souborů.");
                    });

                    return comparisonService.compareFolders(
                            mapLeft,
                            mapRight,
                            editableManualTask,
                            (current, total) -> {
                                updateProgress(current, total);
                                updateMessage(String.valueOf(current));
                                updateTitle(String.valueOf(total));
                            },
                            this::isCancelled
                    );
                }
            };

            progressWindowController.startComparisonWithProgressWindow(comparisonTask);

            // nastaví co se stane po dokončení
            comparisonTask.setOnSucceeded(e -> {
                progresStage.close();

                ComparedDataAndStatisticsForCompereOverview resultData = comparisonTask.getValue();

                if (resultData != null) {
                    loadFinishedDataToGui(resultData, tfLeftFolderPath.getText(), tfRightFolderPath.getText());
                }
            });

            // co se stane při chybě
            comparisonTask.setOnFailed(e -> {
                progresStage.close();
                ErrorUtility.getInstance("Kritická chyba", "Chyba během procesu porovnání." ,e.toString());
                System.out.println(e.toString());
            });

            // co se stane při zrušení
            comparisonTask.setOnCancelled(e -> progresStage.close());

            Thread thread = new Thread(comparisonTask);
            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading(e);
        }
    }

    @FXML
    public void showTaskOverviewGUI(ActionEvent event) {
        appFrameController.showTasksOverview();
    }

    @FXML
    public void saveEditableTask(ActionEvent event) {
        saveChanges();
        isDirtyFlag.set(false);
        jsonManager.saveAllTask_SaveOldSave(taskRepository.getTaskObservableList());
    }

    @FXML
    public void startRecapitulationForCopy(ActionEvent event) {
        if (statisticsAndComparedData.isDataMoveOperations()) {
            // kontrola jestli se záloha vejde do disku
            if (!isRightOperationSizeSmalerThenRightFolder || !isLeftOperationSizeSmalerThenLeftFolder) {
                ErrorUtility.getInstance("Chyba disku",
                        "Synchronizace nebo záloha je příliž velká!",
                        "Velikost operací do levého nebo pravého uložiště je příliž velká!");
                return;
            }
            try {
                FXMLLoader loader = loadFXML("recapitulationBeforeCopyFiles.fxml");
                RecapitulationBeforeCopyFilesController controller = loader.getController();
                controller.setTask(editableManualTask);
                controller.setComparedAndStatisticsData(statisticsAndComparedData);
                controller.setStrategyModified(false);
                controller.setList(resultComparedList);
                controller.setStrategy(strategyForRecapitulate);
                controller.setText_SizeToLeft(lbPieChar_Left.getText());
                controller.setText_SizeToRight(lbPieChar_Right.getText());
                controller.setGui();

                WindowManager.openNewWindow(loader, "Rekapitulace", 372, 350);


            } catch (IOException e) {
                System.out.println(e.toString());
                ErrorUtility.showError_WindowLoading(e);
            }
        } else {
            ErrorUtility.getInstance("Chyba",
                    "V porovnání nejsou položky, vyžadují manipulaci",
                    "Aby mohla být synchronizace/záloha spuštěna musí obsahovat alespoň jednu z těchto operací: " +
                            "PŘIDAT, UPRAVIT, SMAZAT na libovolnou stranu");
        }
    }

    @FXML
    public void choseRightFolder(ActionEvent event) {
        File directory = compareOverviewService.findDirectory(event);
        if (directory != null) {
            tfRightFolderPath.setText(directory.getAbsolutePath());
        }
    }

    @FXML // když klikneme na tlačítko tak se změní filtr z TRUE > FALSE a naopak
    public void setFastFilter(ActionEvent event) {
        Node source = (Node) event.getSource();

        setImageByBtnID(source.getId(), editableManualTask.getGuiOperationFilters());
        markAsDirty();

        applyCurrentFiltersToTable();
    }


    //==================
    // obecné nápomocné metody


    // stará se o poslání objektů a načítá veškerá data
    private void loadFinishedDataToGui(ComparedDataAndStatisticsForCompereOverview data,
                                       String leftFolder, String rightFolder)
    {
        this.statisticsAndComparedData = data;

        // nastavení statistik o disku v GUI
        DiskSpaceStatisticData leftFolderStatistic = utility.getDiskSpaceStatistics(leftFolder);
        lbLeftUsedSpace.setText(utility.getFormatedSize(leftFolderStatistic.getUsedSpace()));
        lbLeftTotalSpace.setText(utility.getFormatedSize(leftFolderStatistic.getTotalSpace()));
        lbLeftFreeSpace.setText(utility.getFormatedSize(leftFolderStatistic.getFreeSpace()));
        pbLeftUsedSpace.setProgress((double) leftFolderStatistic.getUsedSpace() /leftFolderStatistic.getTotalSpace());
        DiskSpaceStatisticData rightFolderStatistic = utility.getDiskSpaceStatistics(rightFolder);
        lbRightUsedSpace.setText(utility.getFormatedSize(rightFolderStatistic.getUsedSpace()));
        lbRightTotalSpace.setText(utility.getFormatedSize(rightFolderStatistic.getTotalSpace()));
        lbRightFreeSpace.setText(utility.getFormatedSize(rightFolderStatistic.getFreeSpace()));
        pbRightUsedSpace.setProgress((double) rightFolderStatistic.getUsedSpace() /rightFolderStatistic.getTotalSpace());

        loadCompateDataAfterChange(data, leftFolder, rightFolder);

        // strom
        resultComparedList = statisticsAndComparedData.getComparedFilesList();
        masterRoot = new TreeItem<>(
                new ComparedItemOperItemRowModel(new FileItemModel("/"),
                        CompareOperations.FOLDER_UNEVEN,
                        new FileItemModel("/"))
        );

        masterRoot.setExpanded(true);


        //  vložíme esitující objekty a metoda nám z nich do root nastaví řádky
        treeTableBuilder.setTreeTable(masterRoot, resultComparedList);

        // nastavíme operace pro složky
        TreeTableFolderOperationMakerService.getFolderOperation(masterRoot);

        applyCurrentFiltersToTable();
        setupDirtyTrackers();

    }
    public void loadCompateDataAfterChange(ComparedDataAndStatisticsForCompereOverview statisticsAndComparedData,
                                           String leftFolder, String rightFolder) {

        // nastavení textu do labels
        lbLeftAddCount.setText(String.valueOf(statisticsAndComparedData.getLeftAddCount()));
        lbLeftUpdateCount.setText(String.valueOf(statisticsAndComparedData.getLeftUpdateCount()));
        lbLeftDeleteCount.setText(String.valueOf(statisticsAndComparedData.getLeftDeleteCount()));
        lbIgnoreCount.setText(String.valueOf(statisticsAndComparedData.getIgnoreCount()));
        lbEqualsCount.setText(String.valueOf(statisticsAndComparedData.getEqualsCount()));
        lbUnknownCount.setText(String.valueOf(statisticsAndComparedData.getUnknownCount()));
        lbRightDeleteCount.setText(String.valueOf(statisticsAndComparedData.getRightDeleteCount()));
        lbRightUpdateCount.setText(String.valueOf(statisticsAndComparedData.getRightUpdateCount()));
        lbRightAddCount.setText(String.valueOf(statisticsAndComparedData.getRightAddCount()));

        /// zobrazení plánované
        // nastavení levého koláčového grafu
        if (utility.isSazeSmalerThenFreeSpace(  leftFolder,
                statisticsAndComparedData.getDataTransportSize_left())) {
            imgPieChar_Left.setImage(imageManager.getPieChartYellow_ToLeft());
            isLeftOperationSizeSmalerThenLeftFolder =true;
        } else {
            imgPieChar_Left.setImage(imageManager.getPieChartRed_ToLeft());
            isLeftOperationSizeSmalerThenLeftFolder = false;
        }

        if (utility.isSazeSmalerThenFreeSpace(  rightFolder,
                statisticsAndComparedData.getDataTransportSize_right())) {
            imgPieChar_Right.setImage(imageManager.getPieChartYellow_ToRight()) ;
            isRightOperationSizeSmalerThenRightFolder =true;
        } else {
            imgPieChar_Right.setImage(imageManager.getPieChartRed_ToRight());
            isRightOperationSizeSmalerThenRightFolder = false;
        }

        // nastavení textů do koláču
        lbPieChar_Left.setText(String.valueOf(
                utility.getFormatedSize(
                        statisticsAndComparedData.getDataTransportSize_left()
                )
        ));
        lbPieChar_Right.setText(String.valueOf(
                utility.getFormatedSize(
                        statisticsAndComparedData.getDataTransportSize_right()
                )
        ));

    }

    // nastaví podle true/false obrázek odpovídajcí vypnutému nebo zapnutému filtru
    private void setFastFiltersImg() {
        GuiOperationFilters_TaskData taskFilters = editableManualTask.getGuiOperationFilters();
        imgAddItem_Left.setImage(
                taskFilters.isActive_ADD_LEFT() ? imageManager.getIcon_addItem_Left() : imageManager.getIcon_addItem_Left_OFF()
        );
        imgModified_left.setImage(
                taskFilters.isActive_UPDATE_LEFT() ? imageManager.getIcon_modified_left()  : imageManager.getIcon_modified_left_OFF()
        );
        imgDelete_left.setImage(
                taskFilters.isActive_DELETE_LEFT() ? imageManager.getIcon_delete_left() : imageManager.getIcon_delete_left_OFF()
        );
        imgBlock.setImage(
                taskFilters.isActive_IGNORE() ? imageManager.getIcon_block() : imageManager.getIcon_block_OFF()
        );
        imgEqual.setImage(
                taskFilters.isActive_EQUALS() ? imageManager.getIcon_equal() : imageManager.getIcon_equal_OFF()
        );
        imgUnknown.setImage(
                taskFilters.isActive_UNKNOWN() ? imageManager.getIcon_unknown() : imageManager.getIcon_unknown_OFF()
        );
        imgDelete_right.setImage(
                taskFilters.isActive_DELETE_RIGHT() ? imageManager.getIcon_delete_right() : imageManager.getIcon_delete_right_OFF()
        );
        imgModified_right.setImage(
                taskFilters.isActive_UPDATE_RIGHT() ? imageManager.getIcon_modified_right() : imageManager.getIcon_modified_right_OFF()
        );
        imgAddItem_right.setImage(
                taskFilters.isActive_ADD_RIGHT() ? imageManager.getIcon_addItem_right() : imageManager.getIcon_addItem_right_OFF()
        );
    }



    private void applyCurrentFiltersToTable() {
        // kdyby nebylo co filtrovate
        if (masterRoot == null || editableManualTask == null) return;

        TreeItem<ComparedItemOperItemRowModel> filtredRoot =
                TreeTableFilterService.createFiltredTree(masterRoot, editableManualTask.getGuiOperationFilters());
        filesTreeTable.setRoot(filtredRoot);
    }

    // nastavý listenery pro textová pole a pokud je nová hodnota jiná než v originalu tak zaznamená změnu
    private void setupDirtyTrackers() {
        tfLeftFolderPath.textProperty().addListener((obs, oldVal, newVal) -> {
            if (originalManualTask == null) return;
            if (!newVal.equals(originalManualTask.getLeftPath())) {
                markAsDirty();
            };
        });
        tfRightFolderPath.textProperty().addListener((obs, oldVal, newVal) ->{
            if (originalManualTask == null) return;
            if (!newVal.equals(originalManualTask.getRightPath())) {
                markAsDirty();
            }
        });
    }

    // nastaví proměnou označující změnu v TaskModelu
    private void markAsDirty() {
        if (!isDirtyFlag.get()) {
            isDirtyFlag.set(true);
            System.out.println("nastaveno true");
        }
    }

    private void setIsDirtyFlagListener() {
        isDirtyFlag.addListener((obs, oldVal, newVal) -> {
           String newClass = "modificator_save";
           if (newVal) {
               if (!btnSaveTask.getStyleClass().contains("modificator_save")){
                   btnSaveTask.getStyleClass().add("modificator_save");
               }
           } else {
                btnSaveTask.getStyleClass().remove("modificator_save");
           }
        });
    }

    public void loadView() {
        tfLeftFolderPath.setText(editableManualTask.getLeftPath());
        tfRightFolderPath.setText(editableManualTask.getRightPath());
    }

    private void setImageByBtnID(String id, GuiOperationFilters_TaskData filters) {
        switch (id) {
            // --- LEVÁ STRANA ---
            case "btnAddItem_Left":
                if (filters.isActive_ADD_LEFT()) {
                    filters.setActive_ADD_LEFT(false);
                    imgAddItem_Left.setImage( imageManager.getIcon_addItem_Left_OFF());
                } else {
                    filters.setActive_ADD_LEFT(true);
                    imgAddItem_Left.setImage( imageManager.getIcon_addItem_Left());
                }
                break;

            case "btnModified_left":
                if (filters.isActive_UPDATE_LEFT()) {
                    filters.setActive_UPDATE_LEFT(false);
                    imgModified_left.setImage( imageManager.getIcon_modified_left_OFF());
                } else {
                    filters.setActive_UPDATE_LEFT(true);
                    imgModified_left.setImage( imageManager.getIcon_modified_left());
                }
                break;

            case "btnDelete_left":
                if (filters.isActive_DELETE_LEFT()) {
                    filters.setActive_DELETE_LEFT(false);
                    imgDelete_left.setImage( imageManager.getIcon_delete_left_OFF());
                } else {
                    filters.setActive_DELETE_LEFT(true);
                    imgDelete_left.setImage( imageManager.getIcon_delete_left());
                }
                break;

            // --- STŘED ---
            case "btnBlock":
                if (filters.isActive_IGNORE()) {
                    filters.setActive_IGNORE(false);
                    imgBlock.setImage( imageManager.getIcon_block_OFF());
                } else {
                    filters.setActive_IGNORE(true);
                    imgBlock.setImage( imageManager.getIcon_block());
                }
                break;

            case "btnEqual":
                if (filters.isActive_EQUALS()) {
                    filters.setActive_EQUALS(false);
                    imgEqual.setImage( imageManager.getIcon_equal_OFF());
                } else {
                    filters.setActive_EQUALS(true);
                    imgEqual.setImage( imageManager.getIcon_equal());
                }
                break;

            case "btnUnknown":
                if (filters.isActive_UNKNOWN()) {
                    filters.setActive_UNKNOWN(false);
                    imgUnknown.setImage( imageManager.getIcon_unknown_OFF());
                } else {
                    filters.setActive_UNKNOWN(true);
                    imgUnknown.setImage( imageManager.getIcon_unknown());
                }
                break;

            // --- PRAVÁ STRANA ---
            case "btnDelete_right":
                if (filters.isActive_DELETE_RIGHT()) {
                    filters.setActive_DELETE_RIGHT(false);
                    imgDelete_right.setImage( imageManager.getIcon_delete_right_OFF());
                } else {
                    filters.setActive_DELETE_RIGHT(true);
                    imgDelete_right.setImage( imageManager.getIcon_delete_right());
                }
                break;

            case "btnModified_right":
                if (filters.isActive_UPDATE_RIGHT()) {
                    filters.setActive_UPDATE_RIGHT(false);
                    imgModified_right.setImage( imageManager.getIcon_modified_right_OFF());
                } else {
                    filters.setActive_UPDATE_RIGHT(true);
                    imgModified_right.setImage( imageManager.getIcon_modified_right());
                }
                break;

            case "btnAddItem_right":
                if (filters.isActive_ADD_RIGHT()) {
                    filters.setActive_ADD_RIGHT(false);
                    imgAddItem_right.setImage( imageManager.getIcon_addItem_right_OFF());
                } else {
                    filters.setActive_ADD_RIGHT(true);
                    imgAddItem_right.setImage( imageManager.getIcon_addItem_right());
                }
                break;
        }
    }

    public void setTaskModel(TaskModel tak) {
        this.originalManualTask = (ManualSyncTask) tak;
        this.editableManualTask  = new ManualSyncTask((ManualSyncTask) tak);
        // nastaví správný obrázek k rychlím filtrům
        setFastFiltersImg();
    };

    public void setTaskRepository(TaskRepository taskRepo) {
        this.taskRepository = taskRepo;
    }

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }
}


