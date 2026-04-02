package cz.sitina.bakalarskaprace.zalohovyandel;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.AppFrameController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.ManualSyncTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.JsonDataManagerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main extends Application {

    JsonDataManagerService jsonManager = JsonDataManagerService.getInstance();

    @Override
    public void start(Stage stage) throws IOException {

        TaskRepository taskRepository = new TaskRepository();



        /// načítá data při zapnutí aplikace
//        createFirstData(taskRepository); //Pro vytvoření dat
       /**/
       for (TaskModel task: jsonManager.loadAllTasks()){

            taskRepository.add(task);
        }/**/


        String resourcesPath = "/cz/sitina/bakalarskaprace/zalohovyandel/";

        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcesPath +"fxml/AppFrameView.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 700);

        AppFrameController appFrameController = loader.getController();
        appFrameController.setTaskRepository(taskRepository);
        appFrameController.showTasksOverview();


        stage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(
                        resourcesPath + "images/ZalohovyAndelLogo70x70.png"))));

        stage.setTitle("Zálohový Anděl");
        stage.setScene(scene);
        stage.show();

    }

    private void createFirstData(TaskRepository taskRepository) {


        ManualSyncTask task1 = new ManualSyncTask("Test operací pro složky",
                "D:\\testBakalarky\\Test03",
                "D:\\testBakalarky\\Test06",
                "testuje jestli se dobře přidělují operace složkám");
        taskRepository.add(task1);

        task1.getBlockSet().add("skola\\semestr1");
        task1.getBlockSet().add("skola\\semestr2\\PRIPO-to-podstatné (1).docx");
        task1.getBlockSet().add("DSC_0063.NEF");

        task1.getJurnalInfoList().add(new JournalItemDataPack(
                java.time.LocalDateTime.now(),         // DateTime: Aktuální datum a čas spuštění
                java.time.LocalTime.of(0, 15, 30),     // procesDuration: Např. 15 minut a 30 vteřin
                CompareStrategy.BIDIRECTIONAL_SYNC,    // compareStrategy: Zvolená strategie z Enumu
                FinishProcesStatus.SUCCESS,            // finishProcesStatus: Stav dokončení z Enumu
                120,                                   // sucessCount: Počet úspěšných
                2,                                     // failCount: Počet selhání
                new java.util.LinkedHashSet<>(List.of("D:\\testBakalarky\\Test05\\_gsdata_", "D:\\testBakalarky" +
                        "\\Test05\\Nový Dokument Microsoft Wordu.docx")),
                "D:\\testBakalarky\\Test03",
                "D:\\testBakalarky\\Test06"

        ));



        taskRepository.add(new ManualSyncTask("zaloha manual 1",
                "D:\\testBakalarky\\Test01",
                "D:\\testBakalarky\\Test02",
                "")
        );
        taskRepository.add(new ManualSyncTask("Obrovské množství souborů",
                "D:\\",
                "F:\\",
                "popis úlohy ")
        );

        BackupTask task11 = new BackupTask("Pro testování zálohy s hystorií",
                "D:\\testBakalarky\\Test05",
                "D:\\testBakalarky\\TestBackup01",
                "test historie ",
                false,
                false);
        task11.setCompareStrategy(CompareStrategy.BACKUP_TO_RIGHT);
        task11.getBlockSet().add("skola\\semestr2\\PRIPO_základy elektroniky.pptx");
        task11.getBlockSet().add("_gsdata_");

        taskRepository.add(task11);

        List<TaskModel> cistyList = new ArrayList<>(taskRepository.getTaskObservableList());
        jsonManager.saveAllTasks_rewriteOldSave(cistyList);
    }

    public static void main(String[] args) {
        launch();
    }
}
