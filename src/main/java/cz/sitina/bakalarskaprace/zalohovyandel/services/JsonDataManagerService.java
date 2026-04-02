package cz.sitina.bakalarskaprace.zalohovyandel.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class JsonDataManagerService {

    // drží instanci JsonDataManagerService pro
    private static JsonDataManagerService instance;

    private String taskFileName = "taskDataFile";
    private  int maxBackup = 4;

    //Složka zálohový anděl
    private File appDirectory;
    private File tasksDirectory;
    // soubor uložených tásků
    private File taskDataJsonFile;


    private final ObjectMapper mapper;


    private JsonDataManagerService() {
        appDirectory = getAppDataFolder("ZalohovyAndel");
        tasksDirectory = new File(appDirectory, "tasks");
        taskDataJsonFile = new File(tasksDirectory, taskFileName+".json");

        if (!tasksDirectory.exists()) {
            tasksDirectory.mkdirs();
        }

        // Inicializace a nastavení Jacksonu
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // bude formátovaný pod sebou (přehledný)
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mapper.registerModule(new JavaTimeModule());

    }
    // pokud neexistuje, vytvoří instanci a pokud existuje tak jen vrátí existující
    public static JsonDataManagerService getInstance() {
        if (instance == null) {
            instance = new JsonDataManagerService();
        }
        return instance;
    }

    public List<TaskModel> loadAllTasks() {
        if (!(taskDataJsonFile).exists()) {
            return new ArrayList<>();
        }

        try {
            return mapper.readValue(taskDataJsonFile, new TypeReference<List<TaskModel>>() {
            });
        } catch (IOException e) {
            ErrorUtility.getInstance("Chyba načítaní dat",
                    "Nepodařilo se načíst uložená data",
                    "Soubor s uloženými úlohami byl poškozen, proto není možné načíst data.");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /// Ukládají zálohu
    public void saveNewTask_saveOldSave(TaskModel task) {
        createBackup();
        saveNewTask_rewriteOldSave(task);
        deleteOldBackup();
    }


    /// přepisují uložený soubor, nevratně je ztracen
    public void saveNewTask_rewriteOldSave(TaskModel task) {
        List<TaskModel> TaskList = loadAllTasks();
        TaskList.add(task);
        saveAllTasks_rewriteOldSave(TaskList);
    }

    public void updateTask_saveOldSave(TaskModel updatedTask) {
        List<TaskModel> taskList = loadAllTasks();
        boolean found = false;
        for (TaskModel taskModel : taskList) {
            if (taskModel.getId() == updatedTask.getId()) {
                taskModel.updateTaskModel(updatedTask);
                found = true;

            }
        }
        if (found) {
            saveAllTask_SaveOldSave(taskList);
        }

    }
    public void updateTask_rewriteOldSave(TaskModel task) {

    }

    public void saveAllTask_SaveOldSave(List<TaskModel> taskList) {
        createBackup();
        saveAllTasks_rewriteOldSave(taskList);
        deleteOldBackup();

    }

    public void saveAllTasks_rewriteOldSave(List<TaskModel> tasks) {
        try {
            mapper.writerFor(new TypeReference<List<TaskModel>>() {}).writeValue(taskDataJsonFile, tasks);
        }catch (IOException e) {
            ErrorUtility.getInstance("Chyba ukládání dat",
                    "Nepodařilo se uložit data",
                    "Aplikace může mít omezená práva ke složce nebo je soubor používán jiným procesem.");
            e.printStackTrace();
        }
    }


    // získáme složku kde jsou data pro ZalohovacihoAndela pokud neexistuje tak
    private File getAppDataFolder(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File appFolder;

        if (os.contains("win")) {
            String appDataPath = System.getenv("APPDATA");
            appFolder = new File(appDataPath, appName);
        } else if (os.contains("mac")) {
            appFolder = new File(userHome, "Library/Application Support/" + appName);
        } else {
            appFolder = new File(userHome, ".config/" + appName);;
        }

        // pokud složka neexistuje tak jí vytvoří
        if (!appFolder.exists()) {
            appFolder.mkdirs();
        }

        return appFolder;
    }

    private void createBackup() {
        if (!taskDataJsonFile.exists()) {
            return;
        }
        try {
            //nastavení datumu a jeho formátování
            DateTimeFormatter formater = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss");
            String time = LocalDateTime.now().format(formater);

            // vytvoření zálohy souboru
            String newName = taskFileName + "_Backup_" + time + ".json";
            File backupFile = new File(tasksDirectory, newName);
            Files.copy(taskDataJsonFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            ErrorUtility.getInstance("Chyba zálohy",
                    "Nepodařilo se vytvořit zálohu úloh",
                    "Pravděpodobně aplikace nemá práva k zápisu nebo čtení, " +
                            "proto není možné vytvořit zálohu.");
            e.printStackTrace();
        }

    }

    private void deleteOldBackup() {
        File[] backups = tasksDirectory.listFiles((dir, name) ->
                name.startsWith(taskFileName + "_Backup_") && name.endsWith(".json"));

        if ( backups == null || backups.length <= maxBackup ) {
            return;
        }

        // seřadíme podle datumu: index 0 = nejstarší
        Arrays.sort(backups, Comparator.comparingLong(File::lastModified));

        int filesToDelete = backups.length - maxBackup;
        for (int i =0; i < filesToDelete; i++) {
            backups[i].delete();
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
