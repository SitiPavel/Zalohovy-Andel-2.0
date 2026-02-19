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
    private Path relativePath;


    public FileItemModel(){
    }
    public FileItemModel(String fileName, Path fileAbsolutePath,
                         long lastModified, long fileSize, Path relativePath) {
        this.fileName.set(fileName);
        this.fileAbsolutePath = fileAbsolutePath;
        this.lastModified.set(lastModified);
        this.fileSize.set(fileSize);
        this.relativePath = relativePath;
    }
    public FileItemModel(String fileName, Path fileAbsolutePath, Path relativePath) {
        this.fileName.set(fileName);
        this.fileAbsolutePath = fileAbsolutePath;
        this.fileSize.set(-1);
        this.relativePath = relativePath;
    }

    ///metoda převede číselný formát v milisekundách od nějakého roku 1970 do klasického datumu
    public SimpleStringProperty  getFormatedLastModified() {
        if (this.lastModified.get() <= 0) {
            return new SimpleStringProperty("---");
        }
        Instant instant = Instant.ofEpochMilli(this.lastModified.get());
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return new SimpleStringProperty(localDateTime.format(formatter));
    }


    /**
     * Velikost souboru jako formátovaný String
     */
    public StringProperty getFormatedSize() {
        StringProperty result = new SimpleStringProperty("");
        long size = fileSize.get();
        if (lastModified.get() == 0) {
            result.set("---");
            return result;
        }
        if (size < 1024) {
            result.set( size + " B");
            return result;
        }
        if (size < 1024*1024) {
            result.set((size / 1024) + " KB");
            return result;
        }
        if (size < 1024*1024*1024) {
            result.set((size / 1024 / 1024) + " MB");
            return result;
        }
        result.set(size /1024/1024/1024 + " GB");
        return result;



    }


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
