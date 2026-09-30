package cz.sitina.bakalarskaprace.zalohovyandel;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.AppFrameController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
//s
public class Main extends Application {

    JsonDataManagerService jsonManager = JsonDataManagerService.getInstance();

    @Override
    public void start(Stage stage) throws IOException {

        TaskRepository taskRepository = new TaskRepository();



        /// načítá data při zapnutí aplikace
       for (TaskModel task: jsonManager.loadAllTasks()){

            taskRepository.add(task);
        }

        String resourcesPath = "/cz/sitina/bakalarskaprace/zalohovyandel/";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcesPath +"fxml/AppFrameView.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 700);

        AppFrameController appFrameController = loader.getController();
        appFrameController.setTaskRepository(taskRepository);
        appFrameController.showTasksOverview();


        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(
                        resourcesPath + "images/ZalohovyAndelLogo70x70.png"))));

        stage.setTitle("Zálohový Anděl");
        stage.setScene(scene);
        stage.show();

    }



    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("javafx.fxml").setLevel(java.util.logging.Level.SEVERE);

        launch();
    }
}
