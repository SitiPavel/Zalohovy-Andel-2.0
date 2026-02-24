package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;
import javafx.concurrent.Task;

public class AutoBackupTask extends TaskModel {
    private boolean isEncripted = false;
    private boolean isCompressed = false;
    private String repeatInterval = "";


    public AutoBackupTask(TaskTypes taskType, String name, String leftPath, String rightPath, String description) {
        super(taskType, name, leftPath, rightPath, description);
    }

    //getry
    public boolean isEncripted() {
        return isEncripted;
    }
    public boolean isCompressed() {
        return isCompressed;
    }

    public String isEncriptedToString() {
        if (isEncripted) {
            return "True";
        }
        return "False";
    }
    public String isCompressedToString() {
        if (isCompressed) {
            return "True";
        }
        return "False";
    }
    public String repeatIntervalToString() {
        return "Interval";
    }
}
