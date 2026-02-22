package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

public class DiskSpaceStatisticData {

    private long totalSpace = 0;
    private long usedSpace = 0;
    private long freeSpace = 0;

    public DiskSpaceStatisticData(long totalSpace, long freeSpace) {
        this.totalSpace = totalSpace;
        this.freeSpace = freeSpace;
        this.usedSpace = totalSpace - freeSpace;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public long getFreeSpace() {
        return freeSpace;
    }
}
