package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BackupDatabaseDTO;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class BackupProcessService {

    int successfullyPackagedCount = 0;


    // zjistí nám do jakého balíčku máme dát dat (fullBackup1, increment1 nebo increment 23 )
    public void executeBackupProcess(List<ComparedItemOperItemRowModel> comparedFilesList,
                                          BackupTask editableBackupTask) {
        List<ComparedItemOperItemRowModel> filesToBackup = comparedFilesList.stream()
                .filter(row -> row.getOperation() == CompareOperations.ADD_RIGHT ||
                                                           row.getOperation() == CompareOperations.UPDATE_RIGHT)
                .collect(Collectors.toList());

        if (filesToBackup.isEmpty()) {
            System.out.println("Záloha není nutná.");
            return;
        }

        try (DatabaseManager databaseManager = new DatabaseManager(editableBackupTask.getBackupDatabasePath())) {

            int dataPackageCount = databaseManager.getPackageCount();

            String packName;
            if (dataPackageCount == 0) {
                packName = "FullBackup_1";
            } else {
                packName = "Increment_" + dataPackageCount;
            }

            Path packageAbsolutePath = Paths.get(editableBackupTask.getRootBackupPath() +"\\"+ packName);
            Files.createDirectories(packageAbsolutePath);

            for (ComparedItemOperItemRowModel row : filesToBackup) {
                FileItemModel leftItem = row.getLeftItem();
                Path sourceAbsolutPath = leftItem.getFileAbsolutePath();

                if (!Files.isReadable(sourceAbsolutPath)) {
                    continue;
                }

                try {
                    Path targetAbsolutPath = packageAbsolutePath.resolve(leftItem.getRelativePath());

                    if (targetAbsolutPath.getParent() != null) {
                        Files.createDirectories(targetAbsolutPath.getParent());
                    }
                    Files.copy(sourceAbsolutPath, targetAbsolutPath);

                    BackupDatabaseDTO copiedItem = new BackupDatabaseDTO(
                            targetAbsolutPath.toString(),
                            leftItem.getRelativePath().toString(),
                            leftItem.getFileSize(),
                            leftItem.getLastModified(),
                            packName

                    );

                    databaseManager.insertNewItem(copiedItem);
                    successfullyPackagedCount++;

                } catch (IOException | SQLException e) {
                    System.err.println("Chyba při zpracování: " + sourceAbsolutPath);
                }


            }
            System.out.println("Uloženo " + successfullyPackagedCount + " souborů do balíčku/složky: " + packName);

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



}
