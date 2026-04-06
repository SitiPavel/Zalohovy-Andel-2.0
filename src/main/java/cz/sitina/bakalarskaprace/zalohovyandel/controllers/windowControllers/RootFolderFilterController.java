package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.services.FileScannerService;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RootFolderFilterController {
    private String rootPath;

    // od original task
    private List<String> originFilteredFolders;
    // od editable task
    private List<String> editFilteredFolders;
    private List<String> foundedFolders;
    // list pro zjištění odšktrnutých polí
    private List<CheckBox> chackBoxList = new ArrayList<>();
    private FileScannerService fileScannerService =  new FileScannerService();



    // cesty
    private String originalPath;
    private String editedPath;

    //callBack
    private Runnable isChanged;

   @FXML private Label lbRootPath;
   @FXML private VBox childConteiner;

    @FXML
    public void setChanges(ActionEvent event) {



        editFilteredFolders.clear();
        for (CheckBox checkBox : chackBoxList) {
            if (!checkBox.isSelected()) {
                editFilteredFolders.add(checkBox.getUserData().toString());
            }
        }
        if (!originFilteredFolders.equals(editFilteredFolders) && isChanged != null) {
            isChanged.run();
        }

        WindowManager.findByActionAndClose(event);
    }




    public void setRootPath(String path) {
        this.rootPath = path;
        lbRootPath.setText(rootPath);
        lbRootPath.setTooltip(Utility.createToolTip_600ms(rootPath));

        // zíkání cest k souboru
        foundedFolders = fileScannerService.loadDirectSubfolders(Path.of(rootPath));
        loadData();

    }

    private void loadData() {
        for (String absolutPath : foundedFolders) {
            // nastavení chackboxu
            String folderName = Path.of(absolutPath).getFileName().toString();

            CheckBox chkBox = new CheckBox(folderName);
            chkBox.setUserData(absolutPath);
            chkBox.getStyleClass().add("checkBox_styl2");


            // pokud se originální cesta rovná te editované tak zjistíme jestli jí zobrazit jako on/off
            boolean isfiltred = false;
            if (originalPath.equals(editedPath)) {
                for (String path : editFilteredFolders) {
                    if (absolutPath.equals(path)) {
                        isfiltred = true;
                        break;
                    }
                }
            }
            // nastavení zaškrtnutí, je zaškrtnut pokud isfiltred = false
            chkBox.setSelected(!isfiltred);

            chackBoxList.add(chkBox);
            childConteiner.getChildren().add(chkBox);
        }
        if (childConteiner.getChildren().isEmpty()) {
            Label label = new Label("Složka neobsahuje podsložky");
            label.getStyleClass().add("darkBoltText");
            childConteiner.getChildren().add(label);
        }
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public void setEditedPath(String editedPath) {
        this.editedPath = editedPath;
    }

    public void setEditFilteredFolders(List<String> rootFilteredFilter) {
        this.editFilteredFolders = rootFilteredFilter;
    }

    public void setOriginFilteredFolders(List<String> originFilteredFolders) {
        this.originFilteredFolders = originFilteredFolders;
    }

    public void setIsChanged(Runnable isChanged) {
        this.isChanged = isChanged;
    }
}
