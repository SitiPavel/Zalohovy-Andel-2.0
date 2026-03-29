package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.PackageContentModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableShowPackageFilesService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.sql.SQLException;
import java.util.List;

public class InsidePackageContentController {

    @FXML private TreeTableView treeTableView;
    @FXML private TreeTableColumn<PackageContentModel, String > colName;

    private TreeItem<PackageContentModel> rootItem = new TreeItem<>(new PackageContentModel("","", true));



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
