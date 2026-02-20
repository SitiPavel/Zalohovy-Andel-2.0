package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

public class CompareOverviewDataPack {

    public record OperationStatisticsForCompareOverview (
            int leftAdd, int leftUpdate, int leftDelete,
            int blocked, int equals, int unknown,
            int rightDelete, int rightUpdate, int rightAdd,
            long totalSize
    ) {}
}
