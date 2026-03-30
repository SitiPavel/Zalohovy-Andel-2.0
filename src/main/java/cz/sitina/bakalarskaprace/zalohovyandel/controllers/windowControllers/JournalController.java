package cz.sitina.bakalarskaprace.zalohovyandel.controllers.windowControllers;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.components.JournalItemController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.JournalItemDataPack;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.WindowManager;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JournalController {

    @FXML private ListView<JournalItemDataPack> lvJournalContainer;
    @FXML private Label lbHeader;

    private Runnable isChanged;
    private TaskModel editedTask;
    private List<JournalItemDataPack> copyOfEditedJournal;



    public JournalController() {
    }


    @FXML
    private void initialize() {
        lvJournalContainer.setCellFactory(listView -> new ListCell<>() {
            // Proměnné pro držení FXML grafiky a Controlleru pro konkrétní buňku
            private Node fxmlRoot;
            private JournalItemController itemController;


            {
                Label label = new Label("Historie je prázdná!");
                label.getStyleClass().add("DarkBoltHeader1");
                lvJournalContainer.placeholderProperty().setValue(label);

                try { // načtení kontroleru a vzhledu komponenty
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/cz/sitina/bakalarskaprace" +
                            "/zalohovyandel/fxml/components/jurnalItem.fxml"));
                    fxmlRoot = loader.load();
                    itemController = loader.getController();
                    itemController.setCopyOfEditedJournal(copyOfEditedJournal);
                    itemController.setThisTitlePane(lvJournalContainer);

                } catch (IOException e) {
                    ErrorUtility.showError_WindowLoading();
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
        copyOfEditedJournal.clear();
        lvJournalContainer.getItems().clear();
    }

    @FXML void closeWindow_Save(ActionEvent event) {
        if (!copyOfEditedJournal.equals(editedTask)) {
            isChanged.run();
            editedTask.updateAllJournalList(copyOfEditedJournal);
        }

        WindowManager.findByActionAndClose(event);
    }

    @FXML
    public void  closeWindow_NoSave(ActionEvent event) {
        WindowManager.findByActionAndClose(event);
    }



    public void setEditedTask(TaskModel editedTask) {
        this.editedTask = editedTask;
        this.copyOfEditedJournal = new ArrayList<>(editedTask.getJurnalInfoList());
        if (!copyOfEditedJournal.isEmpty()) {
            for (JournalItemDataPack item: copyOfEditedJournal) {
                lvJournalContainer.getItems().add(0,item);
            }
        }

    }
    public void setIsChanged(Runnable isChanged) {
        this.isChanged = isChanged;
    }

    public void setHeader(String header) {
        lbHeader.setText(header);
    }
}

