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


    public static void showError_WindowLoading() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kritická chyba při načítání okna");
        alert.setHeaderText("Nepodařilo se načíst obsah okna.");
        alert.setContentText("Chybí potřebné soubory uživatelského rozhraní.");
        alert.showAndWait();
    }

    public static void showError_form(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba formuláře");
        alert.setHeaderText("Některé z polí nejsou vyplněny podle očekávání!");
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static void showError_pointlessCompareOperation(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Chyba operace");
        alert.setHeaderText("Vznikla nesmyslná operace!");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError_databaseError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kritická chyba");
        alert.setHeaderText("Nastala chyba při práci s databází!");
        alert.setContentText(message);
        alert.showAndWait();
    }


}
