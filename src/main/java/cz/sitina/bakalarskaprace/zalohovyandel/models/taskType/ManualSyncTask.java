package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import java.time.LocalDate;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.MANUAL_SYNCHRONIZATION;

public class ManualSyncTask extends TaskModel{




    public ManualSyncTask(String name, String leftPath,
                          String rightPath, String description) {
        super( MANUAL_SYNCHRONIZATION, name, leftPath, rightPath, description);
    }

}
