package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AppFrameController {

    @FXML private AnchorPane contentArea;
    @FXML private Label lblTaskName;

    // definice objektů
    private TaskRepository taskRepository;


    @FXML
    public void showTasksOverview() {
        loadView("TasksOverviewView.fxml", null);

    }

    @FXML
    public void showCompareOverview(TaskModel taskModel) {
        loadView("compareOverviewView.fxml", taskModel);
    }


    // načítání jednotlivích obrazovek
    private void loadView(String fxml, TaskModel taskModel) {
        try {
            String path = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/" + fxml;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));

            Node node = loader.load();

            Object subController = loader.getController();

            if (subController instanceof TaskOverviewController toc) {
                toc.setTaskRepository(taskRepository);
                toc.setAppFrameController(this);
            } else if (subController instanceof CompareOverviewController coc) {
                if (taskModel != null) {
                    ManualSyncTask mst = (ManualSyncTask) taskModel;
                    coc.setTaskModel(mst);
                    coc.setAppFrameController(this);
                    coc.loadView();

                } else  {
                    System.out.println("!!! taskModel nenalezen"); /*ERROR ✪ */
                }
            }

            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            contentArea.getChildren().setAll(node);

            if (taskModel != null) {
                lblTaskName.setText(taskModel.getName());
            } else {
                lblTaskName.setText("");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Nepodařilo se načíst pohled: " + fxml);/*ERROR ✪ */
        }

    }

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
