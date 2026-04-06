package cz.sitina.bakalarskaprace.zalohovyandel.controllers.components;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalBackupItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
    @FXML private Label lbTypeOfWorkInput;
    @FXML private ImageView imgStrategy;
    @FXML private Label lbDurationTime;
    @FXML private Label lbSuccessCount;
    @FXML private Label lbErrorCount;
    @FXML private ListView<String> errorPathsContent;

    @FXML private Label lbHeaderLeft;
    @FXML private Label lbHeaderRight;
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
        if (dataPack == null) {
            return;
        }
        this.dataPack = dataPack;
        // nadpis
        imgTitleProcessStatus.setImage(GuiUtility.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        imgTitleStrategy.setImage(GuiUtility.findIconForStrategy(dataPack.getCompareStrategy()));
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

    public void setItem(JournalBackupItemDataPack dataPack) {
        if (dataPack == null) {
            return;
        }
        this.dataPack = dataPack;
        // nadpis
        imgTitleProcessStatus.setImage(GuiUtility.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        imgTitleStrategy.setImage(GuiUtility.getIconBackupWork(dataPack.isDataRecovery()));
        DateTimeFormatter formatterDateTime = DateTimeFormatter.ofPattern("dd. MM. yyyy.   HH:mm");
        lbStartDate.setText(dataPack.getDateTimeOfStart().format(formatterDateTime));
        lbTitleErrorCount.setText(String.valueOf(dataPack.getFailCount()));

        // obsah
        lbFinishProcessStatus.setText(getStatusName(dataPack.getFinishProcesStatus()));
        lbImgFinishProcessStatus.setImage(GuiUtility.getIconProcessStatus(dataPack.getFinishProcesStatus()));
        imgStrategy.setImage(GuiUtility.getIconBackupWork(dataPack.isDataRecovery()));
        lbTypeOfWorkInput.setText((dataPack.isDataRecovery()) ? "Obnova":"Záloha");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm:ss");
        lbDurationTime.setText(dataPack.getProcessDuration().format(formatterTime));
        lbSuccessCount.setText(String.valueOf(dataPack.getFileCount()));
        lbErrorCount.setText(String.valueOf(dataPack.getFailCount()));




        if (!dataPack.isDataRecovery()) {
            lbHeaderLeft.setText("Zdroj: ");
            lbHeaderRight.setText("Cíl: ");
            lbLeftRootPath.setText(dataPack.getLeftRootPath());

        } else {
            lbHeaderLeft.setText("Balíček: ");
            lbHeaderRight.setText("Obnova: ");
            lbLeftRootPath.setText(getGuiPackageName(dataPack.getLeftRootPath()));
        }
        lbRightRootPath.setText(dataPack.getRightRootPath());


        for (String path : dataPack.getFailPathList()) {
            errorPathsContent.getItems().add(path);
        }

    }

    private String getGuiPackageName(String status) {
        if (dataPack.getLeftRootPath().equals("Plná obnova dat")) {
            return dataPack.getLeftRootPath();
        }
        String[] nameParts = dataPack.getLeftRootPath().split("_");
        String guiName;

        if (nameParts[0].contains("FullBackup")) {
            guiName = "Plná záloha " + nameParts[1];
        } else {
            guiName = "Přírůstek " + nameParts[1];
        }
        // odstraníme .zip.enc
        String[] separateDate = nameParts[2].split("\\.");


        return guiName + "  " +
                Utility.getDateOnly(separateDate[0]) + "  " +
                Utility.getTimeOnly(separateDate[0]);
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
        List<JournalItemDataPack> reversedList = new ArrayList<>(copyOfEditedJournal);
        Collections.reverse(reversedList);
        lvJournalContainer.getItems().addAll(reversedList);
    }

    public void setCopyOfEditedJournal(List<JournalItemDataPack> copyOfEditedJournal) {
        this.copyOfEditedJournal = copyOfEditedJournal;
    }


    public void setThisTitlePane(ListView listView) {
        this.lvJournalContainer = listView;
    }
}
