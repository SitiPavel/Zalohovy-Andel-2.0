package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;



public class CompareProgresController {

    @FXML private Label lbHeader;
    @FXML private Label lbComparedFiles;
    @FXML private Label lbAllFiles;
    @FXML private ProgressBar progressBar;
    @FXML private javafx.scene.control.Button btnCancelComparing;

    private Task<?> runningTask; // pro zrušení celého porovnání


    public void startComparisonWithProgressWindow(Task<?> task)  {
        this.runningTask = task;

        progressBar.progressProperty().bind(task.progressProperty());
        lbComparedFiles.textProperty().bind(task.messageProperty());
        lbAllFiles.textProperty().bind(task.titleProperty());
    }

    // Spuštění neurčitého načítání bez číselných hodnot s plynulou animací progressBaru
    public void startIndeterminateTask(Task<?> task, String headerText) {
        this.runningTask = task;
        lbHeader.setText(headerText);
        lbComparedFiles.setText("");
        lbAllFiles.setText("");
        progressBar.setProgress(-1.0);
    }

    public void setLbHeader(String header) {
        lbHeader.setText(header);
    }

    @FXML
    public void cancelComparing() {
        if (runningTask != null && runningTask.isRunning()) {
            runningTask.cancel();
        }
        closeWindow();
    }



    public void closeWindow() {
        Stage stage = (Stage) btnCancelComparing.getScene().getWindow();
        stage.close();
    }
}
