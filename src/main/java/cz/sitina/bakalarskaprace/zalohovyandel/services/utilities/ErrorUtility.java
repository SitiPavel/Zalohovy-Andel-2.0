package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import javafx.scene.control.Alert;

import java.io.IOException;

public class ErrorUtility {

    /// základní chybové okno
    public static void getInstance(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void showError_WindowLoading(IOException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kritická chyba při načítání okna");
        alert.setHeaderText("Nepodařilo se načíst obsah okna.");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public static void showError_pointlessCompareOperation(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba operace");
        alert.setHeaderText("Vznikla nesmyslná operace!");
        alert.setContentText(message);
        alert.showAndWait();
    }


}
