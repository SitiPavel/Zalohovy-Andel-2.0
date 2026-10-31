package cz.sitina.bakalarskaprace.zalohovyandel.models;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.nio.file.Path;

public class ComparedItemOperItemRowModel {

    private final ObjectProperty<FileItemModel> leftItem;
    private final ObjectProperty<CompareOperations> operation;
    private final ObjectProperty<FileItemModel> rightItem;
    private final BooleanProperty isDirectory;

    public ComparedItemOperItemRowModel(FileItemModel leftItem, CompareOperations operation, FileItemModel rightItem) {
        this(leftItem, operation, rightItem, isFolderOperation(operation));
    }

    public ComparedItemOperItemRowModel(FileItemModel leftItem, CompareOperations operation, FileItemModel rightItem, boolean isDirectory) {
        this.leftItem = new SimpleObjectProperty<>(leftItem);
        this.operation = new SimpleObjectProperty<>(operation);
        this.rightItem = new SimpleObjectProperty<>(rightItem);
        this.isDirectory = new SimpleBooleanProperty(isDirectory);
    }

    private static boolean isFolderOperation(CompareOperations op) {
        if (op == null) return false;
        return op == CompareOperations.FOLDER || op.name().startsWith("FOLDER_");
    }

    // Metoda isDirectory() a isFolder() vrací hodnotu nového atributu isDirectory
    public boolean isDirectory() {
        return isDirectory.get();
    }

    public BooleanProperty isDirectoryProperty() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory.set(isDirectory);
    }

    /**
     * Zpětně kompatibilní alias k isDirectory()
     */
    public boolean isFolder() {
        return isDirectory();
    }

    public Path getNotNullPath() {
        if (leftItem.getValue().getFileAbsolutePath() != null) {
            return leftItem.getValue().getFileAbsolutePath();
        } else {
            return rightItem.getValue().getFileAbsolutePath();
        }
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

    public ObjectProperty<CompareOperations> operationProperty() {
        return operation;
    }

    ///SETRY
    public void setLeftItem(FileItemModel leftItem) {
        this.leftItem.set(leftItem);
    }
    public void setOperation(CompareOperations operation) {this.operation.set(operation);}
    public void setRightItem(FileItemModel rightItem) {
        this.rightItem.set(rightItem);
    }

}
