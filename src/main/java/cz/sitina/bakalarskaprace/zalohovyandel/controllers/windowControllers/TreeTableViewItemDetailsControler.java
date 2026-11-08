package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices.TreeTableGuiModify;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class TreeTableViewItemDetailsControler {

    @FXML private Label lbHeader;
    @FXML private VBox leftDetailsConteiner;
    @FXML private Line lineOperationLeft;
    @FXML private ImageView imgOperationIdentifikator;
    @FXML private Line lineOperationRight;
    @FXML private VBox rightDetailsConteiner;

    private final TreeTableGuiModify guiModify = new TreeTableGuiModify();
    private final List<Node> staticLeftHeaderNodes = new ArrayList<>();
    private final List<Node> staticRightHeaderNodes = new ArrayList<>();

    public record DetailItem(String label, String value, boolean isDifferent) {}

    @FXML
    public void initialize() {
        // OPRAVENO: Uložení statického záhlaví (Text a Line), aby se při naplnění dat nemazalo
        staticLeftHeaderNodes.addAll(leftDetailsConteiner.getChildren());
        staticRightHeaderNodes.addAll(rightDetailsConteiner.getChildren());
    }

    public void setHeader(String header) {
        if (lbHeader != null) {
            lbHeader.setText(header);
        }
    }

    public void setOperation(CompareOperations operation) {
        if (operation == null) {
            hideOperation();
            return;
        }
        // OPRAVENO: Použití velkých verzí ikon (*_lrg.png) pro operace souborů
        Image icon = guiModify.getLargeIconForOperation(operation);
        if (icon != null) {
            imgOperationIdentifikator.setImage(icon);
            setOperationVisible(true);
        } else {
            hideOperation();
        }
    }

    public void hideOperation() {
        setOperationVisible(false);
    }

    private void setOperationVisible(boolean visible) {
        imgOperationIdentifikator.setVisible(visible);
        imgOperationIdentifikator.setManaged(visible);
        if (lineOperationLeft != null) {
            lineOperationLeft.setVisible(visible);
            lineOperationLeft.setManaged(visible);
        }
        if (lineOperationRight != null) {
            lineOperationRight.setVisible(visible);
            lineOperationRight.setManaged(visible);
        }
    }

    public void populateDetails(List<DetailItem> leftItems, List<DetailItem> rightItems) {
        // OPRAVENO: Zachování hlaviček sloupců z Vzor.fxml a dynamické přidání řádků
        leftDetailsConteiner.getChildren().setAll(staticLeftHeaderNodes);
        rightDetailsConteiner.getChildren().setAll(staticRightHeaderNodes);

        if (leftItems != null) {
            for (DetailItem item : leftItems) {
                leftDetailsConteiner.getChildren().add(createPropertyHBox(item));
            }
        }

        if (rightItems != null) {
            for (DetailItem item : rightItems) {
                rightDetailsConteiner.getChildren().add(createPropertyHBox(item));
            }
        }
    }

    // OPRAVENO: Struktura odpovídající Vzor.fxml (HBox, Label darkBoltText, Label darkText s wrapText)
    private Node createPropertyHBox(DetailItem item) {
        HBox rowBox = new HBox();
        rowBox.setPadding(new Insets(3, 0, 0, 0));

        Label label = new Label(item.label() + ":");
        label.getStyleClass().add("darkBoltText");
        label.setMaxWidth(Double.NEGATIVE_INFINITY);
        label.setMinWidth(Double.NEGATIVE_INFINITY);
        label.setPadding(new Insets(0, 5, 0, 0));
        HBox.setHgrow(label, Priority.ALWAYS);

        Label value = new Label(item.value());
        value.getStyleClass().add("darkText");
        value.setMaxWidth(Double.MAX_VALUE);
        value.setWrapText(true);
        HBox.setHgrow(value, Priority.ALWAYS);

        if (item.isDifferent()) {
            value.setStyle("-fx-text-fill: -fx-color-mark2; -fx-font-weight: bold;");
        }

        rowBox.getChildren().addAll(label, value);
        return rowBox;
    }

    @FXML
    public void closeWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}
