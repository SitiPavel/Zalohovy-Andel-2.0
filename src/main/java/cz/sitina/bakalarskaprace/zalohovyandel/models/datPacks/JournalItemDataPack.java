package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

public class JournalItemDataPack {

    private long id;
    private LocalDateTime dateTimeOfStart;
    private LocalTime processDuration;

    private CompareStrategy compareStrategy;
    private FinishProcesStatus finishProcesStatus;

    private int failCount;
    private int fileCount;

    private String leftRootPath;
    private String rightRootPath;


    private Set<String> failPathList;

    public JournalItemDataPack() {
    }

    public JournalItemDataPack(LocalDateTime DateTime,
                               LocalTime processDuration,
                               CompareStrategy compareStrategy,
                               FinishProcesStatus finishProcesStatus,
                               int fileCount,
                               int failCount,
                               Set<String> failPathList,
                               String leftRootPath,
                               String rightRootPath
    ) {
        this.id = System.currentTimeMillis();
        this.dateTimeOfStart = DateTime;
        this.processDuration = processDuration;

        this.compareStrategy = compareStrategy;
        this.finishProcesStatus = finishProcesStatus;

        this.failCount = failCount;
        this.fileCount = fileCount;

        this.leftRootPath =leftRootPath;
        this.rightRootPath = rightRootPath;

        this.failPathList = new LinkedHashSet<>(failPathList);

    }





    public long getId() {
        return id;
    }

    public LocalDateTime getDateTimeOfStart() {
        return dateTimeOfStart;
    }

    public LocalTime getProcessDuration() {
        return processDuration;
    }

    public CompareStrategy getCompareStrategy() {
        return compareStrategy;
    }

    public FinishProcesStatus getFinishProcesStatus() {
        return finishProcesStatus;
    }

    public int getFailCount() {
        return failCount;
    }

    public Set<String> getFailPathList() {
        return failPathList;
    }

    public int getFileCount() {
        return fileCount;
    }

    public String getLeftRootPath() {
        return leftRootPath;
    }

    public String getRightRootPath() {
        return rightRootPath;
    }
}
