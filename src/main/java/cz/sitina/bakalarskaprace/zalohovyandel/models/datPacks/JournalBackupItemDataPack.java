package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class JournalBackupItemDataPack extends JournalItemDataPack {

    private boolean isDataRecovery;

    public JournalBackupItemDataPack() {
        super();
    }

    public JournalBackupItemDataPack(LocalDateTime DateTime,
                               LocalTime processDuration,
                               CompareStrategy compareStrategy,
                               boolean isDataRecovery,
                               FinishProcesStatus finishProcesStatus,
                               int fileCount,
                               int failCount,
                               Set<String> failPathList,
                               String leftRootPath,
                               String rightRootPath
    ) {
        super( DateTime, processDuration, compareStrategy,
         finishProcesStatus, fileCount,
         failCount, failPathList, leftRootPath, rightRootPath);

        this.isDataRecovery = isDataRecovery;
    }

    public boolean isDataRecovery() {
        return isDataRecovery;
    }

    public void setDataRecovery(boolean dataRecovery) {
        isDataRecovery = dataRecovery;
    }
}
