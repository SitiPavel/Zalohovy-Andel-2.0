package cz.sitina.bakalarskaprace.zalohovyandel.models;

import javafx.beans.property.*;

public class CompareItemModel {
    private final StringProperty fileName;
    private final StringProperty fileRelativePath;
    private final  StringProperty fileDate;
    private final IntegerProperty fileSize;

    public CompareItemModel(String fileName, String fileRelativePath,
                            String fileDate, int fileSize) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileRelativePath = new SimpleStringProperty(fileRelativePath);
        this.fileDate = new SimpleStringProperty(fileDate);
        this.fileSize = new SimpleIntegerProperty(fileSize);
    }
    public CompareItemModel(String fileName, String fileRelativePath, String fileDate) {
        this.fileName = new SimpleStringProperty(fileName);
        this.fileRelativePath = new SimpleStringProperty(fileRelativePath);
        this.fileDate = new SimpleStringProperty(fileDate);
        this.fileSize = new SimpleIntegerProperty(-1);
    }



    public String getFileName() {
        return fileName.get();
    }
    public StringProperty fileNameProperty() {
        return fileName;
    }
    public String getFileRelativePath() {
        return fileRelativePath.get();
    }
    public StringProperty fileRelativePathProperty() {
        return fileRelativePath;
    }
    public String getFileDate() {
        return fileDate.get();
    }
    public StringProperty fileDateProperty() {
        return fileDate;
    }
    public int getFileSize() {
        return fileSize.get();
    }
    public IntegerProperty fileSizeProperty() {
        return fileSize;
    }
}
