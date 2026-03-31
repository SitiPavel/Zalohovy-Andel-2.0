package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.copyProcessControlers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.CopyProcessServices.RecoveryProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.FilesUtility;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;

import java.nio.file.Path;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.List;

public class RestoreCopyProcessController extends CopyProcessController{


    // Servisy
    private final RecoveryProcessService recoveryProcessService = new RecoveryProcessService();

    // vstupy přez setery
    private String fullRestoreTarget;
    private Path  targetDirectory;
    private BackupTask originTaskModel;
    private BackupTask editedTaskModel;



    public void executeProcess(BackupTask originTaskModel, BackupTask editedTaskModel,
                               List<BackupDatabaseDTO> foundDtoItems, Path targetDirectory) {

        this.editedTaskModel = editedTaskModel;
        this.originTaskModel = originTaskModel;
        this.targetDirectory = targetDirectory;
        this.totalFilesToProcess = foundDtoItems.size();

        speedSeries.getData().clear();
        speedSeries.getData().add(new XYChart.Data<>(0, 0));
        startTime = System.currentTimeMillis();

        lbHeader.setText("Probíhá obnova dat");

        backupProcessTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                recoveryProcessService.executeRecovery(
                        foundDtoItems,
                        targetDirectory,
                        new FilesUtility.ProgressCallback() {
                            private long lastTime = startTime;
                            private long lastBytes = 0;

                            @Override
                            public void onProgress(long bytesProcessed, long totalBytes, String currentFile, int processedFileCount) {
                                // Update standardního progress baru v Tasku
                                updateProgress(bytesProcessed, totalBytes);
                                updateMessage(currentFile);

                                // Zpracování updatu grafu (s throttlingem 250ms)
                                boolean didUpdate = handleProgressUpdate(bytesProcessed, totalBytes, currentFile, processedFileCount, lastTime, lastBytes);

                                if (didUpdate) {
                                    lastTime = System.currentTimeMillis();
                                    lastBytes = bytesProcessed;
                                }
                            }

                            @Override
                            public void onItemCompleted(boolean success, ComparedItemOperItemRowModel row) {

                            }

                            @Override
                            public void onItemCompleted(boolean success, BackupDatabaseDTO dto) {
                                handleItemCompleted_Dto(success, dto);
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

        // Události Tasku pro Obnovu
        backupProcessTask.setOnSucceeded(event -> processFinished("Obnova dokončena.", "✔", false, FinishProcesStatus.SUCCESS));
        backupProcessTask.setOnCancelled(event -> processFinished("Obnova přerušena.", "✘", false, FinishProcesStatus.INTERRUPTED));
        backupProcessTask.setOnFailed(event -> {
            String msg = (backupProcessTask.getException() != null) ? backupProcessTask.getException().getMessage() : "Neznámá chyba";
            processFinished("Chyba při obnově.", "Chyba: " + msg, true, FinishProcesStatus.FAILURE);
        });

        // Binding pozadí (ukazatel průběhu za grafem)
        chartProgressBackground.setPrefWidth(0);
        chartProgressBackground.prefWidthProperty().bind(
                chartContainer.widthProperty().multiply(backupProcessTask.progressProperty())
        );

        new Thread(backupProcessTask).start();
    }


    // Metoda vracející boolean pro plynulý update grafu
    private boolean handleProgressUpdate(long bytesProcessed, long totalBytes, String currentFile, int processedFileCount, long lastTime, long lastBytes) {
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

    // Metoda handleProgressUpdate je zcela identická s tvou původní.

    private void processFinished(String title, String message, boolean isError, FinishProcesStatus finishProcesStatus) {
        uiTimeline.stop();
        btnCancel.setText("Zavřít");
        lbHeader.setText(title);
        lbFileInProcess.setText(message);
        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());

        if (isError) System.out.println(message);

        long durationMillis = System.currentTimeMillis() - startTime;
        LocalTime processDuration = LocalTime.ofSecondOfDay(durationMillis / 1000);

        // Tvorba žurnálu pro obnovu. Odvozuji parametry podle toho, co vyžaduje tvůj JournalItemDataPack.
        JournalItemDataPack reportData = new JournalItemDataPack(
                dateTimeOfStart, processDuration, originTaskModel.getCompareStrategy(),
                finishProcesStatus, fileCount, errorCount, failPathList,
                originTaskModel.getLeftPath(), originTaskModel.getRightPath() // Zde zapisujeme novou cestu
        );

        originTaskModel.getJurnalInfoList().add(reportData);
        jsonService.updateTask_saveOldSave(originTaskModel);

        if (canBeResetTreeTable != null) {
            canBeResetTreeTable.run();
        }
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


    public void setFullRestoreTarget(String fullRestoreTarget) {
        this.fullRestoreTarget = fullRestoreTarget;
    }
}
