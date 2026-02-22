package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AppFrameController {

    @FXML private AnchorPane contentArea;
    @FXML private Label lblTaskName;

    @FXML
    public void showTasksOverview() {
        loadView("TasksOverviewView.fxml", "Zálohový Anděl - seznam úloh");
    }

    @FXML
    public void showCompareOverview() {
        loadView("compareOverviewView.fxml", "Zálohový Anděl - manuální záloha");
    }

    private void loadView(String fxml, String title) {
        try {
            String path = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/" + fxml;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));

            Node node = loader.load();

            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            contentArea.getChildren().setAll(node);

            lblTaskName.setText(title);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Nepodařilo se načíst pohled: " + fxml);
        }
    }
}
