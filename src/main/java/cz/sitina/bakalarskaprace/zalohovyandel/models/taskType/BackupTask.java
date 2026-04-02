package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;

import java.util.ArrayList;
import java.util.List;

import static cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes.BACKUP;

public class BackupTask extends TaskModel {
    private boolean isEncrypted = false;
    private boolean isCompressed = false;

    private List<String> rootFilteredFolders_Source;
    private String rootBackupPath;
    private String backupDatabasePath;

    public BackupTask() {
        super();
    }

    public BackupTask( String name, String leftPath, String rightPath, String description,
                       boolean isCompressed, boolean isEncrypted) {
        super(BACKUP, name, leftPath, rightPath, description, CompareStrategy.BACKUP_TO_RIGHT);

        this.isCompressed = isCompressed;
        this.isEncrypted = isEncrypted;

        rootFilteredFolders_Source = new ArrayList<>();
        rootBackupPath = rightPath +"\\Backup_" + name  +"_"+ id ;
        backupDatabasePath = rootBackupPath +"\\index.db" ;
    }

    public BackupTask(BackupTask source) {
        super(source);
        this.rootFilteredFolders_Source = new ArrayList<>(source.rootFilteredFolders_Source);
        this.rootBackupPath = source.rootBackupPath;
        this.backupDatabasePath = source.backupDatabasePath;
    }



    public void updateBackupTask(BackupTask task) {
        super.updateTaskModel(task);
        this.rootFilteredFolders_Source = new ArrayList<>(task.rootFilteredFolders_Source);
        this.rootBackupPath = task.rootBackupPath;
        this.backupDatabasePath = task.backupDatabasePath;
    }



    //getry
    public List<String> getRootFilteredFolders_Source() {
        return rootFilteredFolders_Source;
    }

    public String getRootBackupPath() {
        return rootBackupPath;
    }

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

    public String getBackupDatabasePath() {
        return backupDatabasePath;
    }
}
