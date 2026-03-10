package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.CompareProgresController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.ManualCompareSettingsController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.RecapitulationBeforeCopyFilesController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.application.Platform;
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
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager.loadFXML;

public class CompareOverviewController {

    // Nastavení sloupců v TreeTableView  a samotná tabulka
    @FXML private TreeTableView<ComparedItemOperItemRowModel> filesTreeTable;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColLastModifi;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColLastModifi;

    // daůší definované třídy atd..

    private final FileScannerService fileScannerService = new FileScannerService();
    private TreeTableBuilderService treeTableBuilder = new TreeTableBuilderService();

    private final ComparisonService comparisonService = new ComparisonService();
    private final CompareOverviewService compareOverviewService = new CompareOverviewService();
    private final Utility utility = new Utility();
    private AppFrameController appFrameController;



    // definované důležité proměnné
    private List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
    private final TreeItem<ComparedItemOperItemRowModel> root = new TreeItem<>();
    private TreeItem<ComparedItemOperItemRowModel> masterRoot;
    private ManualSyncTask manualTask;

    private final ManualCompareSettingsController compareSetting = new ManualCompareSettingsController();
    private CompareStrategy strategyForRecapitulate;
    private boolean isLeftOperationSizeSmalerThenLeftFolder;
    private boolean isRightOperationSizeSmalerThenRightFolder;


    // definované datové sady
    private ComparedDataAndStatisticsForCompereOverview statisticsAndComparedData;

    ///======================================
    /// definocvané GUI objekty
    @FXML private Button btnLeftFileChoiser;
    @FXML private TextField tfLeftFolderPath;
    @FXML private Button btnRightFileChoiser;
    @FXML private TextField tfRightFolderPath;
    @FXML private Button btnCompareFolders;
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


    private final String pathToImages = "/cz/sitina/bakalarskaprace/zalohovyandel/images/icons/";
    private final Image icon_addItem_Left =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "addItem_left.png")));
    private final Image icon_modified_left  =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "modified_left.png")));
    private final Image icon_delete_left    =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "delete_left.png")));
    private final Image icon_block          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "block.png")));
    private final Image icon_equal          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "equal.png")));
    private final Image icon_unknown   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "unknown.png")));
    private final Image icon_delete_right   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "delete_right.png")));
    private final Image icon_modified_right =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "modified_right.png")));
    private final Image icon_addItem_right =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "addItem_right.png")));

    private final Image icon_addItem_Left_OFF =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "addItem_left_OFF.png")));
    private final Image icon_modified_left_OFF  =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "modified_left_OFF.png")));
    private final Image icon_delete_left_OFF    =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "delete_left_OFF.png")));
    private final Image icon_block_OFF          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "block_OFF.png")));
    private final Image icon_equal_OFF          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "equal_OFF.png")));
    private final Image icon_unknown_OFF   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "unknown_OFF.png")));
    private final Image icon_delete_right_OFF   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "delete_right_OFF.png")));
    private final Image icon_modified_right_OFF =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "modified_right_OFF.png")));
    private final Image icon_addItem_right_OFF =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "addItem_right_OFF.png")));

    // koláče
    private final Image pieChartYellow_ToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "pieChar-Yellow_Left.png")));
    private final Image pieChartRed_ToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "pieChar-Red_left.png")));
    private final Image pieChartYellow_ToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "pieChar-Yellow_Right.png")));
    private final Image pieChartRed_ToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "pieChar-Red_Right.png")));


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




    public CompareOverviewController(){}

    @FXML
    public void initialize() {
        //Definování obsahu sloupců ve filesTreeTable
        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedName());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedSize());
        leftColLastModifi.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedLastModified());

        operationCol.setCellValueFactory(cellData ->
                cellData.getValue().getValue().operationProperty());

        rightColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedName());
        rightColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedSize());
        rightColLastModifi.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedLastModified());


        /// NASTAVENÍ SLOUPCŮ
        // Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModifi.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");
        rightColSize.getStyleClass().add("center-column");
        rightColLastModifi.getStyleClass().add("center-column");



        // jiní nastavení
        filesTreeTable.setTreeColumn(leftColName);
        leftColName.setCellFactory(cellData ->
                new TreeTableCell<ComparedItemOperItemRowModel, String>() {
            Tooltip tooltip = new Tooltip();
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
                }
            }
        });

        treeTableBuilder.setColumnIindentLevelAndToolTips(rightColName);


        filesTreeTable.setShowRoot(false);

        // nastavení generování Obarvených řádků podle operace
        treeTableBuilder.rowFormatingByOperation(filesTreeTable);
        //  nastaví generování ikon do sloupce operace
        treeTableBuilder.setOperationColumnIcon(operationCol);



    }

    //===================
    // Metody reagující na uživatele

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
            controller.setTask(manualTask);

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
        strategyForRecapitulate = manualTask.getCompareStrategy();
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
                            manualTask,
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

    public void showTaskOverviewGUI(ActionEvent event) {
        appFrameController.showTasksOverview();
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
                controller.setTask(manualTask);
                controller.setComparedAndStatisticsData(statisticsAndComparedData);
                controller.setStrategyModified(false);
                controller.setList(resultComparedList);
                controller.setStrategy(strategyForRecapitulate);
                controller.setGui();

                WindowManager.openNewWindow(loader, "Rekapitulace", 372, 296);


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

        setImageByBtnID(source.getId(), manualTask.getGuiOperationFilters());

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

        //


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
        if (utility.isSazeSmalerThenFreeSpace(
                                                leftFolder,
                                                statisticsAndComparedData.getDataTransportSize_left())) {
            imgPieChar_Left.setImage(pieChartYellow_ToLeft);
            isLeftOperationSizeSmalerThenLeftFolder =true;
        } else {
            imgPieChar_Left.setImage(pieChartRed_ToLeft);
            isLeftOperationSizeSmalerThenLeftFolder = false;
        }

        if (utility.isSazeSmalerThenFreeSpace(
                                                rightFolder,
                                                statisticsAndComparedData.getDataTransportSize_right())) {
            imgPieChar_Right.setImage(pieChartYellow_ToRight);
            isRightOperationSizeSmalerThenRightFolder =true;
        } else {
            imgPieChar_Right.setImage(pieChartRed_ToRight);
            isRightOperationSizeSmalerThenRightFolder = false;
        }

        // nastavení textů
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


        // strom
        resultComparedList = statisticsAndComparedData.getComparedFilesList();
        masterRoot = new TreeItem<>(null);
        masterRoot.setExpanded(true);


        //  vložíme esitující objekty a metoda nám z nich do root nastaví řádky
        treeTableBuilder.setTreeTable(masterRoot, resultComparedList);

        applyCurrentFiltersToTable();
    }

    // nastaví podle true/false obrázek odpovídajcí vypnutému nebo zapnutému filtru
    private void setFastFiltersImg() {
        GuiOperationFilters_TaskData taskFilters = manualTask.getGuiOperationFilters();
        imgAddItem_Left.setImage(
                taskFilters.isActive_ADD_LEFT() ? icon_addItem_Left : icon_addItem_Left_OFF
        );
        imgModified_left.setImage(
                taskFilters.isActive_UPDATE_LEFT() ? icon_modified_left  : icon_modified_left_OFF
        );
        imgDelete_left.setImage(
                taskFilters.isActive_DELETE_LEFT() ? icon_delete_left : icon_delete_left_OFF
        );
        imgBlock.setImage(
                taskFilters.isActive_IGNORE() ? icon_block : icon_block_OFF
        );
        imgEqual.setImage(
                taskFilters.isActive_EQUALS() ? icon_equal : icon_equal_OFF
        );
        imgUnknown.setImage(
                taskFilters.isActive_UNKNOWN() ? icon_unknown : icon_unknown_OFF
        );
        imgDelete_right.setImage(
                taskFilters.isActive_DELETE_RIGHT() ? icon_delete_right : icon_delete_right_OFF
        );
        imgModified_right.setImage(
                taskFilters.isActive_UPDATE_RIGHT() ? icon_modified_right :icon_modified_right_OFF
        );
        imgAddItem_right.setImage(
                taskFilters.isActive_ADD_RIGHT() ? icon_addItem_right : icon_addItem_right_OFF
        );
    }



    private void applyCurrentFiltersToTable() {
        // kdyby nebylo co filtrovate
        if (masterRoot == null || manualTask == null) return;

        TreeItem<ComparedItemOperItemRowModel> filtredRoot =
                TreeTableFilterService.createFiltredTree(masterRoot, manualTask.getGuiOperationFilters());
        filesTreeTable.setRoot(filtredRoot);
    }

    public void loadView() {
        tfLeftFolderPath.setText(manualTask.getLeftPath());
        tfRightFolderPath.setText(manualTask.getRightPath());
    }

    private void setImageByBtnID(String id, GuiOperationFilters_TaskData filters) {
        switch (id) {
            // --- LEVÁ STRANA ---
            case "btnAddItem_Left":
                if (filters.isActive_ADD_LEFT()) {
                    filters.setActive_ADD_LEFT(false);
                    imgAddItem_Left.setImage(icon_addItem_Left_OFF);
                } else {
                    filters.setActive_ADD_LEFT(true);
                    imgAddItem_Left.setImage(icon_addItem_Left);
                }
                break;

            case "btnModified_left":
                if (filters.isActive_UPDATE_LEFT()) {
                    filters.setActive_UPDATE_LEFT(false);
                    imgModified_left.setImage(icon_modified_left_OFF); // Opraveno: správný ImageView
                } else {
                    filters.setActive_UPDATE_LEFT(true);
                    imgModified_left.setImage(icon_modified_left);     // Opraveno: správný ImageView
                }
                break;

            case "btnDelete_left":
                if (filters.isActive_DELETE_LEFT()) {
                    filters.setActive_DELETE_LEFT(false);
                    imgDelete_left.setImage(icon_delete_left_OFF);
                } else {
                    filters.setActive_DELETE_LEFT(true);
                    imgDelete_left.setImage(icon_delete_left);
                }
                break;

            // --- STŘED ---
            case "btnBlock":
                if (filters.isActive_IGNORE()) {
                    filters.setActive_IGNORE(false);
                    imgBlock.setImage(icon_block_OFF);
                } else {
                    filters.setActive_IGNORE(true);
                    imgBlock.setImage(icon_block);
                }
                break;

            case "btnEqual":
                if (filters.isActive_EQUALS()) {
                    filters.setActive_EQUALS(false);
                    imgEqual.setImage(icon_equal_OFF);
                } else {
                    filters.setActive_EQUALS(true);
                    imgEqual.setImage(icon_equal);
                }
                break;

            case "btnUnknown":
                if (filters.isActive_UNKNOWN()) {
                    filters.setActive_UNKNOWN(false);
                    imgUnknown.setImage(icon_unknown_OFF);
                } else {
                    filters.setActive_UNKNOWN(true);
                    imgUnknown.setImage(icon_unknown);
                }
                break;

            // --- PRAVÁ STRANA ---
            case "btnDelete_right":
                if (filters.isActive_DELETE_RIGHT()) {
                    filters.setActive_DELETE_RIGHT(false);
                    imgDelete_right.setImage(icon_delete_right_OFF);
                } else {
                    filters.setActive_DELETE_RIGHT(true);
                    imgDelete_right.setImage(icon_delete_right);
                }
                break;

            case "btnModified_right":
                if (filters.isActive_UPDATE_RIGHT()) {
                    filters.setActive_UPDATE_RIGHT(false);
                    imgModified_right.setImage(icon_modified_right_OFF);
                } else {
                    filters.setActive_UPDATE_RIGHT(true);
                    imgModified_right.setImage(icon_modified_right);
                }
                break;

            case "btnAddItem_right":
                if (filters.isActive_ADD_RIGHT()) {
                    filters.setActive_ADD_RIGHT(false);
                    imgAddItem_right.setImage(icon_addItem_right_OFF);
                } else {
                    filters.setActive_ADD_RIGHT(true);
                    imgAddItem_right.setImage(icon_addItem_right);
                }
                break;
        }
    }

    public void setTaskModel(ManualSyncTask tak) {
        this.manualTask = tak;
        // nastaví správný obrázek k rychlím filtrům
        setFastFiltersImg();
    };

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }
}


