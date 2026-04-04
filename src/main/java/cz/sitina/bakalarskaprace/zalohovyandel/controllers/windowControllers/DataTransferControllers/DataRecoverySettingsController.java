package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.DataTransferControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.GuiUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class DataRecoverySettingsController {


    @FXML private Label headerPackageName;

    @FXML private TextField tfNamePath;
    @FXML private Label lbNameLettersCount;
    @FXML private TextField tfDestinationPath;

    private String backupRootFolder;
    private String databasePath;
    private String packageName;
    private boolean isFullRecovery;
    private BackupTask originBackupTask;
    private BackupTask editedBackupTask;

    private Path userSelectedPath;
    private String selectedPackage;

    private SecretKey secretKey;


    private int maxLettersCount = 150;

    @FXML private void initialize() {

    }

    @FXML
    private void closeWindow(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }

    @FXML
    private void findDestinationPath(ActionEvent event) {
        File destinationPath = FileScannerService.findDirectory_Default(event);
        tfDestinationPath.setText(destinationPath.getAbsolutePath());
    }

    @FXML
    private void StartRecovery(ActionEvent event) {
        String errorMassage = checkRecoveryForm();
        if (errorMassage == null || !errorMassage.isEmpty()) {
            ErrorUtility.showError_form(errorMassage);
            return;
        }

        if (backupRootFolder == null || backupRootFolder.isEmpty() ||
                databasePath == null || databasePath.isEmpty()) {
            ErrorUtility.getInstance("Kritická chyba",
                    "Chyba cest k souborům.",
                    "Neplatná cesta ke složce s historií zálohy nebo k databázi.");
            return;
        }

        Path fullRestoreTarget = Paths.get(backupRootFolder, packageName);

        Stage currentStage = (Stage) tfNamePath.getScene().getWindow();
        currentStage.close();

        List<BackupDatabaseDTO> foundDtoItems = findListOfFilesToRestore();

        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/cz/sitina/bakalarskaprace/zalohovyandel/fxml/windows/copyProcessWindow.fxml"));
            RecoveryCopyProcessController controller = new RecoveryCopyProcessController();
            loader.setController(controller);
            loader.load(); // Toto zavolá metodu inicialize a nastaví controler podle FXML
            controller.setFullRestoreTarget(fullRestoreTarget.toString());
            controller.executeProcess(originBackupTask, editedBackupTask, secretKey,
                    foundDtoItems, userSelectedPath, selectedPackage);



            String title =  "Obnova souborů";
            Stage copyProcessStage = WindowManager.openRunningWindow(loader, title, 750, 400);

            copyProcessStage.setOnCloseRequest(e -> {
                controller.cancelAction();
                secretKey = null;
            });


        } catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.showError_WindowLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    // načte data v GUI do oknteinerů, lablu a dalších
    public void loadWindow(String packageName, boolean isFullRecovery, String backupRootFolder, String databasePath,
                           String selectedPackage) {
        if (!isFullRecovery) {
            headerPackageName.setText(getGuiPackageName(packageName));
        }
        this.backupRootFolder = backupRootFolder;
        this.databasePath = databasePath;
        this.packageName = packageName;
        this.isFullRecovery = isFullRecovery;
        this.selectedPackage = selectedPackage;

        GuiUtility.setupCharacterLimit(tfNamePath,lbNameLettersCount,maxLettersCount);


    }

    // podle tlačítka které bylo zmáčknuto se vyhledají objekty z DB
    private List<BackupDatabaseDTO> findListOfFilesToRestore() {
        List<BackupDatabaseDTO> foundDtoItems = new ArrayList<>();

        try (DatabaseManager databaseManager = new DatabaseManager(databasePath, null)) {
            if (isFullRecovery) {
                foundDtoItems = databaseManager.getAllDataWithLatestVersion();
            } else {
                Path packageFolder = Paths.get(backupRootFolder, packageName);
                if (Files.exists(packageFolder)) {
                    foundDtoItems = databaseManager.getItemsFromSpecificPackage(packageName);
                } else {
                    ErrorUtility.showError_databaseError("Datový balíček nebyl nalezen.");
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ErrorUtility.showError_databaseError("Soubor databáze je poškozený nebo k němu aplikace ztratila přístup.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foundDtoItems;
    }

    // skontrolují se vstupy ve formuláři
    private String checkRecoveryForm() {
        String errorMassage = "";
        File destinationFile = new File(tfDestinationPath.getText());
        if (tfNamePath.getText().isEmpty() || tfNamePath.getText().length() > maxLettersCount) {
            tfNamePath.setStyle("-fx-border-color: -fx-color-mark2;");
            errorMassage = errorMassage + "Jméno je prázdné nebo obsahuje více než " + maxLettersCount +" zanků. \n";
        } else {
            userSelectedPath = Path.of(tfDestinationPath.getText(), tfNamePath.getText());
            if (userSelectedPath.toFile().exists()) {
                tfNamePath.setStyle("-fx-border-color: -fx-color-mark2;");
                errorMassage = errorMassage + "Ve vybrané složce již existuje složka se stejným jménem, zvolte jiné " +
                        "jméno složky! \n";
            }
        }

        if (!destinationFile.exists()) {
            tfDestinationPath.setStyle("-fx-border-color: -fx-color-mark2;");
            errorMassage = errorMassage + "Cílová složka nebyla nalezena, zadejte existující složku. \n";
        }

        // odstranení mezer ze začátku a konce
        String folderName = tfNamePath.getText().trim();
        tfNamePath.setText(folderName);
        String folderPath = tfDestinationPath.getText().trim();
        tfDestinationPath.setText(folderPath);



        return errorMassage;
    }


    // získání názvu pro zobrazení balíčku v uživatelském rozhraní
    private String getGuiPackageName( String packageName) {
        String[] nameParts = packageName.split("_");

        String guiName;
        String guiDate;
        String guiTime;

        if (nameParts[0].contains("FullBackup")) {
            guiName = "Plná záloha " + nameParts[1];
        } else {
            guiName = "Přírůstek " + nameParts[1];
        }

        guiDate = Utility.getDateOnly(nameParts[2]);
        guiTime = Utility.getTimeOnly(nameParts[2]);

        return  (guiName + "   " + guiDate + "   " + guiTime);
    }

    public void setTasks(BackupTask originBackupTask, BackupTask editedTaskModel, SecretKey secretKey) {
        this.originBackupTask = originBackupTask;
        this.editedBackupTask = editedTaskModel;
        this.secretKey = secretKey;
    }


}
