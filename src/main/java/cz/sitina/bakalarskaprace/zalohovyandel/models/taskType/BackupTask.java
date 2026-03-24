package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.BACKUP;

public class BackupTask extends TaskModel {
    private boolean isEncrypted = false;
    private boolean isCompressed = false;

    public BackupTask() {
        super();
    }

    public BackupTask( String name, String leftPath, String rightPath, String description) {
        super(BACKUP, name, leftPath, rightPath, description, CompareStrategy.BACKUP_TO_RIGHT);
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

    // setry

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public void setCompressed(boolean compressed) {
        isCompressed = compressed;
    }
}
