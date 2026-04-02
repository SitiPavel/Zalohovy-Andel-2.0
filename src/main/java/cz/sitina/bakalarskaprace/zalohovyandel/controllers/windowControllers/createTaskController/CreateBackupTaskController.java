package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

public class CreateBackupTaskController extends CreateTaskController {

    @FXML private CheckBox chbxIsCompress;
    @FXML private CheckBox chbxIsEncrypt;

    @FXML private PasswordField pwfPassword1;
    @FXML private PasswordField pwfPassword2;

    @FXML private VBox vbPasswordContainer;

    @FXML
    protected void initialize() {
        super.initialize();
        vbPasswordContainer.setDisable(true);
    }


    @FXML
    public void closeWindow_Create(ActionEvent event)  {


        String errorMassage = backupInputCheck(
                "Cesta ke zdrojové složce neexistuje!",
                "Cesta k cílové složce neexistuje!");


        if (!errorMassage.isEmpty()) {
            ErrorUtility.showError_form(errorMassage);
            return;
        }

        BackupTask newBackupTask = new BackupTask(
                tfName.getText(),
                tfLeftPath.getText(),
                tfRightPath.getText(),
                taDescription.getText(),
                chbxIsCompress.isSelected(),
                chbxIsEncrypt.isSelected()
        );
        // pokud je šifrován tak vytvořit Reprezentanta zašifrovaného tasku
         if (chbxIsEncrypt.isSelected()) {
             try {
                 EncryptedBackupTask encryptedTask = new EncryptedBackupTask(newBackupTask, pwfPassword1.getText());
                 jsonService.saveNewTask_saveOldSave(encryptedTask);
                 taskRepository.add(encryptedTask);
             } catch (Exception e) {
                 e.printStackTrace();
                 ErrorUtility.getInstance("Chyba vytváření objektu",
                         "Pří tvorbě nové úlohy se vyskytla chyba. ",
                         "Nastala chyba uvnitř aplikace, prosím zkuste to znovu.");
                 return;
             }
         } else {
             jsonService.saveNewTask_saveOldSave(newBackupTask);
             taskRepository.add(newBackupTask);
         }


        isAddedTask.run();
        WindowManager.findByActionAndClose(event);
    }

    @FXML
    private void turnOnOffEncryption(ActionEvent event) {
        vbPasswordContainer.setDisable(!chbxIsEncrypt.isSelected());
    }


    protected String backupInputCheck(
            String leftFileErrorMassage,
            String rightFileErrorMassage)
    {
        String errorMassage = super.sharedInputCheck(leftFileErrorMassage,rightFileErrorMassage);

        if (chbxIsEncrypt.isSelected()) {
            String passwordError = checkPassword(pwfPassword1.getText(), pwfPassword2.getText());
            if (!passwordError.isEmpty()) {
                errorMassage = (errorMassage + passwordError);
                pwfPassword1.setStyle("-fx-border-color: -fx-color-mark2;");
                pwfPassword2.setStyle("-fx-border-color: -fx-color-mark2;");
            }
        }

        return errorMassage;

    }

    // zkontroluje password
    private String checkPassword(String password, String passwordConfirm) {
        int MIN_LENGTH = 8;

        if (password == null || passwordConfirm == null || password.isEmpty()) {
            return "Heslo nemůže být prázdné.\n";
        }
        // 1. BLOK: Shoda hesel (Základní uživatelská kontrola)
        if (!password.equals(passwordConfirm)) {
            return "Zadaná hesla se neshodují.\n";
        }

        // 2. BLOK: Minimální požadavky na bezpečnost (Early Return)
        if (password.length() < MIN_LENGTH ||
                !password.matches(".*[A-Z].*") ||   // Přítomnost velkého písmene
                !password.matches(".*[a-z].*") ||   // Přítomnost malého písmene
                !password.matches(".*[0-9].*"))     // Přítomnost číslice
        {
            return "Heslo musí obsahovat nějaké malé a velké písmeno m/M, minimálně " +
                    MIN_LENGTH + " znaků a nějakou číslici 0…9. Doporučujeme použít i znaky například: !@#$%\n";
        }

        // 3. BLOK: Vše v pořádku
        return "";
    }
}
