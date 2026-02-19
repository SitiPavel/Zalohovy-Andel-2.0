package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.CompareOverviewTreeTableBuilderService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.BiDirectionalStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.MirroringStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.OneWayStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
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
    /// ✪======V======V======V=======V========
    private final FileScannerService fileScannerService = new FileScannerService();
    private List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
    private CompareOverviewTreeTableBuilderService treeTableBuilder = new CompareOverviewTreeTableBuilderService();

    private final ComparisonService comparisonService = new ComparisonService();
    private final BiDirectionalStrategy biDirectionalStrategy = new BiDirectionalStrategy();
    private final MirroringStrategy mirroringStrategy = new MirroringStrategy();
    private final OneWayStrategy oneWayStrategy = new OneWayStrategy();

    /// =======^=======^=======^=======^=======

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
                cellData.getValue().getValue().getLeftItem().getFormatedSize());
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
        TreeItem<ComparedItemOperItemRowModel> root = new TreeItem<>();
        root.setExpanded(true);

        /// ✪======V======V======V=======V========
        resultComparedList = comparisonService.compareFolders(
                fileScannerService.scanFolder("D:\\testBakalarky\\Test01"),
                fileScannerService.scanFolder("D:\\testBakalarky\\Test03"),
                mirroringStrategy
        );
        /// =======^=======^=======^=======^=======

        /// načtení do TreeTableView
        treeTableBuilder.loadComparedFilesToGUI(root, resultComparedList);
        /// Obarvení řádků podle operace
        treeTableBuilder.rowFormatingByOperation(filesTreeTable);
        ///  vloží ikony do sloupce operace
        treeTableBuilder.setOperationColumnIcon(operationCol);


        filesTreeTable.setRoot(root);
        filesTreeTable.setShowRoot(false);

    }


}
