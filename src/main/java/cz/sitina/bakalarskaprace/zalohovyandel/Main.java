package cz.sitina.bakalarskaprace.zalohovyandel;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.AppFrameController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        TaskRepository taskRepository = new TaskRepository();

        taskRepository.add(new ManualSyncTask("zaloha manual 1",
                "D:\\testBakalarky\\Test01",
                "D:\\testBakalarky\\Test02",
                "")
        );
        taskRepository.add(new ManualSyncTask("Obrovské množství souborů",
                "D:\\media (filmi, fortky, písničky)",
                "D:\\testBakalarky\\Test02",
                "popis úlohy ")
        );


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
        launch();
    }
}
