package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Objects;


public class RecapitulationBeforeCopyFilesController {
    private ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData;

    private TaskModel task;
    private boolean isStrategyModified;
    private CompareStrategy strategy;
    private List<ComparedItemOperItemRowModel> list;


    @FXML
    private Label lbLeftAddCount;
    @FXML
    private Label lbLeftUpdateCount;
    @FXML
    private Label lbLeftDeleteCount;
    @FXML
    private Label lbRightDeleteCount;
    @FXML
    private Label lbRightUpdateCount;
    @FXML
    private Label lbRightAddCount;

    @FXML
    private ImageView imgCompareStrategy;
    @FXML
    private Label modifiedIndicator;

    private final String pathToImages = "/cz/sitina/bakalarskaprace/zalohovyandel/images/visualObjects/";
    private final Image biDirectionalSync =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "biDirectionalSync.png")));
    private final Image oneWayLeftToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "oneWayLeftToRight.png")));
    private final Image oneWayRightToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "oneWayRightToLeft.png")));
    private final Image mirroringLeftToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "mirroringLeftToRight.png")));
    private final Image mirroringRightToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(pathToImages + "mirroringRightToLeft.png")));


    public RecapitulationBeforeCopyFilesController() {
    }

    public void setGui() {
        lbLeftAddCount.setText(String.valueOf(comparedAndStatisticsData.getLeftAddCount()));
        lbLeftUpdateCount.setText(String.valueOf(comparedAndStatisticsData.getLeftUpdateCount()));
        lbLeftDeleteCount.setText(String.valueOf(comparedAndStatisticsData.getLeftDeleteCount()));
        lbRightDeleteCount.setText(String.valueOf(comparedAndStatisticsData.getRightDeleteCount()));
        lbRightUpdateCount.setText(String.valueOf(comparedAndStatisticsData.getRightUpdateCount()));
        lbRightAddCount.setText(String.valueOf(comparedAndStatisticsData.getRightAddCount()));

        switch (strategy) {
            case BIDIRECTIONAL_SYNC -> imgCompareStrategy.setImage(biDirectionalSync);
            case BACKUP_TO_RIGHT -> imgCompareStrategy.setImage(oneWayLeftToRight);
            case BACKUP_TO_LEFT -> imgCompareStrategy.setImage(oneWayRightToLeft);
            case MIRRORING_to_RIGHT -> imgCompareStrategy.setImage(mirroringLeftToRight);
            case MIRRORING_TO_lEFT -> imgCompareStrategy.setImage(mirroringRightToLeft);
        }

        // indikátor upravy se naastaví podle
        modifiedIndicator.setDisable(isStrategyModified);

    }

    @FXML
    public void startCopyFiles(ActionEvent event) {
        try {
            // Získej stage přes libovolný prvek
            Stage currentStage = (Stage) lbLeftAddCount.getScene().getWindow();
            currentStage.close();

            //načítáme okno
            FXMLLoader loader = WindowManager.loadFXML("copyProcessWindow.fxml");
            CopyProcessController copyProcessController = loader.getController();
            copyProcessController.startProcess(task, list);

            String title = (task instanceof ManualSyncTask) ? "Synchronizace souborů" : "Záloha souborů";
            Stage copyProcessStage = WindowManager.openRunningWindow(loader, title, 750, 400);

            copyProcessStage.setOnCloseRequest(e -> copyProcessController.cancelAction());



        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading(e);
        }
    }

    public void setStrategy(CompareStrategy strategy) {
        this.strategy = strategy;
    }

    public void setComparedAndStatisticsData(ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData) {
        this.comparedAndStatisticsData = comparedAndStatisticsData;
    }

    public void setTask(TaskModel task) {
        this.task = task;
    }

    public void setStrategyModified(boolean strategyModified) {
        isStrategyModified = strategyModified;
    }

    public void setList(List<ComparedItemOperItemRowModel> list) {
        this.list = list;
    }
}

