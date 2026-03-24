package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.File;

public class CreateBackupTaskController extends CreateTaskController {


    @FXML
    public void closeWindow_Create(ActionEvent event) {


        String errorMassage = sharedInputCheck(
                "Cesta ke zdrojové složce neexistuje!",
                "Cesta k cílové složce neexistuje!");


        if (!errorMassage.isEmpty()) {
            ErrorUtility.showError_form(errorMassage);
            return;
        }

        BackupTask newTask = new BackupTask(
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
