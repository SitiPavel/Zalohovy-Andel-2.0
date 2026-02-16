package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.TreeTableRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.shape.Path;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;

public class CompareOverviewController {

    /** Nastavení sloupců v TreeTableView  a samotná tabulka*/
    @FXML private TreeTableView<TreeTableRowModel> filesTreeTable;
    @FXML private TreeTableColumn<TreeTableRowModel, String> leftColName;
    @FXML private TreeTableColumn<TreeTableRowModel, Long> leftColSize;
    @FXML private TreeTableColumn<TreeTableRowModel, String> leftColLastModifi;
    @FXML private TreeTableColumn<TreeTableRowModel, CompareOperations> operationCol;
    @FXML private TreeTableColumn<TreeTableRowModel, String> rightColName;
    @FXML private TreeTableColumn<TreeTableRowModel, Long> rightColSize;
    @FXML private TreeTableColumn<TreeTableRowModel, String> rightColLastModifi;

    /** daůší definované objekty*/
    /// ✪======V======V======V=======V========
    private final FileScannerService fileScannerService = new FileScannerService();
    private HashMap<String, FileItemModel> leftHashFileMap;
    /// =======^=======^=======^=======^=======


    public CompareOverviewController(){}

    @FXML
    public void initialize() {


        /** ✪ */leftHashFileMap = fileScannerService.scanFolder("D:\\testBakalarky\\Test01");
        /** ✪ */System.out.println(leftHashFileMap);



        /*Definování obsahu sloupců ve filesTreeTable*/
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


        /*nastavení kořene celé struktury*/
        TreeItem<TreeTableRowModel> root = new TreeItem<>(new TreeTableRowModel(
                item1,
                CompareOperations.ADD_LEFT,
                item2));

        root.setExpanded(true);


        TreeItem<FileItemModel> treeBrancheData = new TreeItem<>(new FileItemModel(
                "HlDokumenty", Paths.get("C:/user/pavel/dokumenty/HlDokumenty")));


        treeBrancheData.getChildren().add(new TreeItem<>());
        treeBrancheData.getChildren().add(new TreeItem<>());


        //root.getChildren().add(treeBrancheData);

        filesTreeTable.setRoot(root);
        filesTreeTable.setShowRoot(false);

    }
}
