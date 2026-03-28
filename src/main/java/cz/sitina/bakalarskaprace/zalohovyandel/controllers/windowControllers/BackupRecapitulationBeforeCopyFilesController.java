package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.BackupProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.SynchronizationProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtiliy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BackupRecapitulationBeforeCopyFilesController {

    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    @FXML private Label modifiedIndicator;
    @FXML private Label sizeToRight;

    private ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData;
    private BackupProcessService backupProcessService = new BackupProcessService();
    private boolean isStrategyModified = false;
    private List<ComparedItemOperItemRowModel> comparedFilesList = new ArrayList<>();

    private BackupTask originalBackupTask;
    private BackupTask editableBackupTask;

    private Runnable canBeResetTreeTable;

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
        Stage currentStage = (Stage) modifiedIndicator.getScene().getWindow();
        currentStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/cz/sitina/bakalarskaprace/zalohovyandel/fxml/windows/copyProcessWindow.fxml"));
            BackupCopyProcessController controller = new BackupCopyProcessController();
            loader.setController(controller);
            loader.load(); // Toto zavolá metodu inicialize a nastaví controler podle FXML
            controller.setCanBeResetTreeTable(canBeResetTreeTable);
            controller.startProcess(originalBackupTask, editableBackupTask, comparedFilesList);

            String title =  "Záloha souborů";
            Stage copyProcessStage = WindowManager.openRunningWindow(loader, title, 750, 400);

            copyProcessStage.setOnCloseRequest(e -> {
                controller.cancelAction();
            });


        } catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.showError_WindowLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void setComparedAndStatisticsData(ComparedDataAndStatisticsForCompereOverview comparedAndStatisticsData) {
        this.comparedAndStatisticsData = comparedAndStatisticsData;
    }

    public void setStrategyModified(boolean strategyModified) {
        isStrategyModified = strategyModified;
    }

    public void setEditableBackupTask(BackupTask editableBackupTask) {
        this.editableBackupTask = editableBackupTask;
    }

    public void setComparedFilesList(List<ComparedItemOperItemRowModel> comparedFilesList) {
        this.comparedFilesList = comparedFilesList;
    }

    public void setOriginalBackupTask(BackupTask originalBackupTask) {
        this.originalBackupTask = originalBackupTask;
    }

    public void setCanBeResetTreeTable(Runnable canBeResetTreeTable) {
        this.canBeResetTreeTable = canBeResetTreeTable;
    }
}
