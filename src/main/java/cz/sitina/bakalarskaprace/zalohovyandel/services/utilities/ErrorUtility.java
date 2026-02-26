package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import javafx.scene.control.Alert;

public class ErrorUtility {

    /// základní chybové okno
    public static void getInstance(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
