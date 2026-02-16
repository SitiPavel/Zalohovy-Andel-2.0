package cz.sitina.bakalarskaprace.zalohovyandel.models;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;

public class TreeTableRowModel {

    private FileItemModel leftItem;
    private CompareOperations operation;
    private FileItemModel rightItem;

    public TreeTableRowModel(FileItemModel leftItem, CompareOperations operation, FileItemModel rightItem) {
        this.leftItem = leftItem;
        this.operation = operation;
        this.rightItem = rightItem;
    }

    public FileItemModel getLeftItem() {
        return leftItem;
    }
    public void setLeftItem(FileItemModel leftItem) {
        this.leftItem = leftItem;
    }
    public CompareOperations getOperation() {
        return operation;
    }

    public void setOperation(CompareOperations operation) {
        this.operation = operation;
    }
    public FileItemModel getRightItem() {
        return rightItem;
    }
    public void setRightItem(FileItemModel rightItem) {
        this.rightItem = rightItem;
    }
}
