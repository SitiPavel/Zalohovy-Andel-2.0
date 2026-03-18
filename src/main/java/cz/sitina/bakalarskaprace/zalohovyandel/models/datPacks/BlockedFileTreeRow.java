package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import java.nio.file.Path;

public class BlockedFileTreeRow {

    private String absolutPath;
    private String fileName;


    public BlockedFileTreeRow(String absolutPath) {
        this.absolutPath = absolutPath.toString();
        this.fileName = Path.of(absolutPath).getFileName().toString();
    }


    public void setAbsolutPath(String absolutPath) {
        this.absolutPath = absolutPath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
