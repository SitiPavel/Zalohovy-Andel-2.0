package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.SynchronizationProcessService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BackupProcessService {
    private int processedFileCount = 0;
    int successfullyPackagedCount = 0;

    public interface ProgressCallback {
        void onProgress(long bytesProcessed, long totalBytes, String currentFile,
                        int processedFileCount);
        void onItemCompleted(boolean success, ComparedItemOperItemRowModel row);
    }


    // zjistí nám do jakého balíčku máme dát dat (fullBackup1, increment1 nebo increment 23 )
    public void executeBackupProcess(List<ComparedItemOperItemRowModel> comparedFilesList,
                                     BackupTask editableBackupTask,
                                     ProgressCallback callback,
                                     java.util.function.Supplier<Boolean> isCancelled) {

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


        try (DatabaseManager databaseManager = new DatabaseManager(editableBackupTask.getBackupDatabasePath())) {

            int dataPackageCount = databaseManager.getPackageCount();

            String packName;
            if (dataPackageCount == 0) {
                packName = "FullBackup_1";
            } else {
                packName = "Increment_" + dataPackageCount;
            }

            Path packageAbsolutePath = Paths.get(editableBackupTask.getRootBackupPath(), packName);
            Files.createDirectories(packageAbsolutePath);

            List<BackupDatabaseDTO> recordsToDatabase = new ArrayList<>();

            for (ComparedItemOperItemRowModel row : filesToBackup) {
                if (isCancelled.get()) {break;}

                FileItemModel leftItem = row.getLeftItem();
                Path sourceAbsolutPath = leftItem.getFileAbsolutePath();
                String currentFileRelativePath = leftItem.getRelativePath().toString();

                if (!Files.isReadable(sourceAbsolutPath)) {
                    callback.onItemCompleted(false, row);
                    continue;
                }

                boolean fileSuccess = true;

                try {
                    Path targetAbsolutPath = packageAbsolutePath.resolve(leftItem.getRelativePath());

                    if (targetAbsolutPath.getParent() != null) {
                        Files.createDirectories(targetAbsolutPath.getParent());
                    }

                    // Files.copy(sourceAbsolutPath, targetAbsolutPath);
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
                        Files.deleteIfExists(targetAbsolutPath);
                        fileSuccess = false;
                        callback.onItemCompleted(false, row);
                    }

                    // A TADY, AŽ SKONČÍ CYKLUS (nebo ho uživatel přeruší stornem), VŠE SE ZAPÍŠE NAJEDNOU
                    if (!recordsToDatabase.isEmpty()) {
                        try {
                            databaseManager.insertNewItemsBatch(recordsToDatabase);
                        } catch (SQLException e) {
                            System.err.println("Kritická chyba: Nepodařilo se uložit dávku záznamů do databáze.");
                            // V reálném nasazení by se zde musely smazat i ty fyzické soubory,
                            // protože nejsou v DB, ale prozatím logujeme chybu.
                        }
                    }

                } catch (IOException e) {
                    System.err.println("Chyba při zpracování: " + sourceAbsolutPath);
                    callback.onItemCompleted(false, row);
                }
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


    private void copyFileWithProgress(Path sourceAbsolutPath, Path targetAbsolutPath,
                                      long alreadyProcessedGlobal, long totalBytes,
                                      String currentFileRelativePath, ProgressCallback callback,
                                      java.util.function.Supplier<Boolean> isCancelled) throws IOException{
        try (java.nio.channels.FileChannel sourceChannel = java.nio.channels.FileChannel.open(sourceAbsolutPath, java.nio.file.StandardOpenOption.READ);
             java.nio.channels.FileChannel targetChannel = java.nio.channels.FileChannel.open(targetAbsolutPath, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE)) {

            long size = sourceChannel.size();
            long position = 0;
            // Zvýšený buffer na 4MB (4 * 1024 * 1024) - dříve jsi volal UI příliš často
            long chunkSize = 4194304;
            long lastReportedBytes = 0;

            while (position < size) {
                if (isCancelled.get()) break;

                // Nativní kopírování přes disk (extrémně rychlé)
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
