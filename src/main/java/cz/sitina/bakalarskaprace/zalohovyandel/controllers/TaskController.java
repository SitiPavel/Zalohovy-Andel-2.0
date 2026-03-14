package cz.sitina.bakalarskaprace.zalohovyandel.controllers;

public interface TaskController {

    boolean isUnsavedChanges();

    void saveChanges();
}
