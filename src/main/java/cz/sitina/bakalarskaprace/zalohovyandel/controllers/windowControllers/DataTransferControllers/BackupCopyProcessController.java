package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.DataTransferControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalBackupItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.FilesUtility;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.crypto.SecretKey;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class BackupCopyProcessController extends CopyProcessController{
    // setData
    private BackupTask originTaskModel;
    private BackupTask editedTaskModel;
    private EncryptedBackupTask encryptedTask;
    private SecretKey secretKey;

    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    public void startProcess(BackupTask originTaskModel, BackupTask editedTaskModel,
                             EncryptedBackupTask encryptedBackupTask, List<ComparedItemOperItemRowModel> list,
                             SecretKey secretKey) {
        this.originTaskModel = originTaskModel;
        this.editedTaskModel = editedTaskModel;
        this.totalFilesToProcess = list.size();
        this.encryptedTask = encryptedBackupTask;
        this.secretKey = secretKey;

        speedSeries.getData().clear();
        speedSeries.getData().add(new XYChart.Data<>(0, 0));
        startTime = System.currentTimeMillis();

        lbHeader.setText("Probíhá zálohování");
        // pro získání zrušení
        cancelRequested.set(false);

        backupProcessTask = new Task<>() {
            @Override
            protected Void call() throws Exception {

                backupProcessService.executeBackupProcess(
                        list,
                        editedTaskModel,
                        secretKey,
                        new FilesUtility.ProgressCallback() {
                            private long lastTime = startTime;
                            private long lastBytes = 0;

                            @Override
                            public void onProgress(long bytesProcessed, long totalBytes, String currentFile, int processedFileCount) {
                                updateProgress(bytesProcessed, totalBytes);
                                updateMessage(currentFile);

                                // 2. Voláme update a ZACHYTÍME, jestli se reálně něco stalo
                                boolean didUpdate = handleProgressUpdate(bytesProcessed, totalBytes, currentFile, processedFileCount, lastTime, lastBytes);

                                // 3. KLÍČOVÁ OPRAVA: Musíme aktualizovat lokální stopky, jinak bude if v handleProgressUpdate příště zase true
                                if (didUpdate) {
                                    lastTime = System.currentTimeMillis();
                                    lastBytes = bytesProcessed;
                                }}

                            @Override
                            public void onItemCompleted(boolean success, ComparedItemOperItemRowModel row) {
                                handleItemCompleted_Row(success, row);
                            }

                            @Override
                            public void onItemCompleted(boolean success, BackupDatabaseDTO dto) {

                            }
                        },
                        cancelRequested::get
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

        if (now - lastTime > 250 || isFinished) {

            double timeDelta = (now - lastTime) / 1000.0;

            if (timeDelta > 0.001 && totalBytes > 0) {
                long deltaData = bytesProcessed - lastBytes;
                double speedPerSecond = (deltaData / 1024.0 / 1024.0) / timeDelta;
                double percentDone = ((double) bytesProcessed / totalBytes) * 100;

                if (smoothedSpeedPerSecond == 0) {
                    smoothedSpeedPerSecond = speedPerSecond;
                } else {
                    // Váhy: 10 % nová okamžitá rychlost, 90 % stará stabilní rychlost
                    smoothedSpeedPerSecond = (0.10 * speedPerSecond) + (0.90 * smoothedSpeedPerSecond);
                }

                long remainingBytes = totalBytes - bytesProcessed;
                double expectedTime = (smoothedSpeedPerSecond > 0) ? (remainingBytes / 1024.0 / 1024.0) / smoothedSpeedPerSecond : 0;

                Platform.runLater(() -> {
                    lbFileInProcess.setText(currentFile);
                    lbProcessedCount.setText(String.valueOf(processedFileCount));
                    speedSeries.getData().add(new XYChart.Data<>(percentDone, smoothedSpeedPerSecond));
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

        JournalBackupItemDataPack reportData = new JournalBackupItemDataPack(
                dateTimeOfStart, processDuration, editedTaskModel.getCompareStrategy(),
                false,
                finishProcesStatus, fileCount, errorCount, failPathList,
                editedTaskModel.getLeftPath(), editedTaskModel.getRightPath()
        );

        originTaskModel.getJurnalInfoList().add(reportData);
        editedTaskModel.getJurnalInfoList().add(reportData);
        if (originTaskModel.isEncrypted()) {
            try {
                encryptedTask.updateEncryptedTask(originTaskModel, secretKey);
                jsonService.updateTask_saveOldSave(encryptedTask);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            jsonService.updateTask_saveOldSave(originTaskModel);
        }
        canBeResetTreeTable.run();
        secretKey = null;
    }

    @FXML
    public void cancelByButton(ActionEvent event) {
        cancelAction();
    }

    public void cancelAction() {
        if (backupProcessTask != null && backupProcessTask.isRunning()) {
            cancelRequested.set(true);
            backupProcessTask.cancel();
        } else {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }

    public void setCanBeResetTreeTable(Runnable canBeResetTreeTable) {
        this.canBeResetTreeTable = canBeResetTreeTable;
    }
}