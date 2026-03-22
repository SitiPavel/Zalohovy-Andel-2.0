package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.SyncBackupProcessService;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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

    private SyncBackupProcessService syncBackupProcessService = new SyncBackupProcessService();
    private JsonDataManagerService jsonService = new JsonDataManagerService();

    private TaskModel originTaskModel;
    private TaskModel editedTaskModel;
    private XYChart.Series<Number, Number> speedSeries;
    private Task<Void> syncBackupProcessTask;
    private long startTime;
    private Timeline uiTimeline;
    private Utility utility = new Utility();


    private int fileCount = 0;
    private int errorCount = 0;
    private boolean isChartConfigured;
    int totalFilesToProcess = 0;
    int filesInItemOperItemROw;
    String currentFilePath = "";

    /// žurnál info
    private LocalDateTime dateTimeOfStart = LocalDateTime.now();
    private int successCount;
    private Set<String> failPathList = new HashSet<>();


    public void initialize() {
        // nastavení default celého grafu
        speedSeries = new XYChart.Series<>();
        lineChartSpeedGraph.getData().add(speedSeries);
        lineChartSpeedGraph.setAnimated(false);

        xAxis = (NumberAxis) lineChartSpeedGraph.getXAxis();
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(100);
        // nastavení kroku po 10 %
        xAxis.setTickUnit(10);

        Tooltip.install(containerExpectedTime, utility.createToolTip_300ms("Odhadovaný čas"));
        Tooltip.install(containerTime, utility.createToolTip_300ms("Uplynulý čas "));
        Tooltip.install(containerFiles, utility.createToolTip_300ms("Hotové soubory"));
        Tooltip.install(containerProcessed, utility.createToolTip_300ms("Hotové soubory vyžadující operaci"));
        Tooltip.install(containerError, utility.createToolTip_300ms("Soubory s chybou"));
    }

    public void startProcess(
            TaskModel originTaskModel,
            TaskModel editedTaskModel,
            List<ComparedItemOperItemRowModel> list
    ) {
        this.originTaskModel = originTaskModel;
        this.editedTaskModel = editedTaskModel;
        isChartConfigured =false;

        // Získáme celkový počet souborů
        totalFilesToProcess = list.size();

        speedSeries.getData().clear();
        speedSeries.getData().add(new XYChart.Data<>(0, 0));
        startTime = System.currentTimeMillis();






        // nastavení nadpisu
        if (editedTaskModel instanceof ManualSyncTask) {
            lbHeader.setText("Probíhá synchronizace");
        } else {
            lbHeader.setText("Probíhá zálohování");
        }

        syncBackupProcessTask = new Task<>() {
            @Override
            protected Void call() throws Exception {


                syncBackupProcessService.executeCopyFiles(
                        editedTaskModel,
                        list,
                        new SyncBackupProcessService.ProgressCallback() {
                            private long lastTime = startTime;
                            private long lastBytes = 0;
                            private int lastProcessedFileCount = 0;
                            private double lastSpeedForGraph = 0.0;


                            @Override
                            public void onProgress(long bytesProcessed, long totalBytes, String currentFile,
                                                   int processedFileCount, String callbackCurrentFile) {

                                currentFilePath = callbackCurrentFile;
                                // 1. Změna nadpisů a os (Jen jednou na začátku)
                                if (!isChartConfigured) {
                                    Platform.runLater(() -> {
                                        if (totalBytes == 0) {
                                            lbHeader.setText("Probíhá odstraňování");
                                            speedSeries.setName("Zpracované soubory");
                                            // NUTNÉ: Přepíše text "MB/s" na "Položek/s" na ose Y
                                            ((NumberAxis) lineChartSpeedGraph.getYAxis()).setLabel("Rychlost (Položek/s)");
                                        }
                                    });
                                    isChartConfigured = true;
                                }

                                //update progressbaru
                                updateProgress(bytesProcessed, totalBytes);
                                updateMessage(currentFile);
                                // nastaví cestu k aktuálnímu zpracovávanému souboru

                                long now = System.currentTimeMillis();
                                boolean isFinished;
                                if (totalBytes > 0) {
                                    isFinished = (bytesProcessed == totalBytes);
                                } else {
                                    isFinished = (processedFileCount == totalFilesToProcess);
                                }

                                //projíždí každých 250 milisekund
                                if (now -lastTime > 250 || isFinished) {
                                    Platform.runLater(()->{
                                            lbFileInProcess.setText(currentFile);
                                    });

                                    // přibilé zpracované byty od minuléch 250 minisekund

                                    double timeDelta =(now - lastTime) / 1000.0;

                                    if (timeDelta >0.001) { // pokud máme UPDATE nebo ADD
                                        double speedPerSecond;
                                        double percentDone;
                                        double expectedTime;

                                        if (totalBytes > 0) {
                                            long deltaData = bytesProcessed - lastBytes;

                                            speedPerSecond = (deltaData / 1024.0 / 1024.0) / timeDelta;
                                            percentDone = ((double) bytesProcessed / totalBytes) * 100;

                                            // Zbývající čas (pro Label) - počítáme z celkového průměru nebo aktuální rychlosti
                                            long remainingBytes = totalBytes - bytesProcessed;
                                            expectedTime = (speedPerSecond > 0) ?
                                                    (remainingBytes / 1024.0 / 1024.0) / speedPerSecond : 0;
                                        }else {// pokud jsou jen DELETE tak ukazujeme jen v souborech
                                            long deltaData = processedFileCount - lastProcessedFileCount;
                                            speedPerSecond = deltaData / timeDelta;

                                            if (speedPerSecond > 0) {
                                                percentDone =((double) processedFileCount / totalFilesToProcess) * 100;
                                                long remainingFiles = totalFilesToProcess - processedFileCount;
                                                expectedTime =  remainingFiles / speedPerSecond;
                                            } else {
                                                percentDone = 100; expectedTime = 0;
                                            }


                                            updateProgress(processedFileCount, totalFilesToProcess);
                                        }

                                        Platform.runLater(() -> {
                                            lbProcessedCount.setText(String.valueOf(processedFileCount));
                                            successCount =processedFileCount;
                                            updateSpeedChart(speedPerSecond, percentDone, expectedTime);
                                        });

                                        // Uložení stavů pro další cyklus
                                        lastTime = now;
                                        lastBytes = bytesProcessed;
                                        lastProcessedFileCount = processedFileCount;
                                    }
                                }
                            }

                            @Override
                            public void onItemCompleted(boolean success) {
                                Platform.runLater(() ->{
                                    if (success) {
                                        fileCount++;
                                        lbFileCount.setText(String.valueOf(fileCount));
                                    } else {
                                        errorCount++;
                                        lbErrorCount.setText(String.valueOf(errorCount));
                                    }
                                });
                            }
                        },
                        this::isCancelled
                );
                return null;
            }

        };

        // UI Timeline
        uiTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long elapsedMilliS = System.currentTimeMillis() - startTime;
            // Ochrana proti zápornému času (kdyby systémový čas přeskočil)
            if (elapsedMilliS < 0) elapsedMilliS = 0;
            lbTime.setText(formatDuration(elapsedMilliS));
        }));
        uiTimeline.setCycleCount(Timeline.INDEFINITE);
        uiTimeline.play();

        // závěrečná informace o dokončení procesu
        syncBackupProcessTask.setOnSucceeded(event ->
                processFinished("Uspěšně dokončeno.", "✔", false, FinishProcesStatus.SUCCESS));

        syncBackupProcessTask.setOnCancelled(event -> processFinished(
                "Proces byl přerušen.", "✘", false, FinishProcesStatus.INTERRUPTED));

        syncBackupProcessTask.setOnFailed(event ->  {
                String msg = "Vyskytla se neznámá chyba";
                if (syncBackupProcessTask.getException() != null){
                    msg = syncBackupProcessTask.getException().getMessage();
                }
                processFinished("Vyskytla se chyba.", "Chyba: " + msg, true,FinishProcesStatus.FAILURE);
        });

        chartProgressBackground.setPrefWidth(0);
        chartProgressBackground.prefWidthProperty().bind(
                chartContainer.widthProperty().multiply(syncBackupProcessTask.progressProperty())
        );


        new Thread(syncBackupProcessTask).start();
    }

    private void updateSpeedChart(double speedPerSecond, double percentDone, double expectedTime) {

        speedSeries.getData().add(new XYChart.Data<>(percentDone, speedPerSecond));
        lbExpectedTime.setText(formatDuration((long) expectedTime * 1000));


    }
    private String formatDuration(long durationMillis) {
        long s = durationMillis / 1000;
        return String.format("%02d:%02d:%02d", s/3600, (s % 3600) / 60,  s % 60);
    }

    private void processFinished(String title, String message, boolean isError, FinishProcesStatus finishProcesStatus) {
        uiTimeline.stop();
        btnCancel.setText("Zavřít");
        lbHeader.setText(title);
        lbFileInProcess.setText(message);
        btnCancel.setOnAction(e -> ((Stage) btnCancel.getScene().getWindow()).close());
        if (isError) {
            System.out.println(message);
        }

        // příprava dat, vytvoření a vložení journal reportu do modelů
        long durationMillis = System.currentTimeMillis() - startTime;
        LocalTime processDuration = LocalTime.ofSecondOfDay(durationMillis / 1000);

        if (finishProcesStatus == FinishProcesStatus.FAILURE) {
            failPathList.add(currentFilePath);
        }

        JournalItemDataPack reportData = new JournalItemDataPack(
                dateTimeOfStart,
                processDuration,
                editedTaskModel.getCompareStrategy(),
                finishProcesStatus,
                fileCount,
                errorCount,
                failPathList
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
        if (syncBackupProcessTask != null && syncBackupProcessTask.isRunning()) {
            syncBackupProcessTask.cancel();
        } else {
            ((Stage) btnCancel.getScene().getWindow()).close();
        }
    }

}
