module cz.sitina.bakalarskaprace.zalohovyandel {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;

    opens cz.sitina.bakalarskaprace.zalohovyandel.models to com.fasterxml.jackson.databind;
    opens cz.sitina.bakalarskaprace.zalohovyandel.models.taskType to com.fasterxml.jackson.databind;
    opens cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks to com.fasterxml.jackson.databind;
    opens cz.sitina.bakalarskaprace.zalohovyandel.models.enums to com.fasterxml.jackson.databind;

    opens cz.sitina.bakalarskaprace.zalohovyandel to javafx.fxml;
    opens cz.sitina.bakalarskaprace.zalohovyandel.controllers to javafx.fxml;
    opens cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers to javafx.fxml;
    opens cz.sitina.bakalarskaprace.zalohovyandel.controllers.components to javafx.fxml;

    exports cz.sitina.bakalarskaprace.zalohovyandel;
    opens cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers to javafx.fxml;

}