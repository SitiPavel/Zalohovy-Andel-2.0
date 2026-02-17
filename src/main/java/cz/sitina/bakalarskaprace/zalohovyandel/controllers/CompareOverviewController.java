package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.nio.file.Paths;
import java.util.HashMap;

public class CompareOverviewController {

    /// Nastavení sloupců v TreeTableView  a samotná tabulka
    @FXML private TreeTableView<ComparedItemOperItemRowModel> filesTreeTable;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, Long> leftColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColLastModifi;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, Long> rightColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> rightColLastModifi;

    /** daůší definované objekty*/
    /// ✪======V======V======V=======V========
    private final FileScannerService fileScannerService = new FileScannerService();
    private HashMap<String, FileItemModel> leftHashFileMap;
    /// =======^=======^=======^=======^=======

    @FXML
    private TreeTableColumn<FileItemModel, String> operationColumn;

    public CompareOverviewController(){}

    @FXML
    public void initialize() {

        operationColumn = new TreeTableColumn<>();


        ///Definování obsahu sloupců ve filesTreeTable
        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().fileNameProperty());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().fileSizeProperty().asObject());
        leftColLastModifi.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedDate());


        rightColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().fileNameProperty());
        rightColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().fileSizeProperty().asObject());
        rightColLastModifi.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getRightItem().getFormatedDate());

        /// ✪======V======V======V=======V========
        FileItemModel item1  = new FileItemModel(
                "bakalarka",
                Paths.get("C:/user/pavel/dokumenty"),
                552564885,
                1522153);

        FileItemModel item2 = new FileItemModel(
                "Fotka testu",
                Paths.get("C:/user/pavel/skola"),
                455844455,
                5265);
        /// =======^=======^=======^=======^=======


        ///nastavení kořene celé struktury
        TreeItem<ComparedItemOperItemRowModel> root = new TreeItem<>();

        root.setExpanded(true);

        /*
        TreeItem<FileItemModel> treeBrancheData = new TreeItem<>(new FileItemModel(
                "HlDokumenty", Paths.get("C:/user/pavel/dokumenty/HlDokumenty")));


        treeBrancheData.getChildren().add(new TreeItem<>());
        treeBrancheData.getChildren().add(new TreeItem<>());
        */

        root.getChildren().add(new TreeItem<>(new ComparedItemOperItemRowModel(
                item1,
                CompareOperations.ADD_LEFT,
                item2)));


        filesTreeTable.setRoot(root);
        filesTreeTable.setShowRoot(false);

    }
}
