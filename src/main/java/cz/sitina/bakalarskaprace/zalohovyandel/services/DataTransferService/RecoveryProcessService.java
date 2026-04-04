package cz.sitina.bakalarskaprace.zalohovyandel.services.DataTransferService;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.services.encryptionServices.SecureArchiveExtractor;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.FilesUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.application.Platform;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

public class RecoveryProcessService {
    private int processedFileCount = 0;
    private int successfullyRecoveredCount = 0;

    // ukazatel zda bylo přerušen proces obnovy
    private boolean wasCancelled = false;
    // ukazatel zda byl soubor rozpracován ale nedokončen díky přerušení
    private boolean isBrokenByCancel = false;


    public void executeRecovery(List<BackupDatabaseDTO> foundDtoItems,
                                Path targetDirectory, SecretKey secretKey,
                                FilesUtility.ProgressCallback callback,
                                Supplier<Boolean> isCancelled) {

        if (foundDtoItems == null || foundDtoItems.isEmpty()) {
            Platform.runLater(() -> {
                ErrorUtility.getInstance("Chyba obnovy",
                        "Prázdný seznam souborů.",
                        "Nenalezeny žádné soubory k obnově.");
            });
            return;
        }

        // Spočítáme celkový objem dat pro Progress Bar
        long totalBytes = foundDtoItems.stream()
                .mapToLong(BackupDatabaseDTO::getSize)
                .sum();
        long globalProcessedBytes = 0;
        Path targetAbsolutPathForFile = null;
        boolean anyFileProcessed = false;

        // tato mapa nám seřadí podle balíčků naše DTO oběkty reprezentující data na disku
        // do listu se vloží všechny položky které mají stejnou absolutní cestu (balíček)
        // cesta se vloží do klíče a my po
        Map<String, List<BackupDatabaseDTO>> groupedByPackage = foundDtoItems.stream()
                .collect(Collectors.groupingBy(BackupDatabaseDTO::getLocation));


        for (Map.Entry<String, List<BackupDatabaseDTO>> mapItem : groupedByPackage.entrySet()) {
            if (isCancelled.get()) {
                wasCancelled = true;
                break;
            }

            String packageName = mapItem.getKey();
            List<BackupDatabaseDTO> mapItemList = mapItem.getValue();

            boolean isArchive = packageName.endsWith(".enc") || packageName.endsWith(".zip");

            if (isArchive) { // obnova pro .zip nebo .enc

                BackupDatabaseDTO firstDto = mapItemList.get(0);
                String fullFilePath = firstDto.getAbsolutPath();

                // Najdeme, kde končí název balíčku a zbytek ustřihneme
                int endIndex = fullFilePath.lastIndexOf(packageName) + packageName.length();
                String packagePathStr = fullFilePath.substring(0, endIndex);


                try (SecureArchiveExtractor extractor = new SecureArchiveExtractor(Path.of(packagePathStr), secretKey)) {
                    ZipEntry zipEntry;

                    // Čteme postupně celý balíček od začátku do konce
                    while ((zipEntry = extractor.getNextEntry()) != null) {

                        if (isCancelled.get()) {
                            wasCancelled = true;
                            isBrokenByCancel = true;
                            break;
                        }

                        // Normalizujeme cestu v zipu pro porovnání
                        String entryName = zipEntry.getName().replace("/", "\\");

                        // Zjistíme, jestli tento soubor v ZIPu zrovna hledáme
                        BackupDatabaseDTO matchedDto = mapItemList.stream()
                                .filter(dto -> dto.getRelativePath().replace("/", "\\").equals(entryName))
                                .findFirst().orElse(null);

                        if (matchedDto != null) {
                            Path targeFileAbsolutPath = targetDirectory.resolve(matchedDto.getRelativePath());

                            // vytvoří složky rodičů
                            if (targeFileAbsolutPath.getParent() != null) {
                                Files.createDirectories(targeFileAbsolutPath.getParent());
                            }

                            try {
                                long bytesExtracted = extractor.extractCurrentEntry(targeFileAbsolutPath,
                                        globalProcessedBytes, totalBytes, matchedDto.getRelativePath(),
                                        callback, isCancelled, processedFileCount);

                                successfullyRecoveredCount++;
                                processedFileCount++;
                                globalProcessedBytes += bytesExtracted;
                                anyFileProcessed = true;
                                callback.onItemCompleted(true, matchedDto);

                            } catch (Exception e) {
                                e.printStackTrace();
                                Files.deleteIfExists(targeFileAbsolutPath);
                                callback.onItemCompleted(false, matchedDto);
                                if (isCancelled.get()) {
                                    wasCancelled = true;
                                    isBrokenByCancel = true;
                                    break;
                                }
                            }

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                for (BackupDatabaseDTO dto : mapItemList) {
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
            boolean keepPartialRecovery = false ;
            java.util.concurrent.FutureTask<Boolean> dialogTask = new java.util.concurrent.FutureTask<>(() -> {
                return FilesUtility.keepPartialCopyDialogWindow(
                        "Obnova přerušena",
                        "Obnova dat byla přerušena uživatelem. ",
                        "Část souborů již byla úspěšně zkopírována. Chcete tyto soubory ponechat " +
                                "(částečná obnova), nebo chcete celou tuto nedokončenou operaci zrušit a vytvořenou složku smazat?"
                );
            });
            Platform.runLater(dialogTask);
            try {
                // Pracovní vlákno se zde zastaví a čeká na kliknutí uživatele
                keepPartialRecovery = dialogTask.get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!keepPartialRecovery) {
                FilesUtility.deleteDirectoryRecursively(targetDirectory);
            }
        }
    }

        private void copyFileWithProgress (Path sourceAbsolutPath, Path targetAbsolutPath,
        long alreadyProcessedGlobal, long totalBytes,
        String currentFileRelativePath, FilesUtility.ProgressCallback callback,
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