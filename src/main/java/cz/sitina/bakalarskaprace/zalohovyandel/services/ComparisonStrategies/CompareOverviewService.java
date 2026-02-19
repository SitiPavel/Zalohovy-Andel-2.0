package cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;

public class CompareOverviewService {
    int leftAddCount = 0;
    int leftUpdateCount = 0;
    int leftDeleteCount = 0;
    int blockedCount = 0;
    int equalsCount = 0;
    int unknownCount = 0;
    int rightDeleteCount = 0;
    int rightUpdateCount = 0;
    int rightAddCount = 0;


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
