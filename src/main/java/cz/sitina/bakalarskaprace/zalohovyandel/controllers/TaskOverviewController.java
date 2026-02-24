package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.AutoBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.AUTO_BACKUP;
import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.MANUAL_SYNCHRONIZATION;

public class TaskOverviewController {

    private TaskRepository taskRepository;
    private AppFrameController appFrameController;

//  načtení obrázků
    private String pathName = "/cz/sitina/bakalarskaprace/zalohovyandel/images/iconsSmall/";
    private final Image entryIcon =
            new Image(getClass().getResourceAsStream(pathName + "Entry.png"));
    private final Image autoBackupIcon =
            new Image(getClass().getResourceAsStream(pathName + "autoBackup_TableIcon_n_n.png"));
    private final Image autoBackupIcon_Com =
            new Image(getClass().getResourceAsStream(pathName + "autoBackup_TableIcon_n_K.png"));
    private final Image autoBackupIcon_Enc =
            new Image(getClass().getResourceAsStream(pathName + "autoBackup_TableIcon_S_n.png"));
    private final Image autoBackupIcon_EncCom =
            new Image(getClass().getResourceAsStream(pathName + "autoBackup_TableIcon_S_K.png"));
    private final Image manualSync_TabbleIcon =
            new Image(getClass().getResourceAsStream(pathName + "manualSync_TabbleIcon.png"));

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




    public void initialize() {
        int entrySize = 20;
        int typeSize = 30;


        // nastavení vzhledu buňky pro vstup do úlohy = přidání tpačítka s ikonou
        taskColEntry.setCellValueFactory(cellData -> new SimpleObjectProperty<>(entryIcon));
        taskColEntry.setCellFactory(cellData -> new TableCell<TaskModel, Image>() {

            private final Button btn = new Button();
            private final ImageView imageView = new ImageView(entryIcon);
            { // inicializační blok
                imageView.setFitWidth(entrySize);
                imageView.setFitHeight(entrySize);
                imageView.setPreserveRatio(true);

                btn.setGraphic(imageView);
                btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                btn.setOnAction(event -> {
                    TaskModel taskModel = getTableView().getItems().get(getIndex());
                    if (taskModel instanceof ManualSyncTask mst) {
                        appFrameController.showCompareOverview(mst);
                    } else {
                        System.out.println("Neznámý typ task");/*ERROR ✪ */
                    }
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
                iconToDisplay = manualSync_TabbleIcon;

            } else if (task instanceof AutoBackupTask autoBackupTask) {

                if (autoBackupTask.isCompressed() &&
                        autoBackupTask.isEncripted()) {
                    iconToDisplay = autoBackupIcon_EncCom;

                } else if (autoBackupTask.isCompressed() &&
                        !autoBackupTask.isEncripted()) {
                    iconToDisplay = autoBackupIcon_Com;

                } else if (autoBackupTask.isEncripted()) {
                    iconToDisplay = autoBackupIcon_Enc;
                } else {
                    iconToDisplay = autoBackupIcon;
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
            if (cellData.getValue() instanceof AutoBackupTask autoBackupTask) {
                return new SimpleStringProperty(autoBackupTask.repeatIntervalToString());
            } else {
                return new SimpleStringProperty("---");
            }
        });
        taskColCompression.setCellValueFactory(cellData ->{
            if (cellData.getValue() instanceof AutoBackupTask autoBackupTask) {
                if (autoBackupTask.isCompressed()) {
                    return new SimpleStringProperty("✓");
                } else {
                    return new SimpleStringProperty("✘");
                }
            } else {
                return new SimpleStringProperty("---");
            }
        });
        taskColEncryption.setCellValueFactory(cellData ->{
            if (cellData.getValue() instanceof AutoBackupTask autoBackupTask) {
                if (autoBackupTask.isEncripted()) {
                    return new SimpleStringProperty("✓");
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

    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;

        taskTable.setItems(this.taskRepository.getTaskList());
    }

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }
}
