package cz.sitina.bakalarskaprace.zalohovyandel.services;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class WindowManager {
    static String windowPath = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/windows/";
    static String componentPath = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/components/";


    /// Rozděleno na dva kroky, loader vytvoří instanci controleru,
    //1 KROK: jen načte controler a data z FXML souboru  (umožňuje předat data do controleru)
    public static FXMLLoader loadFXML(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(windowPath + fxmlFile));
        loader.load();
        return loader;
    }

    public static FXMLLoader loadFXML_component_noLoad(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(WindowManager.class.getResource(componentPath + fxmlFile));
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

    // 2 KROK: načte připravený loader jako modulární okno podle velikosti z FXML
    public static void openNewWindowAutoSized(FXMLLoader loader, String title) {

        Parent root = loader.getRoot();

        Stage newStage = new Stage();
        newStage.setTitle(title);
        Scene scene = new Scene(root);
        newStage.setScene(scene);

        // Přikáže oknu, aby se přesně přizpůsobilo vypočítané velikosti scény
        newStage.sizeToScene();
        newStage.setResizable(false);

        newStage.getIcons().add(new Image(Objects.requireNonNull(WindowManager.class.getResourceAsStream(
                "/cz/sitina/bakalarskaprace/zalohovyandel/images/ZalohovyAndelLogo70x70.png"))));

        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.showAndWait();
    }

    //2 KROK: načte připravený loader jako modulární okno ve kterém mohou běžet různé načítání atd...
    public static Stage openRunningWindow(FXMLLoader loader, String title, int width, int height) {

        Parent root = loader.getRoot();

        Stage newStage = new Stage();
        newStage.setTitle(title);
        Scene scene = new Scene(root, width, height);
        newStage.setScene(scene);
        newStage.setResizable(false);

        var iconStream = WindowManager.class.getResourceAsStream(
                "/cz/sitina/bakalarskaprace/zalohovyandel/images/ZalohovyAndelLogo70x70.png");
        if (iconStream != null) {
            newStage.getIcons().add(new Image(iconStream));
        }

        // nastavuje toto okno jako dialogové, zamezuje zobrazení jiných oken a klikání mimo
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.show();

        return newStage;
    }

    public static void findByActionAndClose(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public static String getWindowPath() {
        return windowPath;
    }

    public static String getComponentPath() {
        return componentPath;
    }
}
