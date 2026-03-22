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
    private LocalTime procesDuration;

    private CompareStrategy compareStrategy;
    private FinishProcesStatus finishProcesStatus;

    private int sucessCount;
    private int failCount;
    private int skipedFiles;

    private Set<ComparedItemOperItemRowModel> failPathList;

    public JournalItemDataPack() {
    }

    public JournalItemDataPack(LocalDateTime DateTime, LocalTime procesDuration, CompareStrategy compareStrategy,
                               FinishProcesStatus finishProcesStatus, int sucessCount, int failCount,
                               Set<ComparedItemOperItemRowModel> failPathList, int skipedFiles) {
        this.id = System.currentTimeMillis();
        this.dateTimeOfStart = DateTime;
        this.procesDuration = procesDuration;

        this.compareStrategy = compareStrategy;
        this.finishProcesStatus = finishProcesStatus;

        this.sucessCount = sucessCount;
        this.failCount = failCount;
        this.skipedFiles = skipedFiles;

        this.failPathList = new LinkedHashSet<>(failPathList);

    }





    public long getId() {
        return id;
    }

    public LocalDateTime getDateTimeOfStart() {
        return dateTimeOfStart;
    }

    public LocalTime getProcesDuration() {
        return procesDuration;
    }

    public CompareStrategy getCompareStrategy() {
        return compareStrategy;
    }

    public FinishProcesStatus getFinishProcesStatus() {
        return finishProcesStatus;
    }

    public int getSucessCount() {
        return sucessCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public Set<ComparedItemOperItemRowModel> getFailPathList() {
        return failPathList;
    }

    public int getSkipedFiles() {
        return skipedFiles;
    }
}
