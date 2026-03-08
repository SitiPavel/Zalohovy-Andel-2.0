package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;

import java.util.List;

public class ComparedDataAndStatisticsForCompereOverview {
    private int leftAddCount = 0;
    private int leftUpdateCount = 0;
    private int leftDeleteCount = 0;
    private int ignoreCount = 0;
    private int equalsCount = 0;
    private int unknownCount = 0;
    private int rightDeleteCount = 0;
    private int rightUpdateCount = 0;
    private int rightAddCount = 0;

    private long dataTransportSize = 0;

    private List<ComparedItemOperItemRowModel> comparedFilesList;

    public ComparedDataAndStatisticsForCompereOverview() {
    }

    public boolean isDataMoveOperations() {
        return (leftAddCount > 0 ||
                leftUpdateCount > 0 ||
                leftDeleteCount > 0 ||
                rightDeleteCount > 0 ||
                rightUpdateCount > 0 ||
                rightAddCount > 0
        );
    }

    public int getLeftAddCount() {
        return leftAddCount;
    }
    public int getLeftUpdateCount() {
        return leftUpdateCount;
    }
    public int getLeftDeleteCount() {
        return leftDeleteCount;
    }
    public int getIgnoreCount() {
        return ignoreCount;
    }
    public int getEqualsCount() {
        return equalsCount;
    }
    public int getUnknownCount() {
        return unknownCount;
    }
    public int getRightDeleteCount() {
        return rightDeleteCount;
    }
    public int getRightUpdateCount() {
        return rightUpdateCount;
    }
    public int getRightAddCount() {
        return rightAddCount;
    }
    public long getDataTransportSize() {
        return dataTransportSize;
    }
    public List<ComparedItemOperItemRowModel> getComparedFilesList() {
        return comparedFilesList;
    }

    public void attributeLeftAddCount(int count) {
        this.leftAddCount += count;
    }
    public void attributeLeftUpdateCount(int count) {
        this.leftUpdateCount += count;
    }
    public void attributeLeftDeleteCount(int count) {
        this.leftDeleteCount += count;
    }

    // --- GLOBÁLNÍ STAVY ---
    public void attributeIgnoreCount(int count) {
        this.ignoreCount += count;
    }
    public void attributeEqualsCount(int count) {
        this.equalsCount += count;
    }
    public void attributeUnknownCount(int count) {
        this.unknownCount += count;
    }

    // --- PRAVÁ STRANA ---
    public void attributeRightDeleteCount(int count) {
        this.rightDeleteCount += count;
    }
    public void attributeRightUpdateCount(int count) {
        this.rightUpdateCount += count;
    }
    public void attributeRightAddCount(int count) {
        this.rightAddCount += count;
    }

    // --- DATOVÝ OBJEM ---
    public void attributeDataTransportSize(long size) {
        this.dataTransportSize += size;
    }

    public void setComparedFilesList(List<ComparedItemOperItemRowModel> comparedFilesList) {
        this.comparedFilesList = comparedFilesList;
    }
}
