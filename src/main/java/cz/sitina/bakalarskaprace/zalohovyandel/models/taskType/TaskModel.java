package cz.sitina.bakalarskaprace.zalohovyandel.models.taskType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.TaskTypes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "taskClassType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ManualSyncTask.class, name = "MANUAL_SYNC_TASK"),
        @JsonSubTypes.Type(value = BackupTask.class, name = "BACKUP_TASK")
})

public abstract class TaskModel {
    private long id; // máme jako daatum a čas vytvoření v milisekundách
    private TaskTypes taskType;
    private String name;
    protected String leftPath;
    protected String rightPath;
    private String description;
    private String status;
    private LocalDate creationDate;
    private Set<String> blockList = new LinkedHashSet<>();
    private List<JournalItemDataPack> jurnalInfoList;

    private GuiOperationFilters_TaskData guiOperationFilters;


    /// uležená data pro porovnání
    private CompareStrategy compareStrategy;
    private boolean isContentHash = false;

    // prázdný konstruktor pro json
    protected TaskModel() {
    }

    // pro nové úlohy
    public TaskModel( TaskTypes taskType,
                     String name,
                     String leftPath,
                     String rightPath,
                     String description) {

        this.id = System.currentTimeMillis();
        this.taskType = taskType;

        this.name = name;
        this.leftPath = leftPath;
        this.rightPath = rightPath;
        this.description = description;
        this.creationDate = LocalDate.now();
        this.guiOperationFilters = new GuiOperationFilters_TaskData();
        this.compareStrategy = CompareStrategy.BIDIRECTIONAL_SYNC;
        this.jurnalInfoList = new ArrayList<>();

    }
    // pro nové úlohy
    public TaskModel( TaskTypes taskType,
                      String name,
                      String leftPath,
                      String rightPath,
                      String description,
                      CompareStrategy compareStrategy) {

        this.id = System.currentTimeMillis();
        this.taskType = taskType;

        this.name = name;
        this.leftPath = leftPath;
        this.rightPath = rightPath;
        this.description = description;
        this.creationDate = LocalDate.now();
        this.guiOperationFilters = new GuiOperationFilters_TaskData();
        this.compareStrategy = compareStrategy;
        this.jurnalInfoList = new ArrayList<>();

    }

    public TaskModel( TaskTypes taskType,
                      String name,
                      String leftPath,
                      String rightPath,
                      String description,
                      GuiOperationFilters_TaskData GuiOperationFilters) {

        this.id = System.currentTimeMillis();
        this.taskType = taskType;

        this.name = name;
        this.leftPath = leftPath;
        this.rightPath = rightPath;
        this.description = description;
        this.creationDate = LocalDate.now();
        // složitý typ který by bez new poslal jen odkaz
        this.guiOperationFilters = GuiOperationFilters;
        this.jurnalInfoList = new ArrayList<>();
    }

    protected TaskModel( TaskModel source) {
        this.id = source.id;
        this.name = source.name;
        this.taskType = source.taskType;
        this.leftPath = source.leftPath;
        this.rightPath = source.rightPath;
        this.description = source.description;
        this.creationDate = source.creationDate;
        this.isContentHash = source.isContentHash;
        this.compareStrategy = source.compareStrategy;

        // složité datové typy, proto vytvoříme nové, kdyby nebylo new tak by se sdílel jen odkaz a né objekt
        this.blockList = new LinkedHashSet<>(source.blockList);
        this.guiOperationFilters = new GuiOperationFilters_TaskData(source.guiOperationFilters);
        this.jurnalInfoList = new ArrayList<>(source.jurnalInfoList);

    }

    public void updateTaskModel(TaskModel task) {
        this.name = task.name;
        this.leftPath = task.leftPath;
        this.rightPath = task.rightPath;
        this.description = task.description;
        this.isContentHash = task.isContentHash;
        this.compareStrategy = task.compareStrategy;

        this.blockList = new LinkedHashSet<>(task.blockList);
        this.guiOperationFilters = new GuiOperationFilters_TaskData(task.guiOperationFilters);
        this.jurnalInfoList = new ArrayList<>(task.jurnalInfoList);
    }

    public void updateAllJournalList(List<JournalItemDataPack> journalList) {
        jurnalInfoList.clear();
        jurnalInfoList.addAll(journalList);
    }

    //getry
    public long getId() {
        return id;
    }
    public TaskTypes getTaskType() {
        return taskType;
    }
    public String getName() {
        return name;
    }
    public String getLeftPath() {
        return leftPath;
    }
    public String getRightPath() {
        return rightPath;
    }
    public String getDescription() {
        return description;
    }
    public LocalDate getCreationDate() {
        return creationDate;
    }
    public Set<String> getBlockSet() {
        return blockList;
    }
    public String getStatus() {
        return status;
    }
    public CompareStrategy getCompareStrategy() {
        return compareStrategy;
    }

    public List<JournalItemDataPack> getJurnalInfoList() {
        return jurnalInfoList;
    }

    public boolean isContentHash() {
        return isContentHash;
    }
    public GuiOperationFilters_TaskData getGuiOperationFilters() {
        return guiOperationFilters;
    }

    //setry

    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setBlockSet(Set<String> blockList) {
        this.blockList = blockList;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    /// setry z předka (pouze tady jsou potřeba)
    public void setCompareStrategy(CompareStrategy compareStrategy) {
        this.compareStrategy = compareStrategy;
    }
    public void setContentHash(boolean contentHash) {
        isContentHash = contentHash;
    }

    public void setJournalInfoList(List<JournalItemDataPack> jurnalInfoList) {
        this.jurnalInfoList = jurnalInfoList;
    }
}

