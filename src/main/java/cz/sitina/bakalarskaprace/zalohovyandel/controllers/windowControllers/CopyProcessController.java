package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.BackupProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.SynchronizationProcessService;
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


public class CopyProcessController {
    @FXML private Label lbHeader;
    @FXML private Label lbFileInProcess;
    @FXML private Label lbExpectedTime;
    @FXML private Label lbTime;
    @FXML private Label lbProcessedCount;
    @FXML private Label lbFileCount;
    @FXML private Label lbErrorCount;
    @FXML private LineChart<Number, Number> lineChartSpeedGraph;
    @FXML private NumberAxis xAxis;
    @FXML private Button btnCancel;
    @FXML private Region chartProgressBackground;
    @FXML private StackPane chartContainer;

    @FXML private HBox containerExpectedTime;
    @FXML private HBox containerTime;
    @FXML private HBox containerFiles;
    @FXML private HBox containerProcessed;
    @FXML private HBox containerError;

    // Servisy
    private final BackupProcessService backupProcessService = new BackupProcessService();
    private final JsonDataManagerService jsonService = new JsonDataManagerService();
    private final Utility utility = new Utility();

    private BackupTask originTaskModel;
    private BackupTask editedTaskModel;

    private XYChart.Series<Number, Number> speedSeries;
    private Task<Void> backupProcessTask;
    private long startTime;
    private Timeline uiTimeline;

    private int fileCount = 0;
    private int errorCount = 0;
    private int totalFilesToProcess = 0;

    /// žurnál info
    private final LocalDateTime dateTimeOfStart = LocalDateTime.now();
    private final Set<String> failPathList = new HashSet<>();

    @FXML
    public void initialize() {
        speedSeries = new XYChart.Series<>();
        lineChartSpeedGraph.getData().add(speedSeries);
        lineChartSpeedGraph.setAnimated(false);

        xAxis = (NumberAxis) lineChartSpeedGraph.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(100);
        xAxis.setTickUnit(10);

        Tooltip.install(containerExpectedTime, utility.createToolTip_300ms("Odhadovaný čas"));
        Tooltip.install(containerTime, utility.createToolTip_300ms("Uplynulý čas"));
        Tooltip.install(containerFiles, utility.createToolTip_300ms("Hotové soubory"));
        Tooltip.install(containerProcessed, utility.createToolTip_300ms("Zálohované soubory"));
        Tooltip.install(containerError, utility.createToolTip_300ms("Soubory s chybou"));
    }

    public void startProcess(BackupTask originTaskModel, BackupTask editedTaskModel, List<ComparedItemOperItemRowModel> list) {
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
                        new BackupProcessService.ProgressCallback() {
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
                                Platform.runLater(() -> {
                                    if (success) {
                                        fileCount++;
                                        lbFileCount.setText(String.valueOf(fileCount));
                                    } else {
                                        errorCount++;
                                        lbErrorCount.setText(String.valueOf(errorCount));
                                        Path errorPath = row.getNotNullPath();
                                        failPathList.add(errorPath.toString());
                                    }
                                });
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
            lbTime.setText(formatDuration(elapsedMilliS));
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
                    lbExpectedTime.setText(formatDuration((long) expectedTime * 1000));
                });
            }
            return true;
        }
        return false;
    }

    private String formatDuration(long durationMillis) {
        long s = durationMillis / 1000;
        return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
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
}