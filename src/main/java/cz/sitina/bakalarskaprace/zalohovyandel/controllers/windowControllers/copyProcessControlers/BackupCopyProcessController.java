package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.copyProcessControlers;

import cz.sitina.bakalarskaprace.zalohovyandel.services.CopyProcessServices.BackupProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.CopyFilesUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class BackupCopyProcessController extends CopyProcessController{
    // setData
    protected BackupTask originTaskModel;
    protected BackupTask editedTaskModel;

    public void startProcess(BackupTask originTaskModel, BackupTask editedTaskModel,
                             List<ComparedItemOperItemRowModel> list) {
        this.originTaskModel = originTaskModel;
        this.editedTaskModel = editedTaskModel;
        this.totalFilesToProcess = list.size();

        speedSeries.getData().clear();
        speedSeries.getData().add(new XYChart.Data<>(0, 0));
        startTime = System.currentTimeMillis();

        lbHeader.setText("Probíhá zálohování");

        backupProcessTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                backupProcessService.executeBackupProcess(
                        list,
                        editedTaskModel,
                        new CopyFilesUtility.ProgressCallback() {
                            private long lastTime = startTime;
                            private long lastBytes = 0;

                            @Override
                            public void onProgress(long bytesProcessed, long totalBytes, String currentFile, int processedFileCount) {
                                // Update standardního progress baru v Tasku
                                updateProgress(bytesProcessed, totalBytes);
                                updateMessage(currentFile);

                                // Zpracování updatu grafu (s throttlingem 250ms)
                                boolean didUpdate = handleProgressUpdate(bytesProcessed, totalBytes, currentFile, processedFileCount, lastTime, lastBytes);
                            }

                            @Override
                            public void onItemCompleted(boolean success, ComparedItemOperItemRowModel row) {
                                handleItemCompleted_Row(success, row);
                            }

                            @Override
                            public void onItemCompleted(boolean success, BackupDatabaseDTO dto) {

                            }
                        },
                        this::isCancelled
                );
                return null;
            }
        };

        // UI Timeline pro stopky
        uiTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long elapsedMilliS = System.currentTimeMillis() - startTime;
            if (elapsedMilliS < 0) elapsedMilliS = 0;
            lbTime.setText(utility.formatDuration(elapsedMilliS));
        }));
        uiTimeline.setCycleCount(Timeline.INDEFINITE);
        uiTimeline.play();

        // Události Tasku
        backupProcessTask.setOnSucceeded(event -> processFinished("Úspěšně dokončeno.", "✔", false, FinishProcesStatus.SUCCESS));
        backupProcessTask.setOnCancelled(event -> processFinished("Proces byl přerušen.", "✘", false, FinishProcesStatus.INTERRUPTED));
        backupProcessTask.setOnFailed(event -> {
            String msg = (backupProcessTask.getException() != null) ? backupProcessTask.getException().getMessage() : "Neznámá chyba";
            processFinished("Vyskytla se chyba.", "Chyba: " + msg, true, FinishProcesStatus.FAILURE);
        });

        // Binding pozadí (ukazatel průběhu za grafem)
        chartProgressBackground.setPrefWidth(0);
        chartProgressBackground.prefWidthProperty().bind(
                chartContainer.widthProperty().multiply(backupProcessTask.progressProperty())
        );

        new Thread(backupProcessTask).start();
    }

    // Metoda vracející boolean pro plynulý update grafu
    protected boolean handleProgressUpdate(long bytesProcessed, long totalBytes,
                                           String currentFile, int processedFileCount,
                                           long lastTime, long lastBytes) {
        long now = System.currentTimeMillis();
        boolean isFinished = (totalBytes > 0) && (bytesProcessed == totalBytes);

        Platform.runLater(() -> lbFileInProcess.setText(currentFile));
        if (now - lastTime > 250 || isFinished) {

            double timeDelta = (now - lastTime) / 1000.0;

            if (timeDelta > 0.001 && totalBytes > 0) {
                long deltaData = bytesProcessed - lastBytes;
                double speedPerSecond = (deltaData / 1024.0 / 1024.0) / timeDelta;
                double percentDone = ((double) bytesProcessed / totalBytes) * 100;

                long remainingBytes = totalBytes - bytesProcessed;
                double expectedTime = (speedPerSecond > 0) ? (remainingBytes / 1024.0 / 1024.0) / speedPerSecond : 0;

                Platform.runLater(() -> {
                    lbProcessedCount.setText(String.valueOf(processedFileCount));
                    speedSeries.getData().add(new XYChart.Data<>(percentDone, speedPerSecond));
                    lbExpectedTime.setText(utility.formatDuration((long) expectedTime * 1000));
                });
            }
            return true;
        }
        return false;
    }




    private void processFinished(String title, String message, boolean isError, FinishProcesStatus finishProcesStatus) {
        uiTimeline.stop();
        btnCancel.setText("Zavřít");
        lbHeader.setText(title);
        lbFileInProcess.setText(message);
        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());

        if (isError) System.out.println(message);

        long durationMillis = System.currentTimeMillis() - startTime;
        LocalTime processDuration = LocalTime.ofSecondOfDay(durationMillis / 1000);

        JournalItemDataPack reportData = new JournalItemDataPack(
                dateTimeOfStart, processDuration, editedTaskModel.getCompareStrategy(),
                finishProcesStatus, fileCount, errorCount, failPathList,
                editedTaskModel.getLeftPath(), editedTaskModel.getRightPath()
        );

        originTaskModel.getJurnalInfoList().add(reportData);
        editedTaskModel.getJurnalInfoList().add(reportData);
        jsonService.updateTask_saveOldSave(originTaskModel);
        canBeResetTreeTable.run();
    }

    @FXML
    public void cancelByButton(ActionEvent event) {
        cancelAction();
    }

    public void cancelAction() {
        if (backupProcessTask != null && backupProcessTask.isRunning()) {
            backupProcessTask.cancel();
        } else {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }

    public void setCanBeResetTreeTable(Runnable canBeResetTreeTable) {
        this.canBeResetTreeTable = canBeResetTreeTable;
    }
}