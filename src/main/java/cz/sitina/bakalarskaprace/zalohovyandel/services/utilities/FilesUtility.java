package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class FilesUtility {

    // služby
    private static Utility utility = new Utility();


    public interface ProgressCallback {
        void onProgress(long bytesProcessed, long totalBytes, String currentFile,
                        int processedFileCount);
        void onItemCompleted(boolean success, ComparedItemOperItemRowModel row);
        void onItemCompleted(boolean success, BackupDatabaseDTO dto);
    }





    // Smaže celou cílovou složku i s jejím obsahem
    public static void deleteDirectoryRecursively(Path targetDirectory) {
        if (!Files.exists(targetDirectory)) return;

        try {
            Files.walk(targetDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            System.out.println("Rollback dokončen. Složka s částečnou obnovou byla smazána.");
        } catch (IOException e) {
            System.err.println("Chyba při mazání složky během rollbacku: " + targetDirectory);
            e.printStackTrace();
        }
    }

    // dialogové okno pro dotaz na smazání částečné obnovy
    public static boolean keepPartialCopyDialogWindow(String title,
                                                    String header,
                                                    String massage) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(massage);

            ButtonType btnKeep = new ButtonType("Ponechat obnovené");
            ButtonType btnDiscard = new ButtonType("Smazat složku");

            alert.getButtonTypes().setAll(btnKeep, btnDiscard);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == btnKeep) {
                future.complete(true);
            } else {
                future.complete(false);
            }
        });

        try {
            /**Thread.interrupted(); = pokud aplikace má více vláken a veldejší je pozastaveno, java ho hned probudí,
             * to třeší Thread.interrupted();, to nám zaručí že okno bude čekat na ospověď z okna
             * */
            Thread.interrupted();
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isDirEmpty(Path path) throws IOException {
        if (path == null || !Files.isDirectory(path)) return false;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            return !ds.iterator().hasNext();
        }
    }


}
