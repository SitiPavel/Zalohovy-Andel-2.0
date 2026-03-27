package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtiliy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BackupRecapitulationBeforeCopyFilesController {

    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    @FXML private Label modifiedIndicator;
    @FXML private Label sizeToRight;

    private ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData;
    private boolean isStrategyModified = false;

    private BackupTask originalBackupTask;
    private BackupTask editableBackupTask;

    @FXML
    private void initialize() {

    }

    public void setGui() {
        lbRightUpdateCount.setText(String.valueOf(comparedAndStatisticsData.getRightUpdateCount()));
        lbRightAddCount.setText(String.valueOf(comparedAndStatisticsData.getRightAddCount()));

        // indikátor upravy se naastaví podle
        modifiedIndicator.setDisable(isStrategyModified);
    }

    @FXML
    public void startCopyFiles(ActionEvent event) {

    }

    public void setComparedAndStatisticsData(ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData) {
        this.comparedAndStatisticsData = comparedAndStatisticsData;
    }

    public void setStrategyModified(boolean strategyModified) {
        isStrategyModified = strategyModified;
    }

    public void setOriginalBackupTask(BackupTask originalBackupTask) {
        this.originalBackupTask = originalBackupTask;
    }

    public void setEditableBackupTask(BackupTask editableBackupTask) {
        this.editableBackupTask = editableBackupTask;
    }
}
