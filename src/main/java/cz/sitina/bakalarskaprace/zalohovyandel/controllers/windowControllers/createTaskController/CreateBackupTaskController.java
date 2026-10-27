package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices.CryptoManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices.EncryptionConfigFileManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

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

        EncryptedBackupTask encryptedTask = null;
        SecretKey secretKey = null;
        char[] passwordChars = null;

        // pokud je šifrován tak vytvořit Reprezentanta zašifrovaného tasku
        if (chbxIsEncrypt.isSelected()) {
            try {
                passwordChars = pwfPassword1.getText().toCharArray();
                encryptedTask = new EncryptedBackupTask(newBackupTask, pwfPassword1.getText());
                secretKey = CryptoManager.deriveKey(passwordChars, encryptedTask.getCryptoSalt());
            } catch (Exception e) {
                e.printStackTrace();
                ErrorUtility.getInstance("Chyba vytváření objektu",
                        "Při tvorbě nové úlohy se vyskytla chyba.",
                        "Nastala chyba uvnitř aplikace při šifrování, prosím zkuste to znovu.");
                if (passwordChars != null) {
                    CryptoManager.clearPassword(passwordChars);
                }
                return;
            }
        }

        // PŘIDÁNO: Inicializace počátečních souborů zálohy a databáze (včetně rollbacku při chybě)
        try {
            backupInitialization(newBackupTask, encryptedTask, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Chyba inicializace úlohy",
                    "Při vytváření struktury úlohy na disku se vyskytla chyba.",
                    "Nepodařilo se vytvořit počáteční soubory zálohy: " + e.getMessage());
            return;
        } finally {
            if (passwordChars != null) {
                CryptoManager.clearPassword(passwordChars);
            }
            secretKey = null;
        }

        if (chbxIsEncrypt.isSelected()) {
            jsonService.saveNewTask_saveOldSave(encryptedTask);
            taskRepository.add(encryptedTask);
        } else {
            jsonService.saveNewTask_saveOldSave(newBackupTask);
            taskRepository.add(newBackupTask);
        }

        isAddedTask.run();
        WindowManager.findByActionAndClose(event);
    }

    /**
     * Inicializace počátečních souborů zálohy (cílový adresář, SQLite databáze index.db a config.json).
     * V případě selhání provede rollback smazáním nově vytvořeného adresáře.
     */
    private void backupInitialization(BackupTask task, EncryptedBackupTask encryptedTask, SecretKey secretKey) throws Exception {
        Path rootDir = Path.of(task.getRootBackupPath());
        boolean folderCreatedByUs = false;

        try {
            // 1. Krok: Vytvoření kořenové složky zálohy
            if (!Files.exists(rootDir)) {
                Files.createDirectories(rootDir);
                folderCreatedByUs = true;
            }

            // 2. Krok: Inicializace a případné zašifrování SQLite databáze index.db
            try (DatabaseManager dbManager = new DatabaseManager(task.getBackupDatabasePath(), secretKey)) {
                // DatabaseManager automaticky zajistí schéma tabulky backup_catalog
            }

            // 3. Krok: Uložení config.json pro šifrovanou zálohu
            if (encryptedTask != null) {
                EncryptionConfigFileManager configManager = new EncryptionConfigFileManager(jsonService.getMapper());
                configManager.makeConfigUpToDate(encryptedTask, rootDir);
            }
        } catch (Exception e) {
            // ROLLBACK: Pokud došlo k chybě a složku jsme nově vytvořili, uklidíme ji
            if (folderCreatedByUs && Files.exists(rootDir)) {
                try {
                    Files.walk(rootDir)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (Exception ex) {
                    System.err.println("Chyba při provádění rollbacku složky: " + ex.getMessage());
                }
            }
            throw e;
        }
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
