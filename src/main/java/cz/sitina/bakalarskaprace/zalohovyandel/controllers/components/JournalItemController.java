package cz.sitina.bakalarskaprace.zalohovyandel.controllers.components;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class JournalItemController {

    @FXML private TitledPane rootTitlePain;
    // nadpis
    @FXML private ImageView imgTitleProcessStatus;
    @FXML private ImageView imgTitleStrategy;
    @FXML private Label lbStartDate;
    @FXML private Label lbTitleErrorCount;

    // obsah
    @FXML private Label lbFinishProcessStatus;
    @FXML private ImageView lbImgFinishProcessStatus;
    @FXML private ImageView imgStrategy;
    @FXML private Label lbDurationTime;
    @FXML private Label lbSuccessCount;
    @FXML private Label lbErrorCount;
    @FXML private ListView<String> errorPathsContent;
    @FXML private Label lbLeftRootPath;
    @FXML private Label lbRightRootPath;


    private List<JournalItemDataPack> copyOfEditedJournal;
    private JournalItemDataPack dataPack;
    private ListView<JournalItemDataPack> lvJournalContainer;

    @FXML
    private void initialize() {
        rootTitlePain.setExpanded(false);

        errorPathsContent.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-control-inner-background: transparent; " +
                        "-fx-padding: 0;"
        );
    }

    public void setItem(JournalItemDataPack dataPack) {
        this.dataPack = dataPack;
        // nadpis
        imgTitleProcessStatus.setImage(GuiUtility.findIconForStrategy(dataPack.getCompareStrategy()));
        imgTitleStrategy.setImage(GuiUtility.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd. MM. yyyy.   HH:mm");
        lbStartDate.setText(dataPack.getDateTimeOfStart().format(formatterDateTime));
        lbTitleErrorCount.setText(String.valueOf(dataPack.getFailCount()));

        // obsah
        lbFinishProcessStatus.setText(getStatusName(dataPack.getFinishProcesStatus()));
        lbImgFinishProcessStatus.setImage(GuiUtility.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        imgStrategy.setImage(GuiUtility.findIconForStrategy(dataPack.getCompareStrategy()));
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
        lbDurationTime.setText(dataPack.getProcessDuration().format(formatterTime));
        lbSuccessCount.setText(String.valueOf(dataPack.getFileCount()));
        lbErrorCount.setText(String.valueOf(dataPack.getFailCount()));
        lbLeftRootPath.setText(dataPack.getLeftRootPath());
        lbRightRootPath.setText(dataPack.getRightRootPath());

        for (String path : dataPack.getFailPathList()) {
            errorPathsContent.getItems().add(path);
        }

    }

    private String getStatusName(FinishProcesStatus status) {
        return switch (status) {
            case SUCCESS -> "Úspěch";
            case FAILURE -> "Ukončeno s chybou";
            case INTERRUPTED -> "Přerušeno";
        };
    }

    @FXML
    private void deleteThisJournalItem(ActionEvent event ) {
        copyOfEditedJournal.removeIf(item -> item.getId() == dataPack.getId());
        refreshErrorContentList();
    }

    private void refreshErrorContentList() {
        lvJournalContainer.getItems().clear();
        lvJournalContainer.getItems().addAll(copyOfEditedJournal);
    }

    public void setCopyOfEditedJournal(List<JournalItemDataPack> copyOfEditedJournal) {
        this.copyOfEditedJournal = copyOfEditedJournal;
    }


    public void setThisTitlePane(ListView listView) {
        this.lvJournalContainer = listView;
    }
}
