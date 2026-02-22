package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import java.time.LocalDate;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.MANUAL_SYNCHRONIZATION;

public class ManualSyncTask extends TaskModel{

    // filtry
    boolean isVisibleEQUALS = true;
    boolean isVisibleNO_EQUALS = true;
    boolean isVisibleIGNORE = true;
    boolean isVisibleUNKONWN = true;
    boolean isVisibleDELETE_LEFT = true, isVisibleDELETE_RIGHT = true,   isVisibleDELETE_BOTH = true;
    boolean isVisibleUPDATE_LEFT = true, isVisibleUPDATE_RIGHT = true;
    boolean isVisibleADD_LEFT = true, isVisibleADD_RIGHT = true;



    public ManualSyncTask(String name, String leftPath,
                          String rightPath, String description) {
        super( MANUAL_SYNCHRONIZATION, name, leftPath, rightPath, description);
    }

}
