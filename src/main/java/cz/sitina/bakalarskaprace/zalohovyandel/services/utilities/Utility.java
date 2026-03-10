package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.DiskSpaceStatisticData;

import java.io.File;
import java.nio.file.Paths;

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

    // zjistíme zda se velikost zálohy vejde do disku
    public boolean isSazeSmalerThenFreeSpace(String diskPath, long size  ) {
        File file = new File(diskPath);

        // pokud cesta neexistuje tak zjistíme přez rodičovké adresáře
        while (!file.exists() && file.getParent() != null) {
            file = file.getParentFile();
        }

        // vypočítáme rezervu pro disk ale 500mb je minimum
        long totalSpace = file.getTotalSpace();
        long freeSpace = file.getUsableSpace();

        long reserve_min = 500L * 1024L * 1024L; // 500 MB
        long reserve_0015 = (long) (totalSpace * 0.015);
        long finalReserve = Math.max(reserve_min, reserve_0015);

        if (freeSpace > finalReserve+size) {
            return true;
        } else {
            return false;
        }
    }



}
