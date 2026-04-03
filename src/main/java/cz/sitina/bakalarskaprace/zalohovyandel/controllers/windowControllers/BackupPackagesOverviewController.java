package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.components.BackupPackageToggleButtonController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.DataTransferControllers.DataRecoverySettingsController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class BackupPackagesOverviewController {
    @FXML
    private FlowPane fpToggleButtonContainer;

    private String absolutDatabasePath;
    private List<String> allPackages;
    private ToggleGroup toggleGroup;

    private String absolutRootFolder;
    private BackupTask originalBackupTask;
    private BackupTask editedBackupTask;
    private String selectedPackage;

    @FXML
    private void initialize() {
        fpToggleButtonContainer.setHgap(10);
        fpToggleButtonContainer.setVgap(10);
    }

    @FXML
    private void restoreDataFromAllPackages(ActionEvent event) {
        loadDialogWindowForRecovery("", true);
    }

    @FXML
    private void restoreDataFromOnePackage(ActionEvent event) {
        if (toggleGroup.getSelectedToggle() == null) {
            ErrorUtility.getInstance("Chyba výběru",
                    "Nebyl vybrán datový balíček",
                    "Z nabídky vyberte na balíček, který má být obnovit.");
            return;
        }

        selectedPackage = toggleGroup.getSelectedToggle().getUserData().toString();

        loadDialogWindowForRecovery(selectedPackage, false);
    }

    @FXML
    private void closeWindow(ActionEvent event) {

        WindowManager.findByActionAndClose(event);
    }

    public void setTasks(BackupTask originBackupTask, BackupTask editedTaskModel) {
        this.originalBackupTask = originBackupTask;
        this.editedBackupTask = editedTaskModel;
        this.absolutDatabasePath = originBackupTask.getBackupDatabasePath();
        this.absolutRootFolder = originBackupTask.getRootBackupPath();

        try (DatabaseManager databaseManager = new DatabaseManager(absolutDatabasePath,null)) {
            allPackages = databaseManager.getAllPackages();

        } catch (SQLException e) {
            e.printStackTrace();
            ErrorUtility.showError_databaseError("Soubor databáze je poškozený nebo k němu aplikace ztratila přístup.");
        }
        loadPackages();
    }

    private void loadPackages() {

        String guiName;
        String guiDate;
        String guiTime;

        toggleGroup = new ToggleGroup();
        String finalGuiName;



        for (String packageName : allPackages) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(WindowManager.getComponentPath() +
                        "BackupPackageToggleButton.fxml"));
                Node graphic = loader.load();
                BackupPackageToggleButtonController controller = loader.getController();

                String[] nameParts = packageName.split("_");

                if (nameParts[0].contains("FullBackup")) {
                    guiName = "Plná záloha " + nameParts[1];
                } else {
                    guiName = "Přírůstek " + nameParts[1];
                }

                String[] timeSeparatedFromEnding= nameParts[2].split("\\.");;
                guiDate = Utility.getDateOnly(timeSeparatedFromEnding[0]);
                guiTime = Utility.getTimeOnly(timeSeparatedFromEnding[0]);

                controller.loadContent(guiName,guiDate,guiTime);
                finalGuiName = (guiName + "   " + guiDate + "   " + guiTime);

                ToggleButton toggleCard = new ToggleButton();

                toggleCard.setUserData(packageName);


                toggleCard.setToggleGroup(toggleGroup);

                toggleCard.setPrefWidth(110);
                toggleCard.setPrefHeight(120);

                toggleCard.setGraphic(graphic);
                toggleCard.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                toggleCard.getStyleClass().add("toggleButton_Styl1_light");

                // finalGuiName1 pro správné definování, z finalGuiName to neche brát
                String finalGuiName1 = finalGuiName;

                toggleCard.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        // načtení okna s oobsahem vybraného package
                        showContentOfPackage(packageName, finalGuiName1);
                    }
                });


                fpToggleButtonContainer.getChildren().add(toggleCard);

            } catch (IOException e) {
                e.printStackTrace();
                ErrorUtility.showError_WindowLoading();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    private void showContentOfPackage(String packageName, String packageNameGUI) {
        try{
            FXMLLoader loader = WindowManager.loadFXML("insidePackageContentOverview.fxml");
            InsidePackageContentController controller = loader.getController();
            controller.loadContentToTable(absolutDatabasePath, packageName, packageNameGUI);

            WindowManager.openNewWindowAutoSized(loader, "Obsah balíčku: " + packageName);
        } catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.showError_WindowLoading();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadDialogWindowForRecovery(String packageName, boolean isFullRecovery) {
        try {
            FXMLLoader loader = WindowManager.loadFXML("DataRecaverySetWindow.fxml");
            DataRecoverySettingsController controller = loader.getController();
            controller.loadWindow(packageName, isFullRecovery, absolutRootFolder, absolutDatabasePath,selectedPackage);
            controller.setTasks(originalBackupTask,editedBackupTask);

            WindowManager.openNewWindowAutoSized(loader, "DataRecaverySetWindow");
        } catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.showError_WindowLoading();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
