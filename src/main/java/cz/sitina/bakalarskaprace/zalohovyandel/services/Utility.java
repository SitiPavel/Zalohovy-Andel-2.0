package cz.sitina.bakalarskaprace.zalohovyandel.services;

public class Utility {

    public Utility() {
    }

    /**
     * Velikost souboru jako formátovaný String
     */
    public String getFormatedSize(long size) {
        if (size == 0) {
            return "---";
        }
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024*1024) {
            return (size / 1024) + " KB";
        }
        if (size < 1024*1024*1024) {
            return (size / 1024 / 1024) + " MB";
        }
        return (size /1024/1024/1024) + " GB";
    }

}
