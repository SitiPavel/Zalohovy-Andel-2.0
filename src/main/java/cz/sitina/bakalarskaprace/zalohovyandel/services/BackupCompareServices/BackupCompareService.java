package cz.sitina.bakalarskaprace.zalohovyandel.services.BackupCompareServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies.OneWayToRightStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BackupCompareService {
    private FileScannerService fileScannerService = new FileScannerService();
    private final OneWayToRightStrategy oneWayToRightStrategy = new OneWayToRightStrategy();



    public ComparedDataAndStatisticsForCompereOverview executeBackupCompare(BackupTask backupTask) {
        ComparedDataAndStatisticsForCompereOverview resultData = new ComparedDataAndStatisticsForCompereOverview();


        //pokud je cesta špatně
        if ( backupTask.getLeftPath() == null ||
                backupTask.getLeftPath().isEmpty() ||
                !fileScannerService.isExistsFolderPath(Path.of(backupTask.getLeftPath()) )) {
            return resultData;
        }

        Map<String, FileItemModel> sourceList;
        List<ComparedItemOperItemRowModel> comparedFilesList = new ArrayList<>();


        sourceList = fileScannerService.scanFolder(backupTask.getLeftPath() ,backupTask.getRootFilteredFolders_Source());

        // získání bezpečné cesty s lomátky (kdyby byly nějaké \ a nějaké /)

        Path rootDirectory = Paths.get(backupTask.getRootBackupPath());
        try {
            // Vytvoří složku a všechny nadřazené složky, pokud neexistují. Když existuje tak metoda nic neudělá
            java.nio.file.Files.createDirectories(rootDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Nepodařilo se vytvořit složku");
        }

        String dbPath = Paths.get(backupTask.getRootBackupPath(), "index.db").toString();

        try (DatabaseManager databaseManager = new DatabaseManager(dbPath);) {
            // cesta k databázi


            // rozhodnutí o operaci a vytvoření řádku do listu nutný pro GUI a zálohování
            for (FileItemModel fileItemModel_source : sourceList.values()) {
                FileItemModel fileItemModel_Destination;
                CompareOperations operation;


                Path relativePathObject = fileItemModel_source.getRelativePath();
                // zjisštění zda je v seznamu blokovaných = nutno porovnat cesty aby nebyly naopak lomítka / a \
                Boolean isBlocked = backupTask.getBlockSet().stream()
                        .anyMatch(blockedStr -> relativePathObject.startsWith(Path.of(blockedStr)));

                // pokud je v blokovaných tak ignoruj
                if (isBlocked) {
                    operation = CompareOperations.IGNORE;
                    fileItemModel_Destination = new FileItemModel();
                } else {
                    // zjistí jestli je jiný
                    boolean isChanged = databaseManager.isFileChanged(
                            fileItemModel_source.getRelativePath().toString(),
                            fileItemModel_source.getFileSize(),
                            fileItemModel_source.getLastModified()
                    );

                    if (isChanged) { //je jiný, najdi operaci
                        fileItemModel_Destination =
                                databaseManager.getLatestItemFromDatabase(fileItemModel_source.getRelativePath());
                        operation = oneWayToRightStrategy.compare(fileItemModel_source, fileItemModel_Destination);

                    } else { // je stejný tak EQUALS
                        operation = CompareOperations.EQUALS;
                        fileItemModel_Destination = new FileItemModel();
                    }
                }

                // vytvoření nového řádku
                ComparedItemOperItemRowModel newRow = new ComparedItemOperItemRowModel(
                        fileItemModel_source,
                        operation,
                        fileItemModel_Destination);

                comparedFilesList.add(newRow);
                TreeTableUtility.countFiltersAndSizePie(newRow, resultData);
            }

            resultData.setComparedFilesList(comparedFilesList);
            return resultData;

        } catch (SQLException e) {
            e.printStackTrace();
            ErrorUtility.showError_databaseError("Nastala nečekaná chyba při práci s databází.");
            return new ComparedDataAndStatisticsForCompereOverview();
        }
    }
}
