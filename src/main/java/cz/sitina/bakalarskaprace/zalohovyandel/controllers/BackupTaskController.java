package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

public class BackupTaskController implements TaskController {

    // třídy poskytující služby
    IconManagerUtility iconManager = new IconManagerUtility();

    // objekty
    private TaskRepository taskRepository;
    private BackupTask originBackupTask;
    private BackupTask editableBackupTask;
    private AppFrameController appFrameController;

    private BooleanProperty isDirtyFlag = new SimpleBooleanProperty(false);


    /// rostě info o root složkách
    @FXML private Label lbSourceFolderPath;
    @FXML private Label lbLeftUsedSpace;
    @FXML private Label lbLeftTotalSpace;
    @FXML private Label lbLeftFreeSpace;
    @FXML private ProgressBar pbLeftUsedSpace;


    @FXML private Label lbDestinationFolderPath;
    @FXML private Label lbRightUsedSpace;
    @FXML private Label lbRightTotalSpace;
    @FXML private Label lbRightFreeSpace;
    @FXML private ProgressBar pbRightUsedSpace;


    /// definocvané GUI objekty
    @FXML private Button btnCompareFolders;
    @FXML private Button btnSaveTask;


    /// definování ukazatelu a filtrování
    @FXML private Label lbIgnoreCount;
    @FXML private Label lbEqualsCount;
    @FXML private Label lbUnknownCount;
    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    // nutné deklarování pro ovládání rychlích filtrů

    @FXML private Button btnAddItem_right;
    @FXML private Button btnModified_right;
    @FXML private Button btnEqual;
    @FXML private Button btnBlock;
    @FXML private Button btnUnknown;


    @FXML private ImageView imgAddItem_right;
    @FXML private ImageView imgModified_right;
    @FXML private ImageView imgEqual;
    @FXML private ImageView imgBlock;
    @FXML private ImageView imgUnknown;


    // objekty v na levé straně symbolizující cíl

    @FXML private ImageView imgTypeOfBackup;
    @FXML private ImageView imgPieChar_Right;
    @FXML private Label lbPieChar_Right;
    @FXML private Label lbDestinationFolderPathContent;




    // nastaví podle true/false obrázek odpovídajcí vypnutému nebo zapnutému filtru
    private void setFastFiltersImg() {
        GuiOperationFilters_TaskData taskFilters = editableBackupTask.getGuiOperationFilters();
        imgAddItem_right.setImage(
                taskFilters.isActive_ADD_RIGHT() ? iconManager.getIcon_addItem_right() : iconManager.getIcon_addItem_right_OFF()
        );
        imgModified_right.setImage(
                taskFilters.isActive_UPDATE_RIGHT() ? iconManager.getIcon_modified_right() : iconManager.getIcon_modified_right_OFF()
        );
        imgEqual.setImage(
                taskFilters.isActive_EQUALS() ? iconManager.getIcon_equal() : iconManager.getIcon_equal_OFF()
        );
        imgBlock.setImage(
                taskFilters.isActive_IGNORE() ? iconManager.getIcon_block() : iconManager.getIcon_block_OFF()
        );
        imgUnknown.setImage(
                taskFilters.isActive_UNKNOWN() ? iconManager.getIcon_unknown() : iconManager.getIcon_unknown_OFF()
        );


    }



    public boolean isUnsavedChanges() {
        return isDirtyFlag.get();
    }

    public void saveChanges() {
        this.originBackupTask.updateTaskModel(editableBackupTask);
        isDirtyFlag.set(false);
    }

    // setry

    public void loadView() {
        lbSourceFolderPath.setText(originBackupTask.getLeftPath());
        lbDestinationFolderPath.setText(originBackupTask.getRightPath());
        lbDestinationFolderPathContent.setText(originBackupTask.getLeftPath());
    }


    public void setTaskModel(TaskModel task) {
        BackupTask backupTask = (BackupTask) task;

        this.originBackupTask = backupTask;
        this.editableBackupTask = new BackupTask(backupTask);

        setFastFiltersImg();
    }


    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }

}
