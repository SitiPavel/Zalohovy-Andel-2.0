package cz.sitina.bakalarskaprace.zalohovyandel.models;

import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.Utility;
import javafx.beans.property.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileItemModel {
    private Path fileAbsolutePath;
    private Path relativePath;
    private final StringProperty fileName = new SimpleStringProperty("");
    private final LongProperty lastModified = new SimpleLongProperty(0);
    private final LongProperty fileSize = new SimpleLongProperty(0);

    private final Utility utility = new Utility();

    public FileItemModel(){
    }
    public FileItemModel(String relativePath){
        this.relativePath = Paths.get(relativePath);
    }
    public FileItemModel(String fileName, Path fileAbsolutePath,
                         long lastModified, long fileSize, Path relativePath) {
        this.fileName.set(fileName);
        this.fileAbsolutePath = fileAbsolutePath;
        this.relativePath = relativePath;
        this.lastModified.set(lastModified);
        this.fileSize.set(fileSize);
    }
    public FileItemModel(String fileName, Path fileAbsolutePath, Path relativePath) {
        this.fileName.set(fileName);
        this.fileAbsolutePath = fileAbsolutePath;
        this.relativePath = relativePath;
        this.fileSize.set(-1);
    }

    ///metoda převede číselný formát v milisekundách od nějakého roku 1970 do klasického datumu
    public SimpleStringProperty  getFormatedLastModified() {
        if (this.lastModified.get() == 0) {
            return new SimpleStringProperty("---");
        }
        Instant instant = Instant.ofEpochMilli(this.lastModified.get());
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return new SimpleStringProperty(localDateTime.format(formatter));
    }

    public StringProperty getFormatedSize() {
        String result = utility.getFormatedSize(this.fileSize.get());
        return new SimpleStringProperty(result);
    }

    public StringProperty getFormatedName() {
        if (this.fileName.get() == null || this.fileName.get().isEmpty()) {
            return new SimpleStringProperty("---");
        }
        return new SimpleStringProperty(this.fileName.get());
    };

    public StringProperty getFormatedNameFileStructure() {
        if (this.fileName.get() == null || this.fileName.get().isEmpty()) {
            return new SimpleStringProperty("---");
        }
        return new SimpleStringProperty(this.fileName.get());
    };


    /// GETRY
    public String getFileName() {
        return fileName.get();
    }
    public StringProperty fileNameProperty() {
        return fileName;
    }
    public Path getFileAbsolutePath() {
        return fileAbsolutePath;
    }
    public long getLastModified() {
        return lastModified.get();
    }
    public LongProperty lastModifiedProperty() {
        return lastModified;
    }
    public long getFileSize() {
        return fileSize.get();
    }
    public LongProperty fileSizeProperty() {
        return fileSize;
    }
    public Path getRelativePath() {
        return relativePath;
    }

    /// SETRY
    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }
    public void setFileAbsolutePath(Path fileAbsolutePath) {
        this.fileAbsolutePath = fileAbsolutePath;
    }
    public void setLastModified(long lastModified) {
        this.lastModified.set(lastModified);
    }
    public void setFileSize(long fileSize) {
        this.fileSize.set(fileSize);
    }
    public void setRelativePath(Path relativePath) {
        this.relativePath = relativePath;
    }
}
