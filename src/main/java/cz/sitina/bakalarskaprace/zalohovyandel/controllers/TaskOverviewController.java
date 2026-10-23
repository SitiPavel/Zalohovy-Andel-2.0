package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.EntryPasswordController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController.CreateBackupTaskController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController.CreateManualSyncController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.createTaskController.CreateTaskController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.RightClickMenuItem;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice.RightClickMenuService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.Optional;

import static cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager.loadFXML;

public class TaskOverviewController {

    private TaskRepository taskRepository;
    private AppFrameController appFrameController;
    private final IconManagerUtility iconManager = new IconManagerUtility();
    private JsonDataManagerService jsonManager = JsonDataManagerService.getInstance();
    private final RightClickMenuService rightClickMenuService = new RightClickMenuService();


    //Definování tabulky a sloupců
    @FXML private TableView<TaskModel> taskTable;
    @FXML private TableColumn<TaskModel, Image> taskColEntry; // tlačítko pro vstup do nastavené úlohy
    @FXML private TableColumn<TaskModel, String> taskColName;
    @FXML private TableColumn<TaskModel, Image> taskColType;
    @FXML private TableColumn<TaskModel, String> taskColStatus;
    @FXML private TableColumn<TaskModel, String> taskColRepeatIntervals;
    @FXML private TableColumn<TaskModel, String> taskColCompression;
    @FXML private TableColumn<TaskModel, String> taskColEncryption;
    @FXML private TableColumn<TaskModel, String> taskColCreatedDate;
    @FXML private TableColumn<TaskModel, String> taskColDescription;

    @FXML private Button btnManualSync;
    @FXML private Button btnBackup;




    public void initialize() {
        int entrySize = 25;
        int typeSize = 35;

        Label tablePlaceholder = new Label("Nejsou vytvořené úlohy!");
        tablePlaceholder.getStyleClass().add("DarkBoltHeader1");
        taskTable.setPlaceholder(tablePlaceholder);

        btnManualSync.setTooltip(Utility.createToolTip_600ms("Vytvořit novou manuální synchronizaci."));
        btnBackup.setTooltip(Utility.createToolTip_600ms("Vytvořit novou zálohovací úlohu."));


        // nastavení řádků
        taskTable.setRowFactory(tableView -> {
            final TableRow<TaskModel> row = new TableRow<>();

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    TaskModel selectedTask = row.getItem();
                    showTaskContextMenu(selectedTask, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });

            return row;
        });


        // nastavení vzhledu buňky pro vstup do úlohy = přidání tpačítka s ikonou
        taskColEntry.setCellValueFactory(cellData -> new SimpleObjectProperty<>(iconManager.getEntryIcon()));
        taskColEntry.setCellFactory(cellData -> new TableCell<TaskModel, Image>() {

            private final Button btn = new Button();
            private final ImageView imageView = new ImageView(iconManager.getEntryIcon());
            { // inicializační blok
                imageView.setFitWidth(entrySize);
                imageView.setFitHeight(entrySize);
                imageView.setPreserveRatio(true);

                btn.setGraphic(imageView);
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                btn.setOnAction(event -> {
                    TaskModel taskModel = getTableView().getItems().get(getIndex());
                    openTask(taskModel);
                });
            }

            // Metoda, která se stará o vykreslení buňky (zde už musí být Image item)
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    setGraphic(btn);
                }
            }

        });

        taskColName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        // nastavení ikony pro
        taskColType.setCellValueFactory(cellData -> {
            TaskModel task = cellData.getValue();
            Image iconToDisplay = null;

            if (task == null) {
                return new SimpleObjectProperty<>(null);
            }
            // nastavení typu ikony
            else if (task instanceof ManualSyncTask manualSyncTask) {
                iconToDisplay = iconManager.getManualSync_TabbleIcon();

            } else if (task instanceof BackupTask backupTask) {
                if (backupTask.isCompressed()) {
                    iconToDisplay = iconManager.getBackupTask_TableIcon_Com_X();
                } else {
                    iconToDisplay = iconManager.getBackup_TableIcon();
                }
            } else if (task instanceof EncryptedBackupTask encryptedBackupTask) {
                if (encryptedBackupTask.isCompressed()) {
                    iconToDisplay = iconManager.getBackupTask_TableIcon_Com_Enc();
                } else {
                    iconToDisplay = iconManager.getBackupTask_TableIcon_X_Enc();
                }
            }
            return new SimpleObjectProperty<>(iconToDisplay);
        });
        taskColType.setCellFactory(cellData -> new TableCell<TaskModel, Image>() {
            private final ImageView imageView = new ImageView();

            // Inicializační blok nastavuje POUZE grafické vlastnosti nezávislé na datech
            {
                imageView.setFitWidth(typeSize);
                imageView.setFitHeight(typeSize);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null); // Prázdný řádek = žádná ikona
                } else {
                    imageView.setImage(item); // Vložíme obrázek, který jsme vybrali v kroku 1
                    setGraphic(imageView);
                }
            }
        });

        taskColStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
        taskColRepeatIntervals.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof BackupTask backupTask) {
                return new SimpleStringProperty(/*backupTask.repeatIntervalToString()*/);
            } else {
                return new SimpleStringProperty("---");
            }
        });
        taskColCompression.setCellValueFactory(cellData ->{
            if (cellData.getValue() instanceof BackupTask backupTask) {
                if (backupTask.isCompressed()) {
                    return new SimpleStringProperty("✔");
                } else {
                    return new SimpleStringProperty("✘");
                }
            } else if (cellData.getValue() instanceof EncryptedBackupTask encryptedBackupTask) {
                if (encryptedBackupTask.isCompressed()) {
                    return new SimpleStringProperty("✔");
                } else {
                    return new SimpleStringProperty("✘");
                }
            }
            else {
                return new SimpleStringProperty("---");
            }
        });
        taskColEncryption.setCellValueFactory(cellData ->{
            if (cellData.getValue() instanceof BackupTask backupTask) {
                if (backupTask.isEncrypted()) {
                    return new SimpleStringProperty("✔");
                } else {
                    return new SimpleStringProperty("✘");
                }
            }else if (cellData.getValue() instanceof EncryptedBackupTask encryptedBackupTask) {
                if (encryptedBackupTask.isEncrypted()) {
                    return new SimpleStringProperty("✔");
                } else {
                    return new SimpleStringProperty("✘");
                }
            } else {
                return new SimpleStringProperty("---");
            }
        });
        taskColCreatedDate.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCreationDate().toString()));
        taskColDescription.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));
    }


    /**===========================
     *        TLAČÍTKA
     * ==========================*/
    @FXML
    public void createManualSync(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = loadFXML("TaskManualSyncCreateWindow.fxml");
            CreateManualSyncController controller = fxmlLoader.getController();
            controller.setTaskRepository(taskRepository);
            controller.setIsAddedTask(() -> taskTable.refresh());

            WindowManager.openNewWindowAutoSized(fxmlLoader, "Nová manuální synchronizace");
        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void createBackup(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = loadFXML("TaskBackupCreateWindow.fxml");
            CreateBackupTaskController controller = fxmlLoader.getController();
            controller.setTaskRepository(taskRepository);
            controller.setIsAddedTask(() -> taskTable.refresh());

            WindowManager.openNewWindowAutoSized(fxmlLoader, "Nová záloha");
        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading();
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void openTask(TaskModel taskModel) {
        if (taskModel == null) return;

        if (taskModel instanceof ManualSyncTask mst) {
            appFrameController.showCompareOverview(mst);
        } else if (taskModel instanceof BackupTask bt) {
            appFrameController.showBackupOverview(bt);
        } else if (taskModel instanceof EncryptedBackupTask ebt) {
            try {
                FXMLLoader loader = WindowManager.loadFXML("EntryPasswordWindow.fxml");
                EntryPasswordController controller = loader.getController();
                controller.loadWindow(ebt, appFrameController);
                WindowManager.openNewWindowAutoSized(loader, "Přehled zálohovací úlohy");
            } catch (IOException e) {
                ErrorUtility.showError_WindowLoading();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ErrorUtility.getInstance("Kritická chyba",
                    "Neznámý typ úlohy",
                    "Aplikace nezná vybraný typ úlohy!");
        }
    }

    private void showTaskContextMenu(TaskModel task, double screenX, double screenY) {
        java.util.List<RightClickMenuItem> items = new java.util.ArrayList<>();

        // 1. Vstoupit do úlohy
        items.add(new RightClickMenuItem("Vstoupit do úlohy", iconManager.getEntryIcon(), () -> {
            openTask(task);
        }));

        // 2. Upravit úlohu (příprava, zatím bez těla akce)
        items.add(new RightClickMenuItem("Upravit", iconManager.getIcon_modified_left(), () -> {
            System.out.println("Úprava úlohy: " + task.getName());
        }));

        // 3. Smazat úlohu
        items.add(new RightClickMenuItem("Smazat úlohu", iconManager.getIconDeleteBoth(), () -> {
            deleteTask(task);
        }));

        rightClickMenuService.showRightClickMenu(items, screenX, screenY);
    }

    private void deleteTask(TaskModel task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrzení smazání");
        alert.setHeaderText("Chcete úlohu \"" + task.getName() + "\" trvale smazat?");
        alert.setContentText("Tato akce je nevratná a data budou trvale smazána.");

        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            jsonManager.deleteTask_SaveOldSave(task);
            taskTable.getItems().remove(task);
            System.out.println("Úloha byla úspěšně smazána.");
        }
    }

    ///  setry
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

        taskTable.setItems(this.taskRepository.getTaskObservableList());
    }

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }

}

