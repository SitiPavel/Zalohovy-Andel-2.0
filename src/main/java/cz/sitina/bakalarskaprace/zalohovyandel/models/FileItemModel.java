package cz.sitina.bakalarskaprace.zalohovyandel.models;

import javafx.beans.property.*;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class FileItemModel {
    private final StringProperty fileName = new SimpleStringProperty("");
    private Path fileAbsolutePath;
    private final LongProperty lastModified = new SimpleLongProperty(0);
    private final LongProperty fileSize = new SimpleLongProperty(0);

    public FileItemModel(){
    }
    public FileItemModel(String fileName, Path fileAbsolutePath,
                         long lastModified, long fileSize) {
        this.fileName.set(fileName);
        this.fileAbsolutePath = fileAbsolutePath;
        this.lastModified.set(lastModified);
        this.fileSize.set(fileSize);
    }
    public FileItemModel(String fileName, Path fileAbsolutePath) {
        this.fileName.set(fileName);
        this.fileAbsolutePath = fileAbsolutePath;
        this.fileSize.set(-1);
    }

    ///metoda převede číselný formát v milisekundách od nějakého roku 1970 do klasického datumu
    public SimpleStringProperty  getFormatedDate() {
        if (this.lastModified.get() <= 0) {
            return new SimpleStringProperty("");
        }
        Instant instant = Instant.ofEpochMilli(this.lastModified.get());
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return new SimpleStringProperty(localDateTime.format(formatter));
    }


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
}
