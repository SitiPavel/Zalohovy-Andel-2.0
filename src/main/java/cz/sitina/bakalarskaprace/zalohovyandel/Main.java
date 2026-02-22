package cz.sitina.bakalarskaprace.zalohovyandel;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        ManualSyncTask task1 = new ManualSyncTask("zaloha manual 1",
                "D:\\testBakalarky\\Test01",
                "D:\\testBakalarky\\Test02", ""
                );

        String resourcesPath = "/cz/sitina/bakalarskaprace/zalohovyandel/";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource( resourcesPath+"fxml/compareOverviewView.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource(resourcesPath+"css/compareOverviewStyl.css")
                ).toExternalForm());


        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(
                        "/cz/sitina/bakalarskaprace/zalohovyandel/images/ZalohovyAndelLogo70x70.png"
                ))));

        stage.setTitle("Zálohový Anděl");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
