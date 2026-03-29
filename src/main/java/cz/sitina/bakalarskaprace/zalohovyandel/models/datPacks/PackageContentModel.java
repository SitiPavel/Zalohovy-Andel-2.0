package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

public class PackageContentModel {

    private String relativePath;
    private String fileName;
    private boolean isFolder;

    public PackageContentModel(String relativePath, String fileName, boolean isFolder) {
        this.relativePath = relativePath;
        this.fileName = fileName;
        this.isFolder = isFolder;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
