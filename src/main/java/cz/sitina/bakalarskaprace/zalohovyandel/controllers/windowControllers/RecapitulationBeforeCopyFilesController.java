package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtiliy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
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


public class RecapitulationBeforeCopyFilesController  {
    private ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData;
    private final IconManagerUtility  iconManager = new IconManagerUtility();

    private TaskModel task;
    private boolean isStrategyModified;
    private CompareStrategy strategy;
    private List<ComparedItemOperItemRowModel> list;


    @FXML private Label lbLeftAddCount;
    @FXML private Label lbLeftUpdateCount;
    @FXML private Label lbLeftDeleteCount;
    @FXML private Label lbRightDeleteCount;
    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    @FXML private ImageView imgCompareStrategy;
    @FXML private Label modifiedIndicator;

    @FXML private Label sizeToLeft;
    @FXML private Label sizeToRight;


    public RecapitulationBeforeCopyFilesController() {
    }

    public void setGui() {
        lbLeftAddCount.setText(String.valueOf(comparedAndStatisticsData.getLeftAddCount()));
        lbLeftUpdateCount.setText(String.valueOf(comparedAndStatisticsData.getLeftUpdateCount()));
        lbLeftDeleteCount.setText(String.valueOf(comparedAndStatisticsData.getLeftDeleteCount()));
        lbRightDeleteCount.setText(String.valueOf(comparedAndStatisticsData.getRightDeleteCount()));
        lbRightUpdateCount.setText(String.valueOf(comparedAndStatisticsData.getRightUpdateCount()));
        lbRightAddCount.setText(String.valueOf(comparedAndStatisticsData.getRightAddCount()));

        imgCompareStrategy.setImage(GuiUtiliy.findIconForStrategy(strategy));

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
            System.out.println(e);
            ErrorUtility.showError_WindowLoading(e);

        }
    }

    public void setText_SizeToLeft(String text) {
        sizeToLeft.setText(text);
    }
    public void setText_SizeToRight(String text) {
        sizeToRight.setText(text);
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

