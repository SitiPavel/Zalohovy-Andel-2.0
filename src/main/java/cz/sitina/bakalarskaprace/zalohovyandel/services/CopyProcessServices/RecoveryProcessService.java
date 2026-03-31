package cz.sitina.bakalarskaprace.zalohovyandel.services.CopyProcessServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.CopyFilesUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class RecoveryProcessService {
    private int processedFileCount = 0;
    private int successfullyRecoveredCount = 0;

    // ukazatel zda bylo přerušen proces obnovy
    private boolean wasCancelled = false;
    // ukazatel zda byl soubor rozpracován ale nedokončen díky přerušení
    private boolean isBrokenByCancel = false;


    public void executeRecovery(List<BackupDatabaseDTO> foundDtoItems,
                                Path targetDirectory,
                                CopyFilesUtility.ProgressCallback callback,
                                Supplier<Boolean> isCancelled) {

        if (foundDtoItems == null || foundDtoItems.isEmpty()) {
            ErrorUtility.getInstance("Chyba obnovy",
                    "Prázdný seznam souborů.",
                    "Nenalezeny žádné soubory k obnově.");
            return;
        }

        // Spočítáme celkový objem dat pro Progress Bar
        long totalBytes = foundDtoItems.stream()
                .mapToLong(BackupDatabaseDTO::getSize)
                .sum();
        long globalProcessedBytes = 0;
        Path targetAbsolutPathForFile = null;
        boolean anyFileProcessed = false;

        for (BackupDatabaseDTO dto : foundDtoItems) {
            if (isCancelled.get()) {
                wasCancelled = true;
                break;
            }

            // Zdrojová cesta v záloze (absolutní cesta uložená v DTO)
            Path sourceAbsolutPath = Paths.get(dto.getAbsolutPath());
            String currentFileRelativePath = dto.getRelativePath();

            if (!Files.isReadable(sourceAbsolutPath)) {
                callback.onItemCompleted(false, dto);
                continue;
            }

            // Cíl, kam soubor zapisujeme (Nová složka + relativní cesta)
            targetAbsolutPathForFile = targetDirectory.resolve(currentFileRelativePath);
            boolean fileSuccess = true;

            try {
                if (targetAbsolutPathForFile.getParent() != null) {
                    Files.createDirectories(targetAbsolutPathForFile.getParent());
                }

                copyFileWithProgress(sourceAbsolutPath, targetAbsolutPathForFile,
                        globalProcessedBytes, totalBytes,
                        currentFileRelativePath, callback,
                        isCancelled);

                if (!isCancelled.get()) {
                    successfullyRecoveredCount++;
                    processedFileCount++;
                    globalProcessedBytes += dto.getSize();
                    anyFileProcessed = true;

                    callback.onItemCompleted(true, dto);
                } else {
                    // Pokud bylo kopírování přerušeno, smažeme nekompletní soubor
                    isBrokenByCancel = true;
                    wasCancelled = true;
                    Files.deleteIfExists(targetAbsolutPathForFile);
                    fileSuccess = false;
                    callback.onItemCompleted(false, dto);
                }
            } catch (IOException e) {
                System.err.println("Chyba při zpracování: " + sourceAbsolutPath);
                callback.onItemCompleted(false, dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Odstranění nekompletního souboru při tvrdém přerušení
        if (isBrokenByCancel && targetAbsolutPathForFile != null) {
            try {
                Files.deleteIfExists(targetAbsolutPathForFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // A TADY, AŽ SKONČÍ CYKLUS buď po uspěšném dokončeí nebo přerušení
        //zeptá se zda zanechat částečnou obnovu nebo smazat
        if (wasCancelled && anyFileProcessed) {
            boolean keepPartialRecovery = keepPartialRecoveryDialogWindow();
            if (!keepPartialRecovery) {
                deleteDirectoryRecursively(targetDirectory);
            }
        }
    }

    // dialogové okno pro dotaz na smazání částečné obnovy
    private boolean keepPartialRecoveryDialogWindow() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Obnova přerušena");
            alert.setHeaderText("Obnova dat byla přerušena uživatelem.");
            alert.setContentText("Část souborů již byla úspěšně zkopírována. Chcete tyto soubory ponechat " +
                    "(částečná obnova), nebo chcete celou tuto nedokončenou operaci zrušit a vytvořenou složku smazat?");

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
            Thread.interrupted();
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Smaže celou cílovou složku i s jejím obsahem
    private void deleteDirectoryRecursively(Path targetDirectory) {
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


    private void copyFileWithProgress(Path sourceAbsolutPath, Path targetAbsolutPath,
                                      long alreadyProcessedGlobal, long totalBytes,
                                      String currentFileRelativePath, CopyFilesUtility.ProgressCallback callback,
                                      Supplier<Boolean> isCancelled) throws IOException {
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