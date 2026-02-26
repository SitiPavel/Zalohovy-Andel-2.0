module cz.sitina.bakalarskaprace.zalohovyandel {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;

    opens cz.sitina.bakalarskaprace.zalohovyandel to javafx.fxml;
    opens cz.sitina.bakalarskaprace.zalohovyandel.controllers to javafx.fxml;
    opens cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers to javafx.fxml;

    exports cz.sitina.bakalarskaprace.zalohovyandel;
}