package cz.sitina.bakalarskaprace.zalohovyandel.controllers.components;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class JurnalItemController {

    // nadpis
    @FXML private ImageView imgTitleProcesStatus;
    @FXML private ImageView imgTitleStrategy;
    @FXML private Label lbStartDate;
    @FXML private Label lbStartTime;

    // obsah
    @FXML private Label lbFinishProcesStatus;
    @FXML private ImageView imgStrategi;
    @FXML private Label lbDurationTime;
    @FXML private Label lbSuccessCount;
    @FXML private Label lbErrorCount;
    @FXML private Label lbSkipCount1;


    private TaskModel taskModel;




    @FXML
    private void deleteThisJurnalItem(ActionEvent event ) {

    }

    public void setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
    }
}
