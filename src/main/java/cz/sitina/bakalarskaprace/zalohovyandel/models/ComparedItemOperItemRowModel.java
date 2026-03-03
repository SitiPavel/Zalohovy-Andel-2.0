package cz.sitina.bakalarskaprace.zalohovyandel.models;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ComparedItemOperItemRowModel {

    private ObjectProperty<FileItemModel> leftItem;
    private ObjectProperty<CompareOperations> operation;
    private ObjectProperty<FileItemModel> rightItem;

    public ComparedItemOperItemRowModel(FileItemModel leftItem, CompareOperations operation, FileItemModel rightItem) {
        this.leftItem = new SimpleObjectProperty<>(leftItem);
        this.operation = new SimpleObjectProperty<>(operation);
        this.rightItem = new SimpleObjectProperty<>(rightItem);
    }

    public boolean isFolder() {
        return this.operation.getValue() == CompareOperations.FOLDER;
    }

    ///GETRY
    public ObjectProperty<FileItemModel> getLeftItemProperty() {
        return leftItem;
    }
    public ObjectProperty<CompareOperations> setLeftItemProperty(FileItemModel leftItem) {
        return operation;
    }
    public ObjectProperty<FileItemModel> getRightItemProperty() {
        return rightItem;
    }


    public FileItemModel getLeftItem() {
        return leftItem.get();
    }
    public CompareOperations getOperation() {
        return operation.get();
    }
    public FileItemModel getRightItem() {
        return rightItem.get();
    }


    ///SETRY
    public void setLeftItem(FileItemModel leftItem) {
        this.leftItem.set(leftItem);
    }
    public ObjectProperty<CompareOperations> operationProperty() {
        return operation;
    }
    public void setRightItem(FileItemModel rightItem) {
        this.rightItem.set(rightItem);
    }

}
