package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.PackageContentModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableShowPackageFilesService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import java.sql.SQLException;
import java.util.List;

public class InsidePackageContentController {

    @FXML private TreeTableView treeTableView;
    @FXML private TreeTableColumn<PackageContentModel, String > colName;

    private TreeItem<PackageContentModel> rootItem = new TreeItem<>(new PackageContentModel("","", true));
    private Utility utility = new Utility();
    private TreeTableGuiModify treeTableGuiModify = new TreeTableGuiModify();



    @FXML
    private void initialize() {
        colName.setCellValueFactory(cellData -> {
            PackageContentModel model = cellData.getValue().getValue();
            return new SimpleObjectProperty<>(model.getFileName());
        });


        Label tablePlaceholder = new Label("Datový balíček neobsahuje soubory!");
        tablePlaceholder.getStyleClass().add("DarkBoltHeader1");
        treeTableView.setPlaceholder(tablePlaceholder);

        treeTableView.setRoot(rootItem);
        treeTableView.setShowRoot(false);
        rootItem.setExpanded(true);

        colName.setCellFactory(CellData ->
                new TreeTableCell<PackageContentModel, String>() {

            Tooltip tooltip = utility.createToolTip_600ms();
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

                    view = treeTableGuiModify.getIconByIsFolder(
                            getTreeTableRow().getItem().isFolder()
                    );
                    setGraphic(view);
                }


            }
        });


    }

    @FXML
    private void closeWindow(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }

    public void loadContentToTable(String databasePath, String packageName, String packageNameGUI) {
        colName.setText(packageNameGUI);

        try (DatabaseManager databaseManager = new DatabaseManager(databasePath)) {
            List<String> relativePaths;
            relativePaths = databaseManager.getPackageContent(packageName);
            TreeTableShowPackageFilesService.buildTreeTablePackageFiles(rootItem, relativePaths);

        } catch (SQLException e){
            e.printStackTrace();
            ErrorUtility.showError_databaseError("Soubor databáze je poškozený nebo k němu aplikace ztratila " +
                    "přístup.");
        } catch (Exception e){
            e.printStackTrace();
        }

    }


}
