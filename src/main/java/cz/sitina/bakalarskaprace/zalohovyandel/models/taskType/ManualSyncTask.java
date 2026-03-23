package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.MANUAL_SYNCHRONIZATION;

public class ManualSyncTask extends TaskModel{

    private List<String> rootFilteredFolders_Left;
    private List<String> rootFilteredFolders_Right;



    // prázdný konstruktor pro json
    public ManualSyncTask() {
        super();
    }

    public ManualSyncTask(String name, String leftPath,
                          String rightPath, String description) {
        super( MANUAL_SYNCHRONIZATION, name, leftPath, rightPath, description);
        this.rootFilteredFolders_Left = new ArrayList<>();
        this.rootFilteredFolders_Right = new ArrayList<>();

    }

    public ManualSyncTask(ManualSyncTask source) {
        super(source);
        this.rootFilteredFolders_Left = new ArrayList<>(source.rootFilteredFolders_Left);
        this.rootFilteredFolders_Right = new ArrayList<>(source.rootFilteredFolders_Right);
    }

    public void updateTaskModel(ManualSyncTask task) {
        super.updateTaskModel(task);
        this.rootFilteredFolders_Left = new ArrayList<>(task.getRootFilteredFolders_Left());
        this.rootFilteredFolders_Right = new ArrayList<>(task.getRootFilteredFolders_Right());
    }


    public List<String> getRootFilteredFolders_Left() {
        return rootFilteredFolders_Left;
    }

    public List<String> getRootFilteredFolders_Right() {
        return rootFilteredFolders_Right;
    }

    public void setLeftPath(String leftPath) {
        this.leftPath = leftPath;
    }
    public void setRightPath(String rightPath) {
        this.rightPath = rightPath;
    }
}
