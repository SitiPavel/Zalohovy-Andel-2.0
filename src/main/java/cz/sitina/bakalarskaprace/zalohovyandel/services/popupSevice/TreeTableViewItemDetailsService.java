package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.CompareProgresController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.TreeTableViewItemDetailsControler;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.TreeTableViewItemDetailsControler.DetailItem;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TreeItem;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TreeTableViewItemDetailsService {

    private final Utility utility = new Utility();

    private record PreparedDetailsData(
            String headerTitle,
            CompareOperations operation,
            boolean isMulti,
            List<DetailItem> leftDetails,
            List<DetailItem> rightDetails
    ) {}

    /**
     * Otevře okno detailů pro jeden vybraný uzel (soubor nebo složku).
     */
    public void showSingleItemDetails(TreeItem<ComparedItemOperItemRowModel> treeItem) {
        if (treeItem == null || treeItem.getValue() == null) {
            return;
        }

        executeWithOptionalProgressDialog("Načítání detailu položky...", () -> {
            ComparedItemOperItemRowModel row = treeItem.getValue();
            boolean isFolder = row.isFolder();

            if (isFolder) {
                return calculateSingleFolderDetails(treeItem);
            } else {
                return calculateSingleFileDetails(row);
            }
        });
    }

    /**
     * Otevře okno statistiky pro hromadný výběr uzlů.
     */
    public void showMultiItemsDetails(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems) {
        if (selectedItems == null || selectedItems.isEmpty()) {
            return;
        }

        executeWithOptionalProgressDialog("Počítání statistiky vybraných položek...", () -> {
            return calculateMultiItemsDetails(selectedItems);
        });
    }

    // =========================================================================
    // ==================== VÝPOČET PRO JEDNOTLIVÝ SOUBOR =======================
    // =========================================================================

    private PreparedDetailsData calculateSingleFileDetails(ComparedItemOperItemRowModel row) {
        FileItemModel left = row.getLeftItem();
        FileItemModel right = row.getRightItem();

        boolean hasLeft = (left != null && left.getFileAbsolutePath() != null);
        boolean hasRight = (right != null && right.getFileAbsolutePath() != null);

        // Porovnání parametrů pro zvýraznění
        boolean diffSize = false;
        boolean diffModified = false;

        if (hasLeft && hasRight) {
            diffSize = (left.getFileSize() != right.getFileSize());
            diffModified = (left.getLastModified() != right.getLastModified());
        }

        List<DetailItem> leftList = new ArrayList<>();
        List<DetailItem> rightList = new ArrayList<>();

        // Levá strana
        if (hasLeft) {
            leftList.add(new DetailItem("Cesta", left.getFileAbsolutePath().toString(), false));
            leftList.add(new DetailItem("Název", left.getFileName(), false));
            String dateFormatted = formatTimestamp(left.getLastModified());
            leftList.add(new DetailItem("Poslední úprava", dateFormatted, diffModified));
            String sizeFormatted = utility.getFormatedSize(left.getFileSize());
            leftList.add(new DetailItem("Velikost", sizeFormatted, diffSize));
        } else {
            leftList.add(new DetailItem("Stav", "Položka na levé straně neexistuje", false));
            leftList.add(new DetailItem("Cesta", "---", false));
            leftList.add(new DetailItem("Název", "---", false));
            leftList.add(new DetailItem("Poslední úprava", "---", false));
            leftList.add(new DetailItem("Velikost", "---", false));
        }

        // Pravá strana
        if (hasRight) {
            rightList.add(new DetailItem("Cesta", right.getFileAbsolutePath().toString(), false));
            rightList.add(new DetailItem("Název", right.getFileName(), false));
            String dateFormatted = formatTimestamp(right.getLastModified());
            rightList.add(new DetailItem("Poslední úprava", dateFormatted, diffModified));
            String sizeFormatted = utility.getFormatedSize(right.getFileSize());
            rightList.add(new DetailItem("Velikost", sizeFormatted, diffSize));
        } else {
            rightList.add(new DetailItem("Stav", "Položka na pravé straně neexistuje", false));
            rightList.add(new DetailItem("Cesta", "---", false));
            rightList.add(new DetailItem("Název", "---", false));
            rightList.add(new DetailItem("Poslední úprava", "---", false));
            rightList.add(new DetailItem("Velikost", "---", false));
        }

        return new PreparedDetailsData("Porovnání souboru", row.getOperation(), false, leftList, rightList);
    }

    // =========================================================================
    // ==================== VÝPOČET PRO JEDNOTLIVOU SLOŽKU ======================
    // =========================================================================

    private PreparedDetailsData calculateSingleFolderDetails(TreeItem<ComparedItemOperItemRowModel> folderItem) {
        ComparedItemOperItemRowModel row = folderItem.getValue();
        FileItemModel left = row.getLeftItem();
        FileItemModel right = row.getRightItem();

        boolean hasLeft = (left != null && left.getFileAbsolutePath() != null);
        boolean hasRight = (right != null && right.getFileAbsolutePath() != null);

        // Rekurzivní spočítání z paměti stromu
        FolderStats leftStats = new FolderStats();
        FolderStats rightStats = new FolderStats();
        aggregateFolderChildren(folderItem, leftStats, rightStats);

        boolean diffSize = (hasLeft && hasRight && leftStats.totalSize != rightStats.totalSize);
        boolean diffFiles = (hasLeft && hasRight && leftStats.filesCount != rightStats.filesCount);
        boolean diffFolders = (hasLeft && hasRight && leftStats.foldersCount != rightStats.foldersCount);

        List<DetailItem> leftList = new ArrayList<>();
        List<DetailItem> rightList = new ArrayList<>();

        if (hasLeft) {
            leftList.add(new DetailItem("Cesta", left.getFileAbsolutePath().toString(), false));
            leftList.add(new DetailItem("Název", getFolderName(left), false));
            leftList.add(new DetailItem("Počet souborů", String.valueOf(leftStats.filesCount), diffFiles));
            leftList.add(new DetailItem("Počet podsložek", String.valueOf(leftStats.foldersCount), diffFolders));
            leftList.add(new DetailItem("Celková velikost", utility.getFormatedSize(leftStats.totalSize), diffSize));
        } else {
            leftList.add(new DetailItem("Stav", "Složka na levé straně neexistuje", false));
            leftList.add(new DetailItem("Cesta", "---", false));
            leftList.add(new DetailItem("Název", "---", false));
            leftList.add(new DetailItem("Počet souborů", "---", false));
            leftList.add(new DetailItem("Počet podsložek", "---", false));
            leftList.add(new DetailItem("Celková velikost", "---", false));
        }

        if (hasRight) {
            rightList.add(new DetailItem("Cesta", right.getFileAbsolutePath().toString(), false));
            rightList.add(new DetailItem("Název", getFolderName(right), false));
            rightList.add(new DetailItem("Počet souborů", String.valueOf(rightStats.filesCount), diffFiles));
            rightList.add(new DetailItem("Počet podsložek", String.valueOf(rightStats.foldersCount), diffFolders));
            rightList.add(new DetailItem("Celková velikost", utility.getFormatedSize(rightStats.totalSize), diffSize));
        } else {
            rightList.add(new DetailItem("Stav", "Složka na pravé straně neexistuje", false));
            rightList.add(new DetailItem("Cesta", "---", false));
            rightList.add(new DetailItem("Název", "---", false));
            rightList.add(new DetailItem("Počet souborů", "---", false));
            rightList.add(new DetailItem("Počet podsložek", "---", false));
            rightList.add(new DetailItem("Celková velikost", "---", false));
        }

        return new PreparedDetailsData("Porovnání složky", row.getOperation(), false, leftList, rightList);
    }

    private static class FolderStats {
        long totalSize = 0;
        int filesCount = 0;
        int foldersCount = 0;
    }

    private void aggregateFolderChildren(TreeItem<ComparedItemOperItemRowModel> current,
                                         FolderStats leftStats, FolderStats rightStats) {
        if (current == null) return;

        for (TreeItem<ComparedItemOperItemRowModel> child : current.getChildren()) {
            if (child.getValue() == null) continue;
            ComparedItemOperItemRowModel childRow = child.getValue();

            boolean isChildFolder = childRow.isFolder();
            FileItemModel childLeft = childRow.getLeftItem();
            FileItemModel childRight = childRow.getRightItem();

            if (isChildFolder) {
                if (childLeft != null && childLeft.getFileAbsolutePath() != null) {
                    leftStats.foldersCount++;
                }
                if (childRight != null && childRight.getFileAbsolutePath() != null) {
                    rightStats.foldersCount++;
                }
                aggregateFolderChildren(child, leftStats, rightStats);
            } else {
                if (childLeft != null && childLeft.getFileAbsolutePath() != null) {
                    leftStats.filesCount++;
                    leftStats.totalSize += Math.max(0, childLeft.getFileSize());
                }
                if (childRight != null && childRight.getFileAbsolutePath() != null) {
                    rightStats.filesCount++;
                    rightStats.totalSize += Math.max(0, childRight.getFileSize());
                }
            }
        }
    }

    // =========================================================================
    // ==================== VÝPOČET PRO MULTIPLE SELECT ========================
    // =========================================================================

    private PreparedDetailsData calculateMultiItemsDetails(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems) {
        int totalRows = selectedItems.size();
        int leftFiles = 0;
        int leftFolders = 0;
        long leftSize = 0;

        int rightFiles = 0;
        int rightFolders = 0;
        long rightSize = 0;

        for (TreeItem<ComparedItemOperItemRowModel> item : selectedItems) {
            if (item == null || item.getValue() == null) continue;
            ComparedItemOperItemRowModel row = item.getValue();
            boolean isFolder = row.isFolder();

            FileItemModel left = row.getLeftItem();
            FileItemModel right = row.getRightItem();

            if (isFolder) {
                // Spočítáme podsložky i vnořené soubory
                FolderStats fLeft = new FolderStats();
                FolderStats fRight = new FolderStats();
                aggregateFolderChildren(item, fLeft, fRight);

                if (left != null && left.getFileAbsolutePath() != null) {
                    leftFolders += (1 + fLeft.foldersCount);
                    leftFiles += fLeft.filesCount;
                    leftSize += fLeft.totalSize;
                }
                if (right != null && right.getFileAbsolutePath() != null) {
                    rightFolders += (1 + fRight.foldersCount);
                    rightFiles += fRight.filesCount;
                    rightSize += fRight.totalSize;
                }
            } else {
                if (left != null && left.getFileAbsolutePath() != null) {
                    leftFiles++;
                    leftSize += Math.max(0, left.getFileSize());
                }
                if (right != null && right.getFileAbsolutePath() != null) {
                    rightFiles++;
                    rightSize += Math.max(0, right.getFileSize());
                }
            }
        }

        boolean diffSize = (leftSize != rightSize);
        boolean diffFiles = (leftFiles != rightFiles);
        boolean diffFolders = (leftFolders != rightFolders);

        List<DetailItem> leftList = new ArrayList<>();
        leftList.add(new DetailItem("Vybrané řádky", String.valueOf(totalRows), false));
        leftList.add(new DetailItem("Počet souborů", String.valueOf(leftFiles), diffFiles));
        leftList.add(new DetailItem("Počet složek", String.valueOf(leftFolders), diffFolders));
        leftList.add(new DetailItem("Celková velikost", utility.getFormatedSize(leftSize), diffSize));

        List<DetailItem> rightList = new ArrayList<>();
        rightList.add(new DetailItem("Vybrané řádky", String.valueOf(totalRows), false));
        rightList.add(new DetailItem("Počet souborů", String.valueOf(rightFiles), diffFiles));
        rightList.add(new DetailItem("Počet složek", String.valueOf(rightFolders), diffFolders));
        rightList.add(new DetailItem("Celková velikost", utility.getFormatedSize(rightSize), diffSize));

        return new PreparedDetailsData("Statistika výběru", null, true, leftList, rightList);
    }

    // =========================================================================
    // ==================== ASYNCHRONNÍ BĚH S TIMEREM PRO PROGRESS =============
    // =========================================================================

    @FunctionalInterface
    private interface DataCalculator {
        PreparedDetailsData calculate();
    }

    private void executeWithOptionalProgressDialog(String progressHeader, DataCalculator calculator) {
        AtomicBoolean isFinished = new AtomicBoolean(false);
        final Stage[] progressStageHolder = new Stage[1];

        Task<PreparedDetailsData> computationTask = new Task<>() {
            @Override
            protected PreparedDetailsData call() {
                return calculator.calculate();
            }
        };

        // Časovač na 1 sekundu - pokud proces ještě běží, otevře se progress okno
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}

            if (!isFinished.get() && computationTask.isRunning()) {
                Platform.runLater(() -> {
                    if (!isFinished.get() && computationTask.isRunning()) {
                        try {
                            FXMLLoader progressLoader = WindowManager.loadFXML("compareProgresWindow.fxml");
                            CompareProgresController progressController = progressLoader.getController();
                            Stage stage = WindowManager.openRunningWindow(progressLoader, "Načítání", 420, 240);
                            progressStageHolder[0] = stage;

                            stage.setOnCloseRequest(e -> {
                                if (computationTask.isRunning()) {
                                    computationTask.cancel();
                                }
                            });

                            progressController.startIndeterminateTask(computationTask, progressHeader);
                        } catch (IOException e) {
                            System.err.println("Chyba při otevírání progress okna: " + e.getMessage());
                        }
                    }
                });
            }
        });

        computationTask.setOnSucceeded(e -> {
            isFinished.set(true);
            if (progressStageHolder[0] != null && progressStageHolder[0].isShowing()) {
                progressStageHolder[0].close();
            }

            PreparedDetailsData data = computationTask.getValue();
            if (data != null) {
                openDetailsWindow(data);
            }
        });

        computationTask.setOnFailed(e -> {
            isFinished.set(true);
            if (progressStageHolder[0] != null && progressStageHolder[0].isShowing()) {
                progressStageHolder[0].close();
            }
            Throwable ex = computationTask.getException();
            ErrorUtility.getInstance(
                    "Chyba při načítání detailu",
                    "Nepodařilo se spočítat detaily",
                    ex != null ? ex.getMessage() : "Neznámá chyba"
            );
        });

        Thread.ofVirtual().start(computationTask);
    }

    private void openDetailsWindow(PreparedDetailsData data) {
        try {
            FXMLLoader loader = WindowManager.loadFXML("TreeTableViewItemDetailsWindow.fxml");
            TreeTableViewItemDetailsControler controller = loader.getController();

            controller.setHeader(data.headerTitle);
            if (data.isMulti) {
                controller.hideOperation();
            } else {
                controller.setOperation(data.operation);
            }

            controller.populateDetails(data.leftDetails, data.rightDetails);
            WindowManager.openNewWindowAutoSized(loader, data.headerTitle);
        } catch (IOException e) {
            ErrorUtility.getInstance(
                    "Chyba při otevírání okna",
                    "Nepodařilo se zobrazit okno detailů",
                    e.getMessage()
            );
        }
    }

    private String getFolderName(FileItemModel item) {
        if (item.getFileName() != null && !item.getFileName().isBlank()) {
            return item.getFileName();
        }
        if (item.getFileAbsolutePath() != null && item.getFileAbsolutePath().getFileName() != null) {
            return item.getFileAbsolutePath().getFileName().toString();
        }
        return "---";
    }

    private String formatTimestamp(long millis) {
        if (millis <= 0) return "---";
        return Utility.getDateOnly(String.valueOf(millis));
    }
}
