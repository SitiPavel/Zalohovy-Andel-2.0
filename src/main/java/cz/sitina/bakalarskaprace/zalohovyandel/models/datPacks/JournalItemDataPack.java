package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type" // Název pole, které se objeví v JSONu
)
@JsonSubTypes({
        // Zde musíš vyjmenovat všechny potomky
        @JsonSubTypes.Type(value = JournalBackupItemDataPack.class, name = "backup"),
})

public class JournalItemDataPack {

    protected long id;
    protected LocalDateTime dateTimeOfStart;
    protected LocalTime processDuration;

    protected CompareStrategy compareStrategy;
    protected FinishProcesStatus finishProcesStatus;

    protected int failCount;
    protected int fileCount;

    protected String leftRootPath;
    protected String rightRootPath;


    protected Set<String> failPathList;

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

    public void setId(long id) {
        this.id = id;
    }

    public void setDateTimeOfStart(LocalDateTime dateTimeOfStart) {
        this.dateTimeOfStart = dateTimeOfStart;
    }

    public void setProcessDuration(LocalTime processDuration) {
        this.processDuration = processDuration;
    }

    public void setCompareStrategy(CompareStrategy compareStrategy) {
        this.compareStrategy = compareStrategy;
    }

    public void setFinishProcesStatus(FinishProcesStatus finishProcesStatus) {
        this.finishProcesStatus = finishProcesStatus;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public void setLeftRootPath(String leftRootPath) {
        this.leftRootPath = leftRootPath;
    }

    public void setRightRootPath(String rightRootPath) {
        this.rightRootPath = rightRootPath;
    }

    public void setFailPathList(Set<String> failPathList) {
        this.failPathList = failPathList;
    }
}
