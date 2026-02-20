package cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class CompareOverviewService {



    public CompareOverviewService() {
    }

    public File findDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Vyber složku pro zálohu/synchronizaci");

        Node source = (Node) event.getSource();
        Window stage = source.getScene().getWindow();

        return directoryChooser.showDialog(stage);
    }

}
