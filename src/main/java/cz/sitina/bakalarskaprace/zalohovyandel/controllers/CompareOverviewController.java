package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.ManualCompareSettingsController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.io.File;
import java.io.IOException;
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
    private final CompareOverviewService compareOverviewService = new CompareOverviewService();
    private final Utility utility = new Utility();
    private AppFrameController appFrameController;
    private final ManualCompareSettingsController compareSetting = new ManualCompareSettingsController();

    private final TreeItem<ComparedItemOperItemRowModel> root = new TreeItem<>();
    private TreeItem<ComparedItemOperItemRowModel> masterRoot;

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
            FXMLLoader loader = WindowManager.loadFXML("manualCompareSetings.fxml");
            ManualCompareSettingsController controller = loader.getController();
            controller.setTask(manualTask);

            WindowManager.openNewWindow( loader,
                    "Nastavení porovnání souborů",
                    710,
                    500);

        } catch (IOException e) {
            ErrorUtility.getInstance("Kritická chyba při načítání okna",
                    "Nepodařilo se načíst obsah okna.",
                    e.getMessage());
        }
    }

    @FXML
    public void startComparingFiles(ActionEvent event) {
        compareAndLoadFiles(
                root,
                tfLeftFolderPath.getText(),
                tfRightFolderPath.getText(),
                manualTask
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
                                     ManualSyncTask manualTask) {
        root.getChildren().clear(); //čistí TreeTableView

        // získáme potřebná data
         statisticsAndComparedData = comparisonService.compareFolders(
                fileScannerService.scanFolder(leftFolder),
                fileScannerService.scanFolder(rightFolder),
                 manualTask
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

        TreeItem<ComparedItemOperItemRowModel> filtredRoot= new TreeItem<>(null);

        //  vložíme esitující objekty a metoda nám z nich do root nastaví řádky
        treeTableBuilder.setTreeTable(root,
                resultComparedList);
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


