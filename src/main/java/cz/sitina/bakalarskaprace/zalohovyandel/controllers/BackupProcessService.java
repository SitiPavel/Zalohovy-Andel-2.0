package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BackupProcessService {
    private int processedFileCount = 0;
    private int successfullyPackagedCount = 0;

    // ukazatel zda bylo přerušen proces zálohy
    private boolean wasCancelled = false;
    // ukazatel zda byl soubor rozpracován ale nedokončen díky přerušení
    private boolean isBrokenByCancel = false;

    public interface ProgressCallback {
        void onProgress(long bytesProcessed, long totalBytes, String currentFile,
                        int processedFileCount);
        void onItemCompleted(boolean success, ComparedItemOperItemRowModel row);
    }


    // zjistí nám do jakého balíčku máme dát dat (fullBackup1, increment1 nebo increment 23 )
    public void executeBackupProcess(List<ComparedItemOperItemRowModel> comparedFilesList,
                                     BackupTask editableBackupTask,
                                     ProgressCallback callback,
                                     Supplier<Boolean> isCancelled) {

        List<ComparedItemOperItemRowModel> filesToBackup = comparedFilesList.stream()
                .filter(row -> row.getOperation() == CompareOperations.ADD_RIGHT ||
                                                           row.getOperation() == CompareOperations.UPDATE_RIGHT)
                .collect(Collectors.toList());

        if (filesToBackup.isEmpty()) {
            System.out.println("Záloha není nutná.");
            return;
        }

        // 1. Spočítáme celkový objem dat pro Progress Bar
        long totalBytes = filesToBackup.stream()
                .mapToLong(row -> row.getLeftItem().getFileSize())
                .sum();
        long globalProcessedBytes = 0;
        Path targetAbsolutPath = null;
        Path packageAbsolutePath;


        try (DatabaseManager databaseManager = new DatabaseManager(editableBackupTask.getBackupDatabasePath())) {

            int dataPackageCount = databaseManager.getPackageCount();

            String packName;
            if (dataPackageCount == 0) {
                packName = "FullBackup_1";
            } else {
                packName = "Increment_" + dataPackageCount;
            }

            packageAbsolutePath = Paths.get(editableBackupTask.getRootBackupPath(), packName);
            Files.createDirectories(packageAbsolutePath);

            List<BackupDatabaseDTO> recordsToDatabase = new ArrayList<>();

            for (ComparedItemOperItemRowModel row : filesToBackup) {
                if (isCancelled.get()) {
                    wasCancelled = true;
                    break;
                }

                FileItemModel leftItem = row.getLeftItem();
                Path sourceAbsolutPath = leftItem.getFileAbsolutePath();
                String currentFileRelativePath = leftItem.getRelativePath().toString();

                if (!Files.isReadable(sourceAbsolutPath)) {
                    callback.onItemCompleted(false, row);
                    continue;
                }

                boolean fileSuccess = true;
                targetAbsolutPath = packageAbsolutePath.resolve(leftItem.getRelativePath());
                try {


                    if (targetAbsolutPath.getParent() != null) {
                        Files.createDirectories(targetAbsolutPath.getParent());
                    }

                    // proces kopírování souboru
                    copyFileWithProgress(sourceAbsolutPath, targetAbsolutPath,
                            globalProcessedBytes, totalBytes,
                            currentFileRelativePath, callback,
                            isCancelled);


                    if (!isCancelled.get()) {
                        BackupDatabaseDTO copiedItem = new BackupDatabaseDTO(
                                targetAbsolutPath.toString(),
                                leftItem.getRelativePath().toString(),
                                leftItem.getFileSize(),
                                leftItem.getLastModified(),
                                packName
                        );

                        recordsToDatabase.add(copiedItem);;

                        successfullyPackagedCount++;
                        processedFileCount++;
                        globalProcessedBytes += leftItem.getFileSize(); // Přidáme velikost souboru do celku

                        callback.onItemCompleted(true, row);
                    } else {
                        // Pokud bylo kopírování přerušeno, smažeme nekompletní soubor
                        isBrokenByCancel = true;
                        wasCancelled = true;
                        Files.deleteIfExists(targetAbsolutPath);
                        fileSuccess = false;
                        callback.onItemCompleted(false, row);
                    }



                } catch (IOException e) {
                    System.err.println("Chyba při zpracování: " + sourceAbsolutPath);
                    callback.onItemCompleted(false, row);
                }
            }

            if (isBrokenByCancel) {
                try {
                    Files.deleteIfExists(targetAbsolutPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // A TADY, AŽ SKONČÍ CYKLUS buď po uspěšném dokončeí nebo přerušení
            if (wasCancelled && !recordsToDatabase.isEmpty() ) {
                boolean keepPartialBackup = keepPartialBackupDialogWindow();
                if (keepPartialBackup) {
                    saveIntoDatabase(recordsToDatabase, databaseManager);
                    System.out.println("Databáze uložena.");
                } else  {
                    deleteDirectoryRecursively(packageAbsolutePath);
                    System.out.println("tady se to zesralo #2");
                    System.out.println("Částečná záloha byla smazána.");
                }
            } else {
                saveIntoDatabase(recordsToDatabase, databaseManager);
            }



        } catch (SQLException e)  {
            e.printStackTrace();
            ErrorUtility.getInstance("Chyba databáze",
                    "Chyba spojení s databází",
                    "Nastala chyba spojení mezi aplikací a databází zálohy.");

        } catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Chyba dat",
                    "Zastavena manipulace s daty. ",
                    "Příčinou může být ztraceného přístupu ke zdrojové/cílové složce, " +
                            "nedostatek místa v cíli nebo chybějící práva čtení nebo zápisu do složky.");
        }

    }

    private void saveIntoDatabase(List<BackupDatabaseDTO> recordsToDatabase, DatabaseManager databaseManager) {
        if (!recordsToDatabase.isEmpty()) {
            try {
                databaseManager.insertNewItemsBatch(recordsToDatabase);
            } catch (SQLException e) {
                System.err.println("Kritická chyba: Nepodařilo se uložit dávku záznamů do databáze.");
                // V reálném nasazení by se zde musely smazat i ty fyzické soubory,
                // protože nejsou v DB, ale prozatím logujeme chybu.
            }
        }
    }

    // dialogové okno pro dotaz na smazání částečné zálohy
    private boolean keepPartialBackupDialogWindow() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Odeslání požadavku na vykreslení do GUI vlákna
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Zálohování přerušeno");
            alert.setHeaderText("Zálohování bylo přerušeno uživatelem.");
            alert.setContentText("Část souborů již byla úspěšně zkopírována. Chcete tyto soubory v záloze ponechat " +
                    "(vytvořit částečnou zálohu), nebo chcete celou tuto nedokončenou operaci zrušit a smazat?");

            ButtonType btnKeep = new ButtonType("Ponechat uložené");
            ButtonType btnDiscard = new ButtonType("Smazat částečnou zálohu");

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
            System.out.println("tady se to zesralo #1");
            e.printStackTrace();
            return false; // V případě chyby raději děláme bezpečný Rollback
        }
    }


    // Smaže celou složku i s jejím obsahem
    private void deleteDirectoryRecursively(Path packageAbsolutePath) {
        if (!Files.exists(packageAbsolutePath)) return;

        try {
            Files.walk(packageAbsolutePath)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(java.io.File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void copyFileWithProgress(Path sourceAbsolutPath, Path targetAbsolutPath,
                                      long alreadyProcessedGlobal, long totalBytes,
                                      String currentFileRelativePath, ProgressCallback callback,
                                      Supplier<Boolean> isCancelled) throws IOException{
        try (FileChannel sourceChannel = FileChannel.open(sourceAbsolutPath, StandardOpenOption.READ);
             FileChannel targetChannel = FileChannel.open(targetAbsolutPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

            long size = sourceChannel.size();
            long position = 0;
            // Zvýšený buffer na 4MB (4 * 1024 * 1024)
            long chunkSize = 4194304;
            long lastReportedBytes = 0;

            while (position < size) {
                if (isCancelled.get()) {
                    isBrokenByCancel = true;
                    wasCancelled = true;
                    break;
                }

                // Nativní kopírování přes disk
                long bytesCopied = sourceChannel.transferTo(position, chunkSize, targetChannel);
                position += bytesCopied;

                if ((position - lastReportedBytes) >= chunkSize) {
                    callback.onProgress(alreadyProcessedGlobal + position, totalBytes, currentFileRelativePath, processedFileCount);
                    lastReportedBytes = position;
                }
            }
            callback.onProgress(alreadyProcessedGlobal + position, totalBytes, currentFileRelativePath, processedFileCount);
        }
    }

}
