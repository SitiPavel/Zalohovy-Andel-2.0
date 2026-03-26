package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

public class BackupDatabaseDTO {
    private String absolutPath;
    private String relativePath;
    private long size;
    private long lastModified;
    private String location; // ve kterém balíčku je uložena

    public BackupDatabaseDTO(String absolutPath, String relativePath, long size, long lastModified, String location) {
        this.absolutPath = absolutPath;
        this.relativePath = relativePath;
        this.size = size;
        this.lastModified = lastModified;
        this.location = location;
    }

    public String getAbsolutPath() {
        return absolutPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getSize() {
        return size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getLocation() {
        return location;
    }
}
