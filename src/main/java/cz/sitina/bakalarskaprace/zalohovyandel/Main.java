package cz.sitina.bakalarskaprace.zalohovyandel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        var resource = getClass().getResource("/cz/sitina/bakalarskaprace/zalohovyandel/fxml/taskOverviewView.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);

        Scene scene = new Scene(fxmlLoader.load(), 800, 580);

        stage.setTitle("Zálohový Anděl");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
