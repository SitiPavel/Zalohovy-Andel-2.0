package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy.*;

public class ManualCompareSettingsController {
    // ostaní definice
    private ManualSyncTask task;
    private CompareStrategy strategy;
    private Runnable markAsDirtyCallback;

    // rádio skupiny
    @FXML private ToggleGroup SelectCompareType;
    @FXML private ToggleGroup SelectedCompareStrategy;

    // radio buttny

    @FXML private RadioButton rbSizeModifi;
    @FXML private RadioButton rbHash;

    @FXML private RadioButton rbStrategy_Bi;
    @FXML private RadioButton rbStrategy_oneToRight;
    @FXML private RadioButton rbStrategy_oneToLeft;
    @FXML private RadioButton rbStrategy_MirrToRight;
    @FXML private RadioButton rbStrategy_MirrToLeft;

    // proměné na detekování změn
    private String oldStrategy;
    private String oldTypeOfCompare;



    public ManualCompareSettingsController() {
    }



    @FXML
    public void confirmSettings(ActionEvent event) {
        ToggleButton selectedCompare = (ToggleButton) SelectCompareType.getSelectedToggle();
        ToggleButton selectedStrategy = (ToggleButton) SelectedCompareStrategy.getSelectedToggle();

        if (selectedCompare == null || selectedStrategy == null) {
            ErrorUtility.getInstance("Kritická chyba vstupů",
                    "Některé z nastavení není vyplněno.",
                    "Před odesláním zkontrolujte všechny povinná pole.");
        } else {
            String typeOfCompare = selectedCompare.getId();
            if (typeOfCompare.equals("rbSizeModifi")) {
                task.setContentHash(false);
            } else if (typeOfCompare.equals("rbHash")) {
                task.setContentHash(true);
            }

            String compareStrategy = selectedStrategy.getId();
            switch (compareStrategy) {
                case "rbStrategy_Bi": task.setCompareStrategy(BIDIRECTIONAL_SYNC);break;
                case "rbStrategy_oneToRight": task.setCompareStrategy(BACKUP_TO_RIGHT);break;
                case "rbStrategy_oneToLeft": task.setCompareStrategy(BACKUP_TO_LEFT);break;
                case "rbStrategy_MirrToRight": task.setCompareStrategy(MIRRORING_to_RIGHT);break;
                case "rbStrategy_MirrToLeft": task.setCompareStrategy(MIRRORING_TO_lEFT);break;
            }

            // callback se provede pokud byly data opravdu změněna
            if (!oldStrategy.equals(compareStrategy) || !oldTypeOfCompare.equals(typeOfCompare)) {
                // pro callback
                if (markAsDirtyCallback != null) {
                    markAsDirtyCallback.run();
                }
            }

            //zavření okna
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            stage.close();
        }

    }

    public void setStrategy(CompareStrategy strategy) {
        this.strategy = strategy;
    }

    public void setCallbackDirtyFlag(Runnable onSaveCallback) {
        this.markAsDirtyCallback = onSaveCallback;

    }

    public void setTask(ManualSyncTask task) {
        this.task = task;

        if (!task.isContentHash()) {
            rbSizeModifi.setSelected(true);
            oldTypeOfCompare = rbSizeModifi.getId();
        } else {
            rbHash.setSelected(true);
            oldTypeOfCompare = rbHash.getId();
        }

        switch (task.getCompareStrategy()) {
            case BIDIRECTIONAL_SYNC -> {
                rbStrategy_Bi.setSelected(true);
                oldStrategy = rbStrategy_Bi.getId();
            }
            case BACKUP_TO_RIGHT -> {
                rbStrategy_oneToRight.setSelected(true);
                oldStrategy = rbStrategy_oneToRight.getId();
            }
            case BACKUP_TO_LEFT -> {
                rbStrategy_oneToLeft.setSelected(true);
                oldStrategy = rbStrategy_oneToLeft.getId();
            }
            case MIRRORING_to_RIGHT -> {
                rbStrategy_MirrToRight.setSelected(true);
                oldStrategy = rbStrategy_MirrToRight.getId();
            }
            case MIRRORING_TO_lEFT -> {
                rbStrategy_MirrToLeft.setSelected(true);
                oldStrategy = rbStrategy_MirrToLeft.getId();
            }
        }

    }
}
