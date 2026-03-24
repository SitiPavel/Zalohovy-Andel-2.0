package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class CreateManualSyncController extends CreateTaskController {


    @FXML
    public void closeWindow_Create(ActionEvent event) {

        String errorMassage =  sharedInputCheck(
                "Cesta k levé složce neexistuje!",
                "Cesta k pravé složce neexistuje!");

        if (!errorMassage.isEmpty()) {
            ErrorUtility.showError_form(errorMassage);

            return;
        }

        ManualSyncTask newTask = new ManualSyncTask(
                tfName.getText(),
                tfLeftPath.getText(),
                tfRightPath.getText(),
                taDescription.getText()

        );

        jsonService.saveNewTask_saveOldSave(newTask);
        taskRepository.add(newTask);
        isAddedTask.run();
        WindowManager.findByActionAndClose(event);

    }
}
