package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;

public class BlockedFileTreeRow {

    private StringProperty path;
    private StringProperty fileName;
    private SimpleObjectProperty<Boolean> isExist;
    private boolean isOrganizationFolder;

    private boolean isFolder;


    public BlockedFileTreeRow(String path, boolean isFolder, Boolean isExist, boolean isOrganizationFolder) {

        this.path = new SimpleStringProperty(path);
        this.fileName = new SimpleStringProperty(Path.of(path).getFileName().toString());
        this.isFolder = isFolder;
        this.isExist = new SimpleObjectProperty<>(isExist);
        this.isOrganizationFolder = isOrganizationFolder;
    }

    public BlockedFileTreeRow(String path, String fileName,boolean isFolder, boolean isExist, boolean isOrganizationFolder) {
        this.path = new SimpleStringProperty(path);
        this.fileName = new SimpleStringProperty(fileName);
        this.isFolder = isFolder;
        this.isExist = new SimpleObjectProperty<>(isExist);
        this.isOrganizationFolder = isOrganizationFolder;
    }

    public String getPath() {
        return path.getValue();
    }

    public String getFileName() {
        return fileName.getValue();
    }

    public StringProperty getFileNameProperty() {
        return fileName;
    }

    public ObjectProperty<Boolean> getExistProperty() {
        return isExist;
    }

    public boolean getExist() {
        return isExist.get();
    }

    public boolean isFolder() {
        return isFolder;
    }

    public boolean isOrganizationFolder() {
        return isOrganizationFolder;
    }

    public void setPath(String path) {
        this.path.setValue(path);
    }

    public void setFileName(String fileName) {
        this.fileName.setValue(fileName);
    }

    public void setExist(Boolean exist) {
        isExist.setValue(exist);
    }

}
