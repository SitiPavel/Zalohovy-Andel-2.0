package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.AppFrameController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices.CryptoManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;

import javax.crypto.SecretKey;


public class EntryPasswordController {

    @FXML private Label lbNameOfBackup;
    @FXML private PasswordField pfPasswordInput;
    @FXML private Label lbIncorrectPassword;

    //servis
    private JsonDataManagerService jsonManager = JsonDataManagerService.getInstance();
    private AppFrameController appFrameController;

    // setter hodnoty
    private EncryptedBackupTask encryptedBackupTask;

    @FXML
    public void confirmPassword(ActionEvent event) {
        try {
            if (encryptedBackupTask.verifyPassword(pfPasswordInput.getText())) {
                SecretKey key = CryptoManager.deriveKey(pfPasswordInput.getText().toCharArray() ,
                                encryptedBackupTask.getCryptoSalt());
                BackupTask unlockedTask = encryptedBackupTask.unlockTask(pfPasswordInput.getText(),
                        jsonManager.getMapper());
                WindowManager.findByActionAndClose(event);
                appFrameController.showBackupOverview_Encrypt(unlockedTask, key);
            } else {
                pfPasswordInput.setStyle("-fx-border-color: -fx-color-mark2;");
                lbIncorrectPassword.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Chyba ověření",
                    "Nastala nečekaná chyba při ověření hesla.",
                    "Systém narazil na interní problém při zpracování zabezpečení. Zkuste to prosím znovu.");
            WindowManager.findByActionAndClose(event);
        }

    }
    @FXML
    public void closeWindow(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }

    public void loadWindow(EncryptedBackupTask encryptedBackupTask, AppFrameController appFrameController) {
        this.encryptedBackupTask = encryptedBackupTask;
        this.appFrameController = appFrameController;

        lbNameOfBackup.setText(encryptedBackupTask.getName());
    }
}
