package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TreeTableBuilderService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.Utility;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.util.ArrayList;
import javafx.event.ActionEvent;


import java.util.List;

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

    // daůší definované objekty

    private final FileScannerService fileScannerService = new FileScannerService();
    private List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
    private TreeTableBuilderService treeTableBuilder = new TreeTableBuilderService();
    private ManualSyncTask manualTask;

    private final ComparisonService comparisonService = new ComparisonService();
    private final BiDirectionalStrategy biDirectionalStrategy = new BiDirectionalStrategy();
    private final MirroringStrategy mirroringStrategy = new MirroringStrategy();
    private final OneWayStrategy oneWayStrategy = new OneWayStrategy();
    private final CompareOverviewService compareOverviewService = new CompareOverviewService();
    private final TreeItem<ComparedItemOperItemRowModel> root = new TreeItem<>();
    private final Utility utility = new Utility();
    private AppFrameController appFrameController;

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
    @FXML private Label lbDataTransportTotalSize;

    // ukazatelé statistik disků
    @FXML private Label lbLeftUsedSpace;
    @FXML private Label lbLeftTotalSpace;
    @FXML private Label lbLeftFreeSpace;
    @FXML private ProgressBar pbLeftUsedSpace;

    @FXML private Label lbRightUsedSpace;
    @FXML private Label lbRightTotalSpace;
    @FXML private Label lbRightFreeSpace;
    @FXML private ProgressBar pbRightUsedSpace;




    public CompareOverviewController(){}

    @FXML
    public void initialize() {
        //Definování obsahu sloupců ve filesTreeTable
        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().fileNameProperty());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedSize());
        leftColLastModifi.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedLastModified());

        operationCol.setCellValueFactory(cellData ->
                cellData.getValue().getValue().operationProperty());

        rightColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().fileNameProperty());
        rightColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedSize());
        rightColLastModifi.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedLastModified());

        // Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        rightColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModifi.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");
        rightColSize.getStyleClass().add("center-column");
        rightColLastModifi.getStyleClass().add("center-column");


        //nastavení kořene celé struktury
        root.setExpanded(true);

        filesTreeTable.setRoot(root);
        filesTreeTable.setShowRoot(false);

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
    public void startSetingsOfComparing(ActionEvent event){
    }
    @FXML
    public void startComparingFiles(ActionEvent event) {
        compareAndLoadFiles(
                root,
                tfLeftFolderPath.getText(),
                tfRightFolderPath.getText(),
                biDirectionalStrategy
        );

    }

    public void showTaskOverviewGUI(ActionEvent event) {
        appFrameController.showTasksOverview();
    }

    @FXML
    public void choseRightFolder(ActionEvent event) {
        File directory = compareOverviewService.findDirectory(event);
        if (directory != null) {
            tfRightFolderPath.setText(directory.getAbsolutePath());
        }
    }


    //==================
    // obecné nápomocné metody


    // stará se o poslání objektů k porovnání shod a načítá veškerá data
    private void compareAndLoadFiles(TreeItem<ComparedItemOperItemRowModel> root,
                                     String leftFolder,
                                     String rightFolder,
                                     ComparationStrategy strategy) {
        root.getChildren().clear(); //čistí TreeTableView

        // získáme potřebná data
         statisticsAndComparedData = comparisonService.compareFolders(
                fileScannerService.scanFolder(leftFolder),
                fileScannerService.scanFolder(rightFolder),
                strategy
        );

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
        lbDataTransportTotalSize.setText(
                utility.getFormatedSize(statisticsAndComparedData.getDataTransportSize())
        );

        resultComparedList = statisticsAndComparedData.getComparedFilesList();

        //  vložíme esitující objekty a metoda nám z nich do root nastaví řádky
        treeTableBuilder.setTreeTable(root,
                resultComparedList,
                filesTreeTable,
                operationCol);
    }

    public void loadView() {
        tfLeftFolderPath.setText(manualTask.getLeftPath());
        tfRightFolderPath.setText(manualTask.getRightPath());
    }

    public void setTaskModel(ManualSyncTask tak) {
        this.manualTask = tak;
    };

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }
}


