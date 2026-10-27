package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices.EncryptionConfigFileManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class TaskEditController {

    private final JsonDataManagerService jsonService = JsonDataManagerService.getInstance();
    private final EncryptionConfigFileManager encryptConfigManager = new EncryptionConfigFileManager(jsonService.getMapper());

    @FXML private Label hlHeader;
    @FXML private TextField tfName;
    @FXML private Label lbNameLettersCount;

    @FXML private Label folderLabel;
    @FXML private HBox leftPathInputConteiner;
    @FXML private TextField tfLeftPath;
    @FXML private Button btnLeftPath;

    @FXML private HBox rightPathInputConteiner;
    @FXML private TextField tfRightPath;
    @FXML private Button btnRightPath;

    @FXML private TextArea taDescription;
    @FXML private Label lbDescriptLettersCount;

    @FXML private Button submitBtn;

    private final int maxLettersCount_name = 150;
    private final int maxLettersCount_Desc = 400;

    private TaskModel task;
    private Runnable onUpdateSuccess;

    @FXML
    private void initialize() {
        GuiUtility.setupCharacterLimit(tfName, lbNameLettersCount, maxLettersCount_name);
        GuiUtility.setupCharacterLimit(taDescription, lbDescriptLettersCount, maxLettersCount_Desc);
    }

    public void setTask(TaskModel task, Runnable onUpdateSuccess) {
        this.task = task;
        this.onUpdateSuccess = onUpdateSuccess;

        if (task == null) {
            return;
        }

        hlHeader.setText("Upravit úlohu \"" + task.getName() + "\"");
        tfName.setText(task.getName() != null ? task.getName() : "");
        tfLeftPath.setText(task.getLeftPath() != null ? task.getLeftPath() : "");
        tfRightPath.setText(task.getRightPath() != null ? task.getRightPath() : "");
        taDescription.setText(task.getDescription() != null ? task.getDescription() : "");

        submitBtn.setText("Upravit");

        // Větvení podle typu úlohy
        if (task instanceof BackupTask || task instanceof EncryptedBackupTask) {
            Tooltip backupTooltip = Utility.createToolTip_600ms("U zálohovací úlohy nelze dodatečně měnit zdrojovou ani cílovou složku.");
            folderLabel.setDisable(true);
            leftPathInputConteiner.setDisable(true);
            rightPathInputConteiner.setDisable(true);

            folderLabel.setTooltip(backupTooltip);
            tfLeftPath.setTooltip(backupTooltip);
            tfRightPath.setTooltip(backupTooltip);
            btnLeftPath.setTooltip(backupTooltip);
            btnRightPath.setTooltip(backupTooltip);
        } else {
            folderLabel.setDisable(false);
            leftPathInputConteiner.setDisable(false);
            rightPathInputConteiner.setDisable(false);
        }
    }

    @FXML
    private void findLeftPath(ActionEvent event) {
        File file = FileScannerService.findDirectory_Default(event);
        if (file != null) {
            tfLeftPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void findRightPath(ActionEvent event) {
        File file = FileScannerService.findDirectory_Default(event);
        if (file != null) {
            tfRightPath.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void closeWindow_Cancel(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }

    @FXML
    private void saveAndClose(ActionEvent event) {
        String newName = tfName.getText() != null ? tfName.getText().trim() : "";
        String newDesc = taDescription.getText() != null ? taDescription.getText().trim() : "";

        // Validace názvu
        if (newName.isEmpty() || newName.length() > maxLettersCount_name) {
            tfName.setStyle("-fx-border-color: -fx-color-mark2;");
            ErrorUtility.showError_form("Jméno úlohy je prázdné nebo obsahuje více než " + maxLettersCount_name + " znaků.");
            return;
        }

        // Validace popisu
        if (newDesc.length() > maxLettersCount_Desc) {
            taDescription.setStyle("-fx-border-color: -fx-color-mark2;");
            ErrorUtility.showError_form("Popis obsahuje více než " + maxLettersCount_Desc + " znaků.");
            return;
        }

        // Validace specifická pro ManualSyncTask (zde se upravují i cesty)
        if (task instanceof ManualSyncTask syncTask) {
            String newLeft = tfLeftPath.getText() != null ? tfLeftPath.getText().trim() : "";
            String newRight = tfRightPath.getText() != null ? tfRightPath.getText().trim() : "";

            File leftFile = new File(newLeft);
            File rightFile = new File(newRight);

            if (!leftFile.exists() || !leftFile.isDirectory()) {
                tfLeftPath.setStyle("-fx-border-color: -fx-color-mark2;");
                ErrorUtility.showError_form("Cesta k levé složce neexistuje!");
                return;
            }
            if (!rightFile.exists() || !rightFile.isDirectory()) {
                tfRightPath.setStyle("-fx-border-color: -fx-color-mark2;");
                ErrorUtility.showError_form("Cesta k pravé složce neexistuje!");
                return;
            }

            syncTask.setName(newName);
            syncTask.setDescription(newDesc);
            syncTask.setLeftPath(newLeft);
            syncTask.setRightPath(newRight);

        } else if (task instanceof EncryptedBackupTask encryptedTask) {
            // Kontrola dostupnosti cílové složky před aktualizací config.json
            String rootPathStr = encryptedTask.getRootFolderPathStr();
            if (rootPathStr == null || rootPathStr.isBlank() || !Files.exists(Path.of(rootPathStr))) {
                ErrorUtility.getInstance("Cílová složka nedostupná",
                        "Nelze aktualizovat šifrovanou úlohu.",
                        "Cílový adresář zálohy (" + rootPathStr + ") není dostupný (odpojený disk nebo smazaná složka). " +
                                "Změnu nelze uložit, protože je nutné aktualizovat konfigurační soubor v cíli.");
                return;
            }

            encryptedTask.setName(newName);
            encryptedTask.setDescription(newDesc);

            // Aktualizace config.json v cíli
            encryptConfigManager.makeConfigUpToDate(encryptedTask, Path.of(rootPathStr));

        } else if (task instanceof BackupTask backupTask) {
            backupTask.setName(newName);
            backupTask.setDescription(newDesc);
        } else {
            task.setName(newName);
            task.setDescription(newDesc);
        }

        // Uložení změn do JSONu a vytvoření bezpečné zálohy předchozího stavu
        jsonService.updateTask_saveOldSave(task);

        if (onUpdateSuccess != null) {
            onUpdateSuccess.run();
        }

        WindowManager.findByActionAndClose(event);
    }
}
