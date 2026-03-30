package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController;

import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;

public class CreateTaskController {
    protected JsonDataManagerService jsonService = new JsonDataManagerService();

    @FXML protected TextField tfName;
    @FXML protected TextField tfLeftPath;
    @FXML protected TextField tfRightPath;
    @FXML protected TextArea taDescription;

    @FXML protected Label lbNameLettersCount;
    @FXML protected Label lbDescriptLettersCount;

    protected int maxLettersCount_name = 150;
    protected int maxLettersCount_Desc = 400;

    protected TaskRepository taskRepository;

    protected Runnable isAddedTask;

    @FXML protected void initialize() {
        GuiUtility.setupCharacterLimit(tfName, lbNameLettersCount, maxLettersCount_name);
        GuiUtility.setupCharacterLimit(taDescription, lbDescriptLettersCount, maxLettersCount_Desc);
    }

    @FXML
    // pro levou složku u synch a pro zdrojovou u zálohy
    protected void findLeftPath(ActionEvent event) {
        File file = FileScannerService.findDirectory_Default(event);
        tfLeftPath.setText(file.getAbsolutePath());
    }

    @FXML
    // pro pravou složku u synch a pro cílovou u zálohy
    protected void findRightPath(ActionEvent event) {
        File file = FileScannerService.findDirectory_Default(event);
        tfRightPath.setText(file.getAbsolutePath());
    }


    @FXML
    protected void closeWindow_NoCreate(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }



    ///  ověření zda inputy sedí a můžeme ukládat
    ///  změní string na všechny Error hlášky + vrátí zda byla chyba
    protected String sharedInputCheck(
            String leftFileErrorMassage,
            String rightFileErrorMassage)
    {
        String errorMassage = "";
        File leftFile = new File(tfLeftPath.getText());
        File rightFile = new File(tfRightPath.getText());


        // kontrola všech polí
        if (tfName.getText().isEmpty() || tfName.getText().length() > maxLettersCount_name) {
            tfName.setStyle("-fx-border-color: -fx-color-mark2;");
            errorMassage = errorMassage + "Jméno je prázdné nebo obsahuje více než " + maxLettersCount_name +"zanků.\n";
        }
        if (!leftFile.exists()) {
            tfLeftPath.setStyle("-fx-border-color: -fx-color-mark2;");
            errorMassage = errorMassage + leftFileErrorMassage+ "\n";
        }
        if (!rightFile.exists()) {
            tfRightPath.setStyle("-fx-border-color: -fx-color-mark2;");
            errorMassage = errorMassage + rightFileErrorMassage + "\n";
        }

        if (taDescription.getText().length() > maxLettersCount_Desc) {
            tfName.setStyle("-fx-border-color: -fx-color-mark2;");
            errorMassage = errorMassage + "Popis obsahuje více než " + maxLettersCount_Desc + "zanků.\n";
        }

        return errorMassage;
    }

    public void setIsAddedTask(Runnable isAddedTask) {
        this.isAddedTask = isAddedTask;
    }

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

    }


}
