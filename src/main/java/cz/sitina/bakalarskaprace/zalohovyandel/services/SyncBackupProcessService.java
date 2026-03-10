package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;
import java.util.List;

public class SyncBackupProcessService {
    int processedFileCount = 0;

    public interface ProgressCallback {
        void onProgress(long bytesProcessed, long totalBytes, String currentFile, int processedFileCount);
        void onItemCompleted(boolean success);
    }

    public void executeCopyFiles(
            TaskModel task,
            List<ComparedItemOperItemRowModel> itemsToProcess,
            ProgressCallback callback,
            java.util.function.Supplier<Boolean> isCancelled) {

        long totalBytes = itemsToProcess.stream()
                .mapToLong(item -> getBytesToTransfer(item))
                .sum();

        // Globální počítadlo hotových bytů
        long globalProcessedBytes = 0;

        Path leftRoot = Paths.get(task.getLeftPath());
        Path rightRoot= Paths.get(task.getRightPath());

        callback.onProgress(globalProcessedBytes, totalBytes, "Start...", processedFileCount);
        boolean success = true;

        for (ComparedItemOperItemRowModel row : itemsToProcess) {
            if (isCancelled.get()) {break;}

            CompareOperations operations = row.getOperation();
            FileItemModel leftItem = row.getLeftItem();
            FileItemModel rightItem = row.getRightItem();

            String currentFileName = (leftItem.getRelativePath() != null && !leftItem.getRelativePath().toString().isEmpty())
                    ? leftItem.getRelativePath().toString() : rightItem.getRelativePath().toString();

            long fileSize = 0;
            try {
                switch (operations) {
                    case ADD_LEFT, UPDATE_LEFT -> {
                        Path source = rightItem.getFileAbsolutePath();
                        Path target = leftRoot.resolve(rightItem.getRelativePath());
                        fileSize = rightItem.getFileSize();

                        copyFileWithProgress(source, target,
                                globalProcessedBytes, totalBytes,
                                currentFileName, callback,
                                isCancelled);
                        processedFileCount++;
                    }
                    case ADD_RIGHT, UPDATE_RIGHT -> {
                        Path source = leftItem.getFileAbsolutePath();
                        Path target = rightRoot.resolve(leftItem.getRelativePath());
                        fileSize = leftItem.getFileSize();

                        copyFileWithProgress(source, target,
                                globalProcessedBytes, totalBytes,
                                currentFileName, callback,
                                isCancelled);
                        processedFileCount++;
                    }
                    case DELETE_LEFT -> {
                        Files.deleteIfExists(leftItem.getFileAbsolutePath());
                        processedFileCount++;
                    }
                    case DELETE_RIGHT -> {
                        Files.deleteIfExists(rightItem.getFileAbsolutePath());
                        processedFileCount++;
                    }
                    case DELETE_BOTH -> {
                        Files.deleteIfExists(leftItem.getFileAbsolutePath());
                        Files.deleteIfExists(rightItem.getFileAbsolutePath());
                        processedFileCount++;
                    }
                }
                if (operations.name().contains("ADD") || operations.name().contains("UPDATE")) {
                    globalProcessedBytes += fileSize;
                }

            } catch (IOException e) {
                System.out.println("Chyba chyba při zpracování souboru "
                        + currentFileName + " : " + e.getMessage());
                success = false;
            }

            callback.onItemCompleted(success);
        }
    }
    private void  copyFile(Path source, Path target) throws IOException {
        // pokud složka neexistuje tak se vytvoří
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }

    private long getBytesToTransfer(ComparedItemOperItemRowModel row) {
        return switch (row.getOperation()) {
            case UPDATE_RIGHT, ADD_RIGHT -> row.getLeftItem().getFileSize();
            case UPDATE_LEFT, ADD_LEFT -> row.getRightItem().getFileSize();
            default -> 0;
        };
    }

    private void copyFileWithProgress(Path source, Path target,
                                      long alreadyProcessedGlobal, long totalBytes,
                                      String fileName, ProgressCallback callback,
                                      java.util.function.Supplier<Boolean> isCancelled) throws IOException {

        // pokud složka neexistuje
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }

        // stream IO pro čtení a zápis
        try (var in = Files.newInputStream(source);
            var out = Files.newOutputStream(target)) {

            byte[] buffer = new byte[8192]; // velikost bloků v bytech se kterými bude kód manipulovat
            int length;
            long bytesCopiedForThisFile = 0;
            long lastReportedBites =0;

            while ( (length = in.read(buffer)) > 0) {
                if (isCancelled.get()) {
                    break;
                }

                out.write(buffer, 0, length);
                bytesCopiedForThisFile += length;


                //nechceme jlásit každých blok proto budeme kopírovat 1 MB od posledního glášení
                // pokud bychom hlásili každý blok mohlo byt to zahltit aplikaci
                if ( (bytesCopiedForThisFile - lastReportedBites)  >= 1024*1024) {
                    callback.onProgress(alreadyProcessedGlobal + bytesCopiedForThisFile, totalBytes, fileName, processedFileCount);
                    lastReportedBites = bytesCopiedForThisFile;
                }
            }
            callback.onProgress(alreadyProcessedGlobal + bytesCopiedForThisFile, totalBytes, fileName, processedFileCount);
        }//Soubor je přenesen a uzavřen

        /*
        * PŘENOS METADAT, důležité aby se kopie souboru tvářila jako identická kopie a algoritmy je považovali za stejné
        * */
        // zapsání časových razítek
        try {
            BasicFileAttributes sourceAttributes = Files.readAttributes(source, BasicFileAttributes.class);
            BasicFileAttributeView targetView = Files.getFileAttributeView(target, BasicFileAttributeView.class);

            targetView.setTimes(
                    sourceAttributes.lastModifiedTime(),
                    sourceAttributes.lastAccessTime(),
                    sourceAttributes.creationTime()
            );

        } catch (IOException e) {
            System.err.println("Nepodařilo se nastavit časová razítka pro: " + fileName +
                    "\n"+ e.getMessage());
        }

        // Přenos DOS atributů (Hidden, ReadOnly, System) - Důležité pro Windows
        try {
            DosFileAttributes sourceDos = Files.readAttributes(source, DosFileAttributes.class);

            Files.setAttribute(target, "dos:hidden", sourceDos.isHidden());
            Files.setAttribute(target, "dos:readonly", sourceDos.isReadOnly());
            Files.setAttribute(target, "dos:archive", sourceDos.isArchive());
            Files.setAttribute(target, "dos:system", sourceDos.isSystem());
        } catch (UnsupportedOperationException e) {
            // Ignorujeme, pokud systém nepodporuje DOS atributy (např. Linux/Mac)
        } catch (Exception e) {
            System.err.println("Nepodařilo se nastavit DOS atributy pro: " + fileName +
                    "\n" + e.getMessage());
        }



    }
}
