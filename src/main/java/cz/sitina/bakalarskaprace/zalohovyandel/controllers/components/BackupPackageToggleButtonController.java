package cz.sitina.bakalarskaprace.zalohovyandel.controllers.components;

import cz.sitina.bakalarskaprace.zalohovyandel.services.DatabaseManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.sql.SQLException;
import java.util.List;

public class BackupPackageToggleButtonController {

    @FXML private Label lbName;
    @FXML private Label lbDate;
    @FXML private Label lbTime;


    public void loadContent(String packageName, String date, String time) {
        this.lbName.setText(packageName);
        this.lbDate.setText(date);
        this.lbTime.setText(time);
    }


}
