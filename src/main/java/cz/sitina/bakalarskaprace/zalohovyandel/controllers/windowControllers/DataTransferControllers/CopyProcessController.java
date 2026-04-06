package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.DataTransferControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DataTransferService.BackupProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
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

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public abstract class CopyProcessController {
    @FXML protected Label lbHeader;
    @FXML protected Label lbFileInProcess;
    @FXML protected Label lbExpectedTime;
    @FXML protected Label lbTime;
    @FXML protected Label lbProcessedCount;
    @FXML protected Label lbFileCount;
    @FXML protected Label lbErrorCount;
    @FXML protected LineChart<Number, Number> lineChartSpeedGraph;
    @FXML protected NumberAxis xAxis;
    @FXML protected Button btnCancel;
    @FXML protected Region chartProgressBackground;
    @FXML protected StackPane chartContainer;

    @FXML protected HBox containerExpectedTime;
    @FXML protected HBox containerTime;
    @FXML protected HBox containerFiles;
    @FXML protected HBox containerProcessed;
    @FXML protected HBox containerError;

    // servis
    protected final Utility utility = new Utility();
    protected final BackupProcessService backupProcessService = new BackupProcessService();
    protected final JsonDataManagerService jsonService = JsonDataManagerService.getInstance();;


    // data objekty
    protected XYChart.Series<Number, Number> speedSeries;

    protected long startTime;
    protected Timeline uiTimeline;

    protected int fileCount = 0;
    protected int errorCount = 0;
    protected int totalFilesToProcess = 0;
    protected Task<Void> backupProcessTask;
    protected double smoothedSpeedPerSecond = 0;


    protected Runnable canBeResetTreeTable;


    // žurnál info
    protected final LocalDateTime dateTimeOfStart = LocalDateTime.now();
    protected final Set<String> failPathList = new HashSet<>();

    @FXML
    protected void initialize() {
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

    /// V konkrétních třídách se doplní startProcess
    /// public void startProcess

    // metody pro startProcess


    // Pomocná metoda pro zapsání chyby nebo úspěchu souboru
    protected void handleItemCompleted_Row(boolean success, ComparedItemOperItemRowModel row) {
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

    protected void handleItemCompleted_Dto(boolean success, BackupDatabaseDTO dto) {
        Platform.runLater(() -> {
            if (success) {
                fileCount++;
                lbFileCount.setText(String.valueOf(fileCount));
            } else {
                errorCount++;
                lbErrorCount.setText(String.valueOf(errorCount));
                String errorPath = dto.getAbsolutPath();
                failPathList.add(errorPath);
            }
        });
    }




        ///  =======^=======^=======^=======^=======




}
