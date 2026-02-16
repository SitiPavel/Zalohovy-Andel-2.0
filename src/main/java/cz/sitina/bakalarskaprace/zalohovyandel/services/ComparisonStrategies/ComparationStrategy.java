package cz.sitina.bakalarskaprace.zalohovyandel.services.ComparisonStrategies;

import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.FileItemModel;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareOperations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ComparationStrategy {

    public CompareOperations compare(
            FileItemModel leftItem,
            FileItemModel rightItem);
}
