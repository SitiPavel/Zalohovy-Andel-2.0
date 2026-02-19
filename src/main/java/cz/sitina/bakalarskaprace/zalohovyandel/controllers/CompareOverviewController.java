package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.CompareOverviewTreeTableBuilderService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.Node;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.util.List;

public class CompareOverviewController {

    /// Nastavení sloupců v TreeTableView  a samotná tabulka
    @FXML private TreeTableView<ComparedItemOperItemRowModel> filesTreeTable;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColLastModifi;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColLastModifi;

    /** daůší definované objekty*/

    private final FileScannerService fileScannerService = new FileScannerService();
    private List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
    private CompareOverviewTreeTableBuilderService treeTableBuilder = new CompareOverviewTreeTableBuilderService();

    private final ComparisonService comparisonService = new ComparisonService();
    private final BiDirectionalStrategy biDirectionalStrategy = new BiDirectionalStrategy();
    private final MirroringStrategy mirroringStrategy = new MirroringStrategy();
    private final OneWayStrategy oneWayStrategy = new OneWayStrategy();
    private final CompareOverviewService compareOverviewService = new CompareOverviewService();
    private final TreeItem<ComparedItemOperItemRowModel> root = new TreeItem<>();


    /// definocvané GUI objekty
    @FXML private Button btnLeftFileChoiser;
    @FXML private TextField tfLeftFolderPath;
    @FXML private Button btnRightFileChoiser;
    @FXML private TextField tfRightFolderPath;
    @FXML private Button btnCompareFolders;



    public CompareOverviewController(){}

    @FXML
    public void initialize() {
        ///Definování obsahu sloupců ve filesTreeTable
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

        /// Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        rightColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModifi.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");
        rightColSize.getStyleClass().add("center-column");
        rightColLastModifi.getStyleClass().add("center-column");


        ///nastavení kořene celé struktury
        root.setExpanded(true);



        filesTreeTable.setRoot(root);
        filesTreeTable.setShowRoot(false);

    }

    /**
     Metody reagující na uživatele
     */

    @FXML
    public void choseLeftFolder(ActionEvent event) {
        File directory = compareOverviewService.findDirectory(event);
        if (directory != null) {
            tfLeftFolderPath.setText(directory.getAbsolutePath());
        }

    }
    @FXML
    public void startComparingFiles(ActionEvent event) {
        compareAndLoadFiles(
                root,
                tfLeftFolderPath.getText(),
                tfRightFolderPath.getText(),
                mirroringStrategy
        );

    }

    @FXML
    public void choseRightFolder(ActionEvent event) {
        File directory = compareOverviewService.findDirectory(event);
        if (directory != null) {
            tfRightFolderPath.setText(directory.getAbsolutePath());
        }
    }


    /**
     obecné nápomocné metody
     */

    private void compareAndLoadFiles(TreeItem<ComparedItemOperItemRowModel> root,
                                     String leftFolder,
                                     String rightFolder,
                                     ComparationStrategy strategy) {
        root.getChildren().clear();
        resultComparedList = comparisonService.compareFolders(
                fileScannerService.scanFolder(leftFolder),
                fileScannerService.scanFolder(rightFolder),
                strategy
        );

        /// načtení do TreeTableView
        treeTableBuilder.loadComparedFilesToGUI(root, resultComparedList);
        /// Obarvení řádků podle operace
        treeTableBuilder.rowFormatingByOperation(filesTreeTable);
        ///  vloží ikony do sloupce operace
        treeTableBuilder.setOperationColumnIcon(operationCol);
    }
}
