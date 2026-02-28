package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WindowManager {
    static String path = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/windows/";


    /// Rozděleno na dva kroky, loader vytvoří instanci controleru,
    //1 KROK: jen načte controler a data z FXML souboru  (umožňuje předat data do controleru)
    public static FXMLLoader loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(path + fxmlFile));
        loader.load();
        return loader;
    }
    //2 KROK: načte připravený loader jako modulární okno
    public static void openNewWindow(FXMLLoader loader, String title, int width, int height) {


        Parent root = loader.getRoot();

        Stage newStage = new Stage();
        newStage.setTitle(title);
        Scene scene = new Scene(root, width, height);
        newStage.setScene(scene);
        newStage.setResizable(false);

        newStage.getIcons().add(new Image(Objects.requireNonNull(WindowManager.class.getResourceAsStream(
                       "/cz/sitina/bakalarskaprace/zalohovyandel/images/ZalohovyAndelLogo70x70.png"))));

        // nastavuje toto okno jako dialogové, zamezuje zobrazení jiných oken a klikání mimo
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.showAndWait(); // aplikace v kódu čeká na reakci uživatele



    }
}
