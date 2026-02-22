package cz.sitina.bakalarskaprace.zalohovyandel.services;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;

import java.io.File;
import java.util.Locale;

public class Utility {

    public Utility() {
    }

    // Velikost souboru jako formátovaný String
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

    // získání dat o obsazenosti disku
    public DiskSpaceStatisticData getDiskSpaceStatistics(String path) {

        File file = new File(path);

        long totalSpace = file.getTotalSpace();
        long usedSpace = file.getUsableSpace();
        return new DiskSpaceStatisticData(totalSpace, usedSpace);
    }

}
