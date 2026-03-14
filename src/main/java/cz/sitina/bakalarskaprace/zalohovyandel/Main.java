package cz.sitina.bakalarskaprace.zalohovyandel;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.AppFrameController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
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

public class Main extends Application {

    JsonDataManagerService jsonManager = new JsonDataManagerService();

    @Override
    public void start(Stage stage) throws IOException {

        TaskRepository taskRepository = new TaskRepository();

        ///  pouze na první test data
        //createFirstData(taskRepository);

        // načítá data při zapnutí aplikace
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

    private void createFirstData(TaskRepository taskRepository) {

        taskRepository.add(new ManualSyncTask("Test operací pro složky",
                "D:\\testBakalarky\\Test03",
                "D:\\testBakalarky\\Test06",
                "testuje jestli se dobře přidělují operace složkám")
        );


        taskRepository.add(new ManualSyncTask("zaloha manual 1",
                "D:\\testBakalarky\\Test01",
                "D:\\testBakalarky\\Test02",
                "")
        );
        taskRepository.add(new ManualSyncTask("Obrovské množství souborů",
                "D:\\",
                "F:\\",
                "popis úlohy ")
        );

        ManualSyncTask task11 = new ManualSyncTask("testování",
                "D:\\testBakalarky\\Test05",
                "D:\\testBakalarky\\Test04",
                "Slouží k testování přenosu ");
        task11.setCompareStrategy(CompareStrategy.MIRRORING_to_RIGHT);

        taskRepository.add(task11);

        List<TaskModel> cistyList = new ArrayList<>(taskRepository.getTaskObservableList());
        jsonManager.saveAllTasks_rewriteOldSave(cistyList);
    }

    public static void main(String[] args) {
        launch();
    }
}
