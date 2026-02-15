package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.CompareItemModel;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;


import java.time.LocalDate;

public class CompareOverviewController {

    @FXML private TreeTableView<CompareItemModel> leftTreeTable;
    @FXML private TreeTableColumn<CompareItemModel, String> leftColName;
    @FXML private TreeTableColumn<CompareItemModel, String> leftColDate;
    @FXML private TreeTableColumn<CompareItemModel, Integer> leftColSize;

    public CompareOverviewController(){}

    @FXML
    public void initialize() {

        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().fileNameProperty());
        leftColDate.setCellValueFactory(cellData ->
                cellData.getValue().getValue().fileNameProperty());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().fileSizeProperty().asObject());


        TreeItem<CompareItemModel> root = new TreeItem<>(new CompareItemModel("Root","", ""));
        root.setExpanded(true);

        TreeItem<CompareItemModel> treeBrancheData = new TreeItem<>(new CompareItemModel("HlDokumenty", "", ""));

        treeBrancheData.getChildren().add(new TreeItem<>(new CompareItemModel(
                "bakalarka",
                "C:/user/pavel/dokumenty",
                "2023-12-14",
                1522153)));
        treeBrancheData.getChildren().add(new TreeItem<>(new CompareItemModel(
                "Fotka testu",
                "C:/user/pavel/dokumenty",
                "2024-08-30",
                5265)));

        root.getChildren().add(treeBrancheData);

        leftTreeTable.setRoot(root);
        leftTreeTable.setShowRoot(false);

    }
}
