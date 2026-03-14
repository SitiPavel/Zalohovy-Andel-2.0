package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import java.time.LocalDate;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.MANUAL_SYNCHRONIZATION;

public class ManualSyncTask extends TaskModel{



    // prázdný konstruktor pro json
    public ManualSyncTask() {
        super();
    }

    public ManualSyncTask(String name, String leftPath,
                          String rightPath, String description) {
        super( MANUAL_SYNCHRONIZATION, name, leftPath, rightPath, description);
    }

    public ManualSyncTask(ManualSyncTask source) {
        super(source);
    }

    public void updateTaskModel(TaskModel task) {
        super.updateTaskModel(task);
    }

}
