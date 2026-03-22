package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.components.JournalItemController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;

public class JournalController {

    @FXML private ListView<JournalItemDataPack> lvJournalContainer;

    private TaskModel taskModel;

    public JournalController() {
    }


    @FXML
    private void initialize() {
        lvJournalContainer.setCellFactory(listView -> new ListCell<>() {
            // Proměnné pro držení FXML grafiky a Controlleru pro konkrétní buňku
            private Node fxmlRoot;
            private JournalItemController itemController;

            {
                try { // načtení kontroleru a vzhledu komponenty
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/sitina/bakalarskaprace" +
                            "/zalohovyandel/fxml/components" +
                            "/jurnalItem.fxml"));
                    fxmlRoot = loader.load();
                    itemController = loader.getController();
                } catch (IOException e) {
                    ErrorUtility.showError_WindowLoading(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void updateItem(JournalItemDataPack item, boolean empty) {
                super.updateItem(item, empty);

                this.setStyle("-fx-background-color: transparent; " +
                        "-fx-padding: 3;");

                if (empty) {
                    setGraphic(null);
                } else {
                    itemController.setItem(this.getItem());
                    setGraphic(fxmlRoot);
                }
            }
        });
    }



    @FXML
    public void deleteAllItems() {

    }

    @FXML void closeWindow_Save(ActionEvent event) {

    }

    @FXML
    public void  closeWindow_NoSave(ActionEvent event) {

    }



    public void setTaskModel(TaskModel taskModel) {
        this.taskModel = taskModel;
        if (taskModel.getJurnalInfoList().isEmpty()) {
            Label label = new Label("Historie je prázdná!");
            label.getStyleClass().add("DarkBoltHeader1");
            lvJournalContainer.placeholderProperty().setValue(label);
        } else {
            for (JournalItemDataPack item: taskModel.getJurnalInfoList()) {
                lvJournalContainer.getItems().add(item);
            }
        }

    }
}
