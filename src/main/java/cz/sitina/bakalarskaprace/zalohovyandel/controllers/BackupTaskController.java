package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.*;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers.DataTransferControllers.BackupRecapitulationBeforeCopyFilesController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.ComparedDataAndStatisticsForCompereOverview;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.GuiOperationFilters_TaskData;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.BackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.EncryptedBackupTask;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.*;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableBuilderService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFilterService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableFolderOperationMakerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.TreeTableUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager.loadFXML;

public class BackupTaskController implements TaskController {

    // třídy poskytující služby
    private IconManagerUtility iconManager = new IconManagerUtility();
    private JsonDataManagerService jsonManager = JsonDataManagerService.getInstance();;
    private final PopupDialogService popupDialogService = new PopupDialogService();
    private Utility utility = new Utility();
    private FileScannerService fileScannerService = new FileScannerService();
    private ComparisonService comparisonService = new ComparisonService();


    private TreeTableGuiModify treeTableGuiModify = new TreeTableGuiModify();
    private TreeTableBuilderService treeBuilder = new TreeTableBuilderService();

    private EncryptedBackupTask encryptedBackupTask;
    private SecretKey sessionKey;


    // objekty
    private TaskRepository taskRepository;
    private BackupTask originBackupTask;
    private BackupTask editableBackupTask;
    private AppFrameController appFrameController;
    private TreeItem<ComparedItemOperItemRowModel> masterRoot;
    private List<ComparedItemOperItemRowModel> resultComparedList = new ArrayList<>();
    private ComparedDataAndStatisticsForCompereOverview statisticsAndComparedData;



    private BooleanProperty isDirtyFlag = new SimpleBooleanProperty(false);
    private boolean isSourceOperationSizeSmallerThenDestinationFolder;

    // Nastavení sloupců v TreeTableView  a samotná tabulka
    @FXML private TreeTableView<ComparedItemOperItemRowModel> filesTreeTable;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColName;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColSize;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, String> leftColLastModify;
    @FXML private TreeTableColumn<ComparedItemOperItemRowModel, CompareOperations> operationCol;


    /// rostě info o root složkách
    @FXML private Label lbSourceFolderPath;
    @FXML private Label lbLeftUsedSpace;
    @FXML private Label lbLeftTotalSpace;
    @FXML private Label lbLeftFreeSpace;
    @FXML private ProgressBar pbLeftUsedSpace;


    @FXML private Label lbDestinationFolderPath;
    @FXML private Label lbRightUsedSpace;
    @FXML private Label lbRightTotalSpace;
    @FXML private Label lbRightFreeSpace;
    @FXML private ProgressBar pbRightUsedSpace;


    /// definocvané GUI objekty
    @FXML private Button btnCompareFolders;
    @FXML private Button btnSaveTask;


    /// definování ukazatelu a filtrování
    @FXML private Label lbIgnoreCount;
    @FXML private Label lbEqualsCount;
    @FXML private Label lbUnknownCount;
    @FXML private Label lbRightUpdateCount;
    @FXML private Label lbRightAddCount;

    // nutné deklarování pro ovládání rychlích filtrů

    @FXML private Button btnAddItem_right;
    @FXML private Button btnModified_right;
    @FXML private Button btnEqual;
    @FXML private Button btnBlock;
    @FXML private Button btnUnknown;


    @FXML private ImageView imgAddItem_right;
    @FXML private ImageView imgModified_right;
    @FXML private ImageView imgEqual;
    @FXML private ImageView imgBlock;
    @FXML private ImageView imgUnknown;


    // objekty v na levé straně symbolizující cíl

    @FXML private ImageView imgTypeOfBackup;
    @FXML private ImageView imgPieChar_Right;
    @FXML private Label lbPieChar_Right;
    @FXML private Label lbDestinationFolderPathContent;

    @FXML private Button btnLeftRootFilter;
    @FXML private Button btnShowOverview;
    @FXML private Button btnStartRecapitulationForCopy;
    @FXML private Button btnShowJurnal;
    @FXML private Button btnShowBlockList;
    @FXML private Button btnShowPackages;


    @FXML
    private void initialize() {
        btnLeftRootFilter.setTooltip(Utility.createToolTip_600ms("Vyberte složky které má aplikace úplně přeskočit. " +
                "Nebudou se skenovat ani porovnávat, což zrychlí proces. "));

        btnCompareFolders.setTooltip(Utility.createToolTip_600ms("Porovnat zdrojovou s cílovou složku."));
        btnSaveTask.setTooltip(Utility.createToolTip_600ms("Uložit nastavení této úlohy."));
        btnShowOverview.setTooltip(Utility.createToolTip_600ms("Odejít zpět na přehled synchronizačních a zálohových " +
                "úloh."));
        btnStartRecapitulationForCopy.setTooltip(Utility.createToolTip_600ms("zálohovat porovnané složky."));
        btnShowJurnal.setTooltip(Utility.createToolTip_600ms("Souhrn výsledků dokončených záloh."));
        btnShowBlockList.setTooltip(Utility.createToolTip_600ms("Menu pro nastavení ignorovaných souborů. " +
                "Tyto soubory budou v porovnání zobrazeny jako blokované a nebudou zahrnuty do synchronizace."));
        btnShowPackages.setTooltip(Utility.createToolTip_600ms("Zobrazit historii záloh"));
        lbPieChar_Right.setTooltip(Utility.createToolTip_600ms("Celková velikost dat k zápisu na pravou stranu."));


        btnBlock.setTooltip(Utility.createToolTip_600ms("Zapnout/vypnout zobrazení ignorovaných souborů. Tyto soubory" +
                " nebudou zálohovány!"));
        btnEqual.setTooltip(Utility.createToolTip_600ms("Zapnout/vypnout zobrazení totožných souborů."));
        btnUnknown.setTooltip(Utility.createToolTip_600ms("Zapnout/vypnout zobrazení soborů u kterých aplikace nerozeznala vhodnou operaci."));

        btnModified_right.setTooltip(Utility.createToolTip_600ms("Zapnout/vypnout zobrazení upravených soborů které budou zálohovány jako nová verze. "));
        btnAddItem_right.setTooltip(Utility.createToolTip_600ms("Zapnout/vypnout zobrazení souborů které budou " +
                "zálohovány."));


        leftColName.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedName());
        leftColSize.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedSize());
        leftColLastModify.setCellValueFactory(cellData ->
                cellData.getValue().getValue().getLeftItem().getFormatedLastModified());

        operationCol.setCellValueFactory(cellData ->
                cellData.getValue().getValue().operationProperty());

        filesTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label tablePlaceholder = new Label("Není porovnaná zdrojová složka s cílovou, nutno porovnat!");
        tablePlaceholder.getStyleClass().add("DarkBoltHeader1");
        filesTreeTable.setPlaceholder(tablePlaceholder);

        /// NASTAVENÍ SLOUPCŮ
        // Stylování sloupců
        leftColName.getStyleClass().add("left-column");
        leftColSize.getStyleClass().add("center-column");
        leftColLastModify.getStyleClass().add("center-column");
        operationCol.getStyleClass().add("center-column");

        // nastaví jako hlavní zavírací strom sloupec leftColName
        filesTreeTable.setTreeColumn(leftColName);

        // nastavení ROOT
        filesTreeTable.setShowRoot(false);
        masterRoot = new TreeItem<>(new ComparedItemOperItemRowModel(
                new FileItemModel("/"),
                CompareOperations.FOLDER_UNEVEN,
                new FileItemModel("/")) );
        filesTreeTable.setRoot(masterRoot);
        masterRoot.setExpanded(true);


        // nastavení levého sloupce názvů
        leftColName.setCellFactory(cellData ->
                new TreeTableCell<ComparedItemOperItemRowModel, String>() {

                    Tooltip tooltip = utility.createToolTip_600ms();
                    // nastavení obrázku pro oddělení složky a souboru

                    ImageView view = new ImageView();

                    @Override
                    protected void updateItem(String item, boolean empty) {

                        super.updateItem(item, empty);

                        if (empty || item == null) {
                            setTooltip(null);
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item);
                            tooltip.setText(item);
                            setTooltip(tooltip);

                            view = treeTableGuiModify.getIconByRowFormat(
                                    getTreeTableRow().getItem().getOperation()
                            );
                            setGraphic(view);
                        }

                    }
                });

        operationCol.setCellFactory(col  -> {

            TreeTableCell<ComparedItemOperItemRowModel, CompareOperations> cell = new TreeTableCell<>() {

                private final ImageView imageView = treeTableGuiModify.findAndSetForOperationIcon(this);
                Image icon = null;

                @Override
                protected void updateItem(CompareOperations operation, boolean empty) {
                    super.updateItem(operation, empty);

                    if (empty || operation == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        icon = treeTableGuiModify.getIconForOperation(operation);
                        imageView.setImage(icon);

                        setGraphic(imageView);
                        setText(null);
                    }
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty() && event.getButton() == MouseButton.SECONDARY) {

                    // získá list vybraných řádků
                    ObservableList<TreeItem<ComparedItemOperItemRowModel>> selectedItems =
                            filesTreeTable.getSelectionModel().getSelectedItems();
                    // pokud je vybráno více tak změnu operace aplikuje na více
                    if (selectedItems.size() > 1){
                        popupDialogService.showOperationPopupWindow_group(selectedItems, true,
                                ()->  { TreeTableFolderOperationMakerService.getFolderOperation(masterRoot);
                                        readAndSetStatistic();
                        });
                    } else {
                        ComparedItemOperItemRowModel rowItem = cell.getTreeTableRow().getItem();

                        if (rowItem != null) {
                            popupDialogService.showOperationPopupWindow(cell.getTreeTableRow().getTreeItem(), true,
                                    ()-> readAndSetStatistic());
                        }
                    }

                    // Zastavení dalšího probublávání události
                    event.consume();
                }

            });

            return cell;
        });

        // nastavení generování Obarvených řádků podle operace
        treeTableGuiModify.rowFormatingByOperation(filesTreeTable);

        setIsDirtyFlagListener();

    }

    private void emptyMethod(){}

    /// ==============================
    /// definování akcí tlačítek
    @FXML
        private void sourceRootFilter(ActionEvent event) {
        try {
            FXMLLoader loader = loadFXML("RootFolderFilterWindow.fxml");
            RootFolderFilterController controller = loader.getController();
            controller.setOriginalPath(originBackupTask.getLeftPath());
            controller.setEditedPath(editableBackupTask.getLeftPath());
            controller.setEditFilteredFolders(editableBackupTask.getRootFilteredFolders_Source());
            controller.setOriginFilteredFolders(originBackupTask.getRootFilteredFolders_Source());
            controller.setRootPath(editableBackupTask.getLeftPath());
            controller.setIsChanged(() -> {
                markAsDirty();
                resetWindow();
            });


            WindowManager.openNewWindowAutoSized(loader, "Nastavení filtru kořenové složky");

        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML // nastavení porovnání
    private void startSettingsOfComparing(ActionEvent event) {

    }

    @FXML // porovnání souborů
    private void startComparingFiles(ActionEvent event) {
        try {
            FXMLLoader loader = loadFXML("compareProgresWindow.fxml");
            CompareProgresController progressWindowController = loader.getController();
            Stage progresStage = WindowManager.openRunningWindow(loader, "Průběh porovnání", 420, 240);


            Task<ComparedDataAndStatisticsForCompereOverview> comparationTask = new Task<>() {
                @Override
                protected ComparedDataAndStatisticsForCompereOverview call() throws Exception {

                    Platform.runLater(() -> {
                        progressWindowController.setLbHeader("Skenuji zdrojové soubory... 1/3");
                    });

                    Map<String, FileItemModel> sourceMap = fileScannerService.scanFolder(
                                                                  editableBackupTask.getLeftPath(),
                                                                  editableBackupTask.getRootFilteredFolders_Source(),
                                                         false);

                    // Zrušení uživatelem?
                    if (isCancelled()) return null;

                    ///  načítání dat z databáze do Map<>
                    Platform.runLater(() -> {
                        progressWindowController.setLbHeader("Skenuji histori záloh... 2/3");
                    });

                    Map<String, FileItemModel> databaseMap = Map.of();
                    Path rootDirectory = Paths.get(editableBackupTask.getRootBackupPath());
                    try {
                        // Vytvoří složku a všechny nadřazené složky, pokud neexistují. Když existuje tak metoda nic neudělá
                        Files.createDirectories(rootDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Nepodařilo se vytvořit složku");
                    }
                    String dbPath = editableBackupTask.getBackupDatabasePath();

                    try (DatabaseManager databaseManager = new DatabaseManager(dbPath, sessionKey)) {
                        databaseMap = databaseManager.loadLatestIndexToMemory();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Zrušení uživatelem?
                    if (isCancelled()) return null;

                    Platform.runLater(() -> {
                        progressWindowController.setLbHeader("Probíhá porovnání souborů... 3/3");
                    });

                    // Tady se volá porovnání souborů
                    return comparisonService.compareFolders(
                            sourceMap,
                            databaseMap,
                            editableBackupTask,
                            (current, total) -> {
                                updateProgress(current, total);
                                updateMessage(String.valueOf(current));
                                updateTitle(String.valueOf(total));
                            },
                            this::isCancelled
                    );
                }
            };

            progressWindowController.startComparisonWithProgressWindow(comparationTask);

            // 3. Co se stane po úspěšném dokončení
            comparationTask.setOnSucceeded(e -> {
                progresStage.close();
                ComparedDataAndStatisticsForCompereOverview resultData = comparationTask.getValue();

                if (resultData != null) {
                    loadFinishedDataToGui(
                            resultData,
                            lbSourceFolderPath.getText(),
                            editableBackupTask.getRootBackupPath());
                }
            });

            comparationTask.setOnFailed(e -> {
                progresStage.close();
                Throwable exception = comparationTask.getException();
                String message = (exception != null) ? exception.getMessage() : "Neznámá chyba";
                ErrorUtility.getInstance("Kritická chyba", "Chyba během procesu porovnání zálohy.", message);

                if (exception == null) {
                    exception.printStackTrace(); // Tisk do konzole pro debug
                }
            });

            comparationTask.setOnCancelled(e -> progresStage.close());

            Thread thread = new Thread(comparationTask);
            thread.setDaemon(true); // Daemon vlákno se ukončí, když se vypne hlavní okno aplikace
            thread.start();

        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML //
    private void showTaskOverviewGUI(ActionEvent event) {
        sessionKey = null;
        appFrameController.showTasksOverview();
        originBackupTask = null;
        editableBackupTask = null;
    }

    @FXML
    private void saveEditableTask(ActionEvent event) {
        saveChanges();
        isDirtyFlag.set(false);
        jsonManager.saveAllTask_SaveOldSave(taskRepository.getTaskObservableList());
    }

    @FXML
    private void startRecapitulationForCopy(ActionEvent event) {
        // zjistíme zda není porovnáno
        if (masterRoot == null || masterRoot.getChildren() == null || masterRoot.getChildren().isEmpty()) {
            ErrorUtility.getInstance("Chyba seznamu porovnání",
                    "Není vytvořen seznam s konkrétními kroky pro porovnání souborů!",
                    "Nutno levou a pravou složku nejdříve porovnat a až potom synchronizovat!");
            return;
        }
        // zda obsahuje seznam položky, které vyžadují manipulaci
        if (statisticsAndComparedData.isDataMoveOperations()) {

            // zjistíme zda se vejde
            if (!isSourceOperationSizeSmallerThenDestinationFolder) {
                ErrorUtility.getInstance("Chyba disku",
                        "Synchronizace nebo záloha je příliž velká!",
                        "Velikost operací do levého nebo pravého uložiště je příliž velká!");
                return;
            }

            try {
                FXMLLoader loader = loadFXML("backupRecapitulationBeforeCopyFiles.fxml");
                BackupRecapitulationBeforeCopyFilesController controller = loader.getController();
                controller.setComparedAndStatisticsData(statisticsAndComparedData);
                controller.setOriginalBackupTask(originBackupTask);
                controller.setEditableBackupTask(editableBackupTask);
                controller.setEncryptedTask(encryptedBackupTask);
                controller.setSecretKey(sessionKey);
                controller.setComparedFilesList(statisticsAndComparedData.getComparedFilesList());
                controller.setCanBeResetTreeTable(() -> {
                    resetWindow();
                    Label tablePlaceholder = new Label("Proběhla záloha ✔");
                    tablePlaceholder.getStyleClass().add("DarkBoltHeader1");
                    filesTreeTable.setPlaceholder(tablePlaceholder);
                });

                controller.setGui();

                WindowManager.openNewWindowAutoSized(loader, "Rekapitulace");

            } catch (IOException e) {
                ErrorUtility.showError_WindowLoading();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            ErrorUtility.getInstance("Chyba",
                    "V porovnání nejsou položky, vyžadují manipulaci",
                    "Aby mohla být synchronizace/záloha spuštěna musí obsahovat alespoň jednu z těchto operací: " +
                            "PŘIDAT, UPRAVIT, SMAZAT na libovolnou stranu");
        }
    }

    @FXML
    private void showBlockList(ActionEvent event) {
        try {
            FXMLLoader loader = loadFXML("BlockedFoldersFilesOverviewWindow.fxml");
            BlockedFoldersFilesOverviewController controller = loader.getController();
            controller.setTaskModel(editableBackupTask);
            controller.setIsBlockSetChanged(() -> {
                markAsDirty();
            });

            WindowManager.openNewWindow(loader, "Seznam blokovaných souborů a složek", 750, 500);
        }  catch (Exception e) {
            ErrorUtility.getInstance("Kritická chyba",
                    "Nastala chyba okna",
                    "");
            e.printStackTrace();
        }

    }

    @FXML
    private void showJournal(ActionEvent event) {
        try {
            FXMLLoader loader = loadFXML("journalWindow.fxml");
            JournalController controller = loader.getController();
            controller.setEditedTask(editableBackupTask);
            controller.setHeader("Historie dokončených záloh");
            controller.setIsChanged(() -> markAsDirty());

            WindowManager.openNewWindowAutoSized(loader, "Journal");


        } catch (IOException e) {
            ErrorUtility.showError_WindowLoading();
        }
    }

    @FXML
    private void showPackages(ActionEvent event) {
        try {
            FXMLLoader loader  = loadFXML("BackupPackagesOverview.fxml");
            BackupPackagesOverviewController controller = loader.getController();
            controller.setTasks(originBackupTask, editableBackupTask, encryptedBackupTask, sessionKey);


            WindowManager.openNewWindowAutoSized(loader, "Přehled datových balíčků");

        } catch (IOException e) {
            e.printStackTrace();
            ErrorUtility.showError_WindowLoading();
        } catch (Exception e ){
            e.printStackTrace();
        }
    }
    @FXML void setFastFilter(ActionEvent event) {
        Node source = (Node) event.getSource();

        setImageByBtnID(source.getId(), editableBackupTask.getGuiOperationFilters());
        markAsDirty();

        applyCurrentFiltersToTable();
    }

    private void applyCurrentFiltersToTable() {
        // kdyby nebylo co filtrovate
        if (masterRoot == null || editableBackupTask == null) return;

        TreeItem<ComparedItemOperItemRowModel> filteredRoot =
                TreeTableFilterService.createFiltredTree(masterRoot, editableBackupTask.getGuiOperationFilters());
        filesTreeTable.setRoot(filteredRoot);
    }

    /// =======================
    /// Další metody

    public void readAndSetStatistic() {
        if (statisticsAndComparedData == null) {
            return;
        }
        statisticsAndComparedData.resetCounters();
        TreeTableUtility.readStatisticForOperationBackupSize(masterRoot, statisticsAndComparedData);
        loadComputeDataAfterChange(statisticsAndComparedData, lbDestinationFolderPath.getText());
    }

    private void loadFinishedDataToGui(ComparedDataAndStatisticsForCompereOverview data, String sourceFolder,
                                       String destinationFolder) {
        this.statisticsAndComparedData = data;

        // nastavení statistik o disku v GUI
        DiskSpaceStatisticData leftFolderStatistic = utility.getDiskSpaceStatistics(sourceFolder);
        lbLeftUsedSpace.setText(utility.getFormatedSize(leftFolderStatistic.getUsedSpace()));
        lbLeftTotalSpace.setText(utility.getFormatedSize(leftFolderStatistic.getTotalSpace()));
        lbLeftFreeSpace.setText(utility.getFormatedSize(leftFolderStatistic.getFreeSpace()));
        pbLeftUsedSpace.setProgress((double) leftFolderStatistic.getUsedSpace() /leftFolderStatistic.getTotalSpace());
        DiskSpaceStatisticData rightFolderStatistic = utility.getDiskSpaceStatistics(destinationFolder);
        lbRightUsedSpace.setText(utility.getFormatedSize(rightFolderStatistic.getUsedSpace()));
        lbRightTotalSpace.setText(utility.getFormatedSize(rightFolderStatistic.getTotalSpace()));
        lbRightFreeSpace.setText(utility.getFormatedSize(rightFolderStatistic.getFreeSpace()));
        pbRightUsedSpace.setProgress((double) rightFolderStatistic.getUsedSpace() /rightFolderStatistic.getTotalSpace());


        loadComputeDataAfterChange(statisticsAndComparedData, destinationFolder);

        resultComparedList = data.getComparedFilesList();
        masterRoot = new TreeItem<>(new ComparedItemOperItemRowModel(
                new FileItemModel("/"),
                CompareOperations.FOLDER_UNEVEN,
                new FileItemModel("/"))
        );

        masterRoot.setExpanded(true);

        treeBuilder.buildTreeTableItemOperItem(masterRoot, data.getComparedFilesList());


        // nastavíme operace pro složky
        TreeTableFolderOperationMakerService.getFolderOperation(masterRoot);

        applyCurrentFiltersToTable();

    }

    private void loadComputeDataAfterChange(ComparedDataAndStatisticsForCompereOverview data,
                                            String destinationFolder) {
        lbRightAddCount.setText(String.valueOf(data.getRightAddCount()));
        lbRightUpdateCount.setText(String.valueOf(data.getRightUpdateCount()));
        lbEqualsCount.setText(String.valueOf(data.getEqualsCount()));
        lbIgnoreCount.setText(String.valueOf(data.getIgnoreCount()));
        lbUnknownCount.setText(String.valueOf(data.getUnknownCount()));

        if (utility.isSazeSmalerThenFreeSpace(destinationFolder,
                data.getDataTransportSize_right())) {
            imgPieChar_Right.setImage(iconManager.getPieChartYellow_ToRight()) ;
            isSourceOperationSizeSmallerThenDestinationFolder =true;
        } else {
            imgPieChar_Right.setImage(iconManager.getPieChartRed_ToRight());
            isSourceOperationSizeSmallerThenDestinationFolder = false;
        }

        lbPieChar_Right.setText(String.valueOf(
                utility.getFormatedSize(
                        data.getDataTransportSize_right()
                )
        ));
    }

    private void resetWindow() {
        // vynulování ukazatelů
        statisticsAndComparedData = new ComparedDataAndStatisticsForCompereOverview();
        loadFinishedDataToGui(statisticsAndComparedData, "", "");
        //vynulování stromu
        masterRoot.getChildren().clear();
        filesTreeTable.setRoot(masterRoot);
    }


    // nastaví podle true/false obrázek odpovídajcí vypnutému nebo zapnutému filtru
    private void setFastFiltersImg() {
        GuiOperationFilters_TaskData taskFilters = editableBackupTask.getGuiOperationFilters();
        imgAddItem_right.setImage(
                taskFilters.isActive_ADD_RIGHT() ? iconManager.getIcon_addItem_right() : iconManager.getIcon_addItem_right_OFF()
        );
        imgModified_right.setImage(
                taskFilters.isActive_UPDATE_RIGHT() ? iconManager.getIcon_modified_right() : iconManager.getIcon_modified_right_OFF()
        );
        imgEqual.setImage(
                taskFilters.isActive_EQUALS() ? iconManager.getIcon_equal() : iconManager.getIcon_equal_OFF()
        );
        imgBlock.setImage(
                taskFilters.isActive_IGNORE() ? iconManager.getIcon_block() : iconManager.getIcon_block_OFF()
        );
        imgUnknown.setImage(
                taskFilters.isActive_UNKNOWN() ? iconManager.getIcon_unknown() : iconManager.getIcon_unknown_OFF()
        );


    }

    // nastaví proměnou označující změnu v TaskModelu
    private void markAsDirty() {
        if (!isDirtyFlag.get()) {
            isDirtyFlag.set(true);
            System.out.println("nastaveno true");
        }
    }

    private void setIsDirtyFlagListener() {
        isDirtyFlag.addListener((obs, oldVal, newVal) -> {
            String newClass = "modificator_save";
            if (newVal) {
                if (!btnSaveTask.getStyleClass().contains("modificator_save")){
                    btnSaveTask.getStyleClass().add("modificator_save");
                }
            } else {
                btnSaveTask.getStyleClass().remove("modificator_save");
            }
        });
    }

    public boolean isUnsavedChanges() {
        return isDirtyFlag.get();
    }

    public void saveChanges() {
        if (encryptedBackupTask != null && sessionKey != null) {
            try {
                this.encryptedBackupTask.updateEncryptedTask(editableBackupTask, sessionKey);
            } catch (Exception e) {
                ErrorUtility.getInstance("Chyba uložení",
                        "Nastala nečekávaá chyba při uložení nastavení této úlohy.",
                        "Chyba mohla nastat při ukládání úlohy nastavení nebo při šifrování citlivích " +
                                "informací této úlohy");
            }

        } else {
            this.originBackupTask.updateBackupTask( editableBackupTask);
        }
        isDirtyFlag.set(false);
    }

    private void setImageByBtnID(String id, GuiOperationFilters_TaskData filters) {
        switch (id) {
            // --- STŘED ---
            case "btnBlock":
                if (filters.isActive_IGNORE()) {
                    filters.setActive_IGNORE(false);
                    imgBlock.setImage( iconManager.getIcon_block_OFF());
                } else {
                    filters.setActive_IGNORE(true);
                    imgBlock.setImage( iconManager.getIcon_block());
                }
                break;

            case "btnEqual":
                if (filters.isActive_EQUALS()) {
                    filters.setActive_EQUALS(false);
                    imgEqual.setImage( iconManager.getIcon_equal_OFF());
                } else {
                    filters.setActive_EQUALS(true);
                    imgEqual.setImage( iconManager.getIcon_equal());
                }
                break;

            case "btnUnknown":
                if (filters.isActive_UNKNOWN()) {
                    filters.setActive_UNKNOWN(false);
                    imgUnknown.setImage( iconManager.getIcon_unknown_OFF());
                } else {
                    filters.setActive_UNKNOWN(true);
                    imgUnknown.setImage( iconManager.getIcon_unknown());
                }
                break;


            case "btnModified_right":
                if (filters.isActive_UPDATE_RIGHT()) {
                    filters.setActive_UPDATE_RIGHT(false);
                    imgModified_right.setImage( iconManager.getIcon_modified_right_OFF());
                } else {
                    filters.setActive_UPDATE_RIGHT(true);
                    imgModified_right.setImage( iconManager.getIcon_modified_right());
                }
                break;

            case "btnAddItem_right":
                if (filters.isActive_ADD_RIGHT()) {
                    filters.setActive_ADD_RIGHT(false);
                    imgAddItem_right.setImage( iconManager.getIcon_addItem_right_OFF());
                } else {
                    filters.setActive_ADD_RIGHT(true);
                    imgAddItem_right.setImage( iconManager.getIcon_addItem_right());
                }
                break;
        }
    }

    // setry

    public void loadView() {
        lbSourceFolderPath.setText(originBackupTask.getLeftPath());
        lbDestinationFolderPath.setText(originBackupTask.getRightPath());
        lbDestinationFolderPathContent.setText(originBackupTask.getRightPath());
    }


    public void setTaskModel(TaskModel task) {
        BackupTask backupTask = (BackupTask) task;

        this.originBackupTask = backupTask;
        this.editableBackupTask = new BackupTask(backupTask);

        imgTypeOfBackup.setImage(setRightBackupTypeIcon());

        Tooltip ttSourcePath = utility.createToolTip_300ms(lbSourceFolderPath.getText());
        lbSourceFolderPath.setTooltip(ttSourcePath);
        Tooltip ttDestinationPath = utility.createToolTip_300ms(lbDestinationFolderPath.getText());
        lbDestinationFolderPath.setTooltip(ttDestinationPath);

        setFastFiltersImg();
    }

    private Image setRightBackupTypeIcon() {
        if (originBackupTask.isCompressed() && originBackupTask.isEncrypted()) {
            return iconManager.getBackupTask_Com_Enc_200x200();
        } else if (!originBackupTask.isCompressed() && originBackupTask.isEncrypted()) {
            return iconManager.getBackupTask_X_Enc_200x200();
        }else if (originBackupTask.isCompressed() && !originBackupTask.isEncrypted()) {
            return iconManager.getBackupTask_Com_X_200x200();
        } else {
            return iconManager.getBackupTask_200x200();
        }
    }


    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void setAppFrameController(AppFrameController appFrameController) {
        this.appFrameController = appFrameController;
    }

    public void setEncryptObjects(SecretKey sessionKey, EncryptedBackupTask encryptedBackupTask) {
        this.sessionKey = sessionKey;
        this.encryptedBackupTask = encryptedBackupTask;
    }
}
