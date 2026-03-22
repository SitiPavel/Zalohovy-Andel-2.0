package cz.sitina.bakalarskaprace.zalohovyandel.controllers.components;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtiliy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;

import java.time.format.DateTimeFormatter;
import java.util.Formatter;

public class JournalItemController {

    // nadpis
    @FXML private ImageView imgTitleProcessStatus;
    @FXML private ImageView imgTitleStrategy;
    @FXML private Label lbStartDate;

    // obsah
    @FXML private Label lbFinishProcessStatus;
    @FXML private ImageView lbImgFinishProcessStatus;
    @FXML private ImageView imgStrategy;
    @FXML private Label lbDurationTime;
    @FXML private Label lbSuccessCount;
    @FXML private Label lbErrorCount;
    @FXML private Label lbSkipCount;
    @FXML private TitledPane errorPathsContainer;

    private Utility utility = new Utility();




    public void setItem(JournalItemDataPack dataPack) {
        // nadpis
        imgTitleProcessStatus.setImage(GuiUtiliy.findIconForStrategy(dataPack.getCompareStrategy()));
        imgTitleStrategy.setImage(GuiUtiliy.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd. MM. yyyy.   HH:mm");
        lbStartDate.setText(dataPack.getDateTimeOfStart().format(formatterDateTime));

        // obsah
        lbFinishProcessStatus.setText(getStatusName(dataPack.getFinishProcesStatus()));
        lbImgFinishProcessStatus.setImage(GuiUtiliy.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        imgStrategy.setImage(GuiUtiliy.findIconForStrategy(dataPack.getCompareStrategy()));
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");
        lbDurationTime.setText(dataPack.getProcesDuration().format(formatterTime));
        lbSuccessCount.setText(String.valueOf(dataPack.getSucessCount()));
        lbErrorCount.setText(String.valueOf(dataPack.getFailCount()));
        lbSkipCount.setText(String.valueOf(dataPack.getSkipedFiles()));






    }

    private String getStatusName(FinishProcesStatus status) {
        return switch (status) {
            case SUCCESS -> "Úspěch";
            case FAILURE -> "Ukončeno s chybou";
            case INTERRUPTED -> "Přerušeno";
        };
    }

    @FXML
    private void deleteThisJurnalItem(ActionEvent event ) {

    }


}
