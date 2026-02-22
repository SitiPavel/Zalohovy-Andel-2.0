package cz.sitina.bakalarskaprace.zalohovyandel.models;

import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.format.TextStyle;

public class TaskRepository {

    private final ObservableList<TaskModel> taskList = FXCollections.observableArrayList();

    public void add(TaskModel task) {
        taskList.add(task);
    }

    public TaskModel getListItem(long id) {
        for (TaskModel task : taskList) {
            if (task.getId() == id) {
                return task;
            }
        }
        return null; /// ERROR doplnit
    }

}
