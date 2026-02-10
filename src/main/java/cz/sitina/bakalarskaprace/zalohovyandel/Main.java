package cz.sitina.bakalarskaprace.zalohovyandel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        String resourcesPath = "/cz/sitina/bakalarskaprace/zalohovyandel/";
        FXMLLoader fxmlLoader = new FXMLLoader(getClass()
                .getResource( resourcesPath+"fxml/taskOverviewView.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource(resourcesPath+"css/taskOverviewStyl.css")
                ).toExternalForm());

        stage.setTitle("Zálohový Anděl");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
