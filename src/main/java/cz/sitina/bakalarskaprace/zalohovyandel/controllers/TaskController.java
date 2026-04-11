package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

import cz.sitina.bakalarskaprace.zalohovyandel.models.repositories.TaskRepository;
import cz.sitina.bakalarskaprace.zalohovyandel.models.taskType.TaskModel;

public interface TaskController {

    boolean isUnsavedChanges();

    void saveChanges();

    void setTaskModel(TaskModel taskModel);

    void setTaskRepository(TaskRepository repository);

}
