package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.BACKUP;

public class BackupTask extends TaskModel {
    private final boolean isEncrypted = false;
    private final boolean isCompressed = false;

    public BackupTask() {
        super();
    }

    public BackupTask( String name, String leftPath, String rightPath, String description) {
        super(BACKUP, name, leftPath, rightPath, description);
    }

    public BackupTask(BackupTask source) {
        super(source);

    }

    public void updateTask(BackupTask task) {
        super.updateTaskModel(task);

    }

    //getry
    public boolean isEncrypted() {
        return isEncrypted;
    }
    public boolean isCompressed() {
        return isCompressed;
    }

    public String isEncryptedToString() {
        if (isEncrypted) {
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
}
