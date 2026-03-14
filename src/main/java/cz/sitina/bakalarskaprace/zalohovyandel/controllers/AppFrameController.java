package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AppFrameController {

    @FXML private AnchorPane contentArea;
    @FXML private Label lblTaskName;

    // definice objektů
    private TaskRepository taskRepository;
    private TaskController curentController;


    @FXML
    public void showTasksOverview() {
        if (checkAndSaveUnsavedChanges()) {
            loadView("TasksOverviewView.fxml", null);
        }
    }

    @FXML
    public void showCompareOverview(TaskModel taskModel) {
        if (checkAndSaveUnsavedChanges()) {
            loadView("compareOverviewView.fxml", taskModel);
        }
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
            } else if (subController instanceof ManualSyncController coc) {
                if (taskModel != null) {
                    ManualSyncTask mst = (ManualSyncTask) taskModel;
                    coc.setTaskModel(mst);
                    coc.setAppFrameController(this);
                    coc.loadView();

                } else  {
                    ErrorUtility.getInstance("Kritická chyba",
                            "Neznámá úloha",
                            "Nepodařilo se najít vybranou Úlohu!"
                            );
                }
            }

            // uložíme spuštěný controller
            if (subController instanceof TaskController) {
                this.curentController = (TaskController) subController;
            } else {
                this.curentController = null;
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
            System.err.println("Nepodařilo se načíst pohled: " + fxml);
            ErrorUtility.showError_WindowLoading(e);
        }

    }

    // kontroluje změny v task v rámci kontrolerů ptá se na uložení změn uživatele
    private boolean checkAndSaveUnsavedChanges() {
        if (curentController != null && curentController.isUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Neuložené změny");
            alert.setHeaderText("Máte neuložené rozpracované změny.");
            alert.setContentText("Neuložené změny budou nevratně ztraceny, chcete je před odchodem uložit?");

            ButtonType btnSave = new ButtonType("Uložit");
            ButtonType btnDiscard = new ButtonType("Zahodit");
            ButtonType btnCancel = new ButtonType("Zrušit", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(btnSave, btnDiscard, btnCancel);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent()) {
                if (result.get() == btnSave) {
                    curentController.saveChanges(); // Zavolá logiku uvnitř ManualSyncControlleru
                    return true; // Uloženo, můžeme odejít
                } else if (result.get() == btnDiscard) {
                    return true; // Neuloženo, ale uživatel chce i tak odejít
                } else {
                    return false; // Uživatel klikl na Zrušit, ZŮSTÁVÁME v okně
                }
            }
            return false;
        }
        return true;
    }

    // zachycení ukončení aplikace křížkem
    public void setupCloseInterceptor(Stage primaryStage) {
        primaryStage.setOnCloseRequest(event -> {
            if (!checkAndSaveUnsavedChanges()) {
                event.consume();
            }
        });
    }

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
}
