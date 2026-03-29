package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.PackageContentModel;
import javafx.scene.control.TreeItem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeTableShowPackageFilesService {


    public static void buildTreeTablePackageFiles(TreeItem<PackageContentModel> rootItem, List<String> relativePathsStr) {
        HashMap<String, TreeItem<PackageContentModel>> resultMap =  new HashMap<>();
        resultMap.put("",rootItem);

        for (String currentPathStr : relativePathsStr) {

            // aby mi to rodělilo podle zpětného lomítka "\" musím ho 4x udělat "\\\\"
            String[] partsOfPath = currentPathStr.split("\\\\");
            int partsLength = partsOfPath.length;
            String accumulatedPath = "";

            for (int i = 0; i < partsLength; i++) {


                boolean isFile = (i == partsOfPath.length - 1);

                if (isFile) {

                    TreeItem<PackageContentModel> foundedItem = resultMap.get(accumulatedPath);
                    PackageContentModel newItem  = new PackageContentModel(
                            currentPathStr,
                            partsOfPath[i],
                            false
                    );
                    foundedItem.getChildren().add(new TreeItem<>(newItem));
                } else {
                    String newPath = accumulatedPath.isEmpty() ? partsOfPath[i] : accumulatedPath + "\\" + partsOfPath[i];
                    TreeItem<PackageContentModel> foundedItem = resultMap.get(newPath);

                    if (foundedItem == null) {
                        PackageContentModel newItem = new PackageContentModel(
                                newPath,
                                partsOfPath[i],
                                true
                        );
                        TreeItem<PackageContentModel> parentItem = resultMap.get(accumulatedPath);
                        TreeItem<PackageContentModel> newTreeNode = new TreeItem<>(newItem);

                        parentItem.getChildren().add(newTreeNode);
                        resultMap.put(newPath, newTreeNode);
                    }
                    accumulatedPath = newPath;
                }
            }
        }
    }
}
