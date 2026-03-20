package cz.sitina.bakalarskaprace.zalohovyandel.services.TableServices;

import cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks.BlockedFileTreeRow;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.IconManagerUtility;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.ImageView;

import javax.sound.sampled.Line;
import java.util.List;

public class BlockedTreeTableGuiService {

    IconManagerUtility iconManager = new IconManagerUtility();

    // získání ikon na řádek podle typu položky
    public ImageView getIcon_BlockRow(BlockedFileTreeRow blockedRow) {
        ImageView imageView = new ImageView();
        {
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
        }

        if (blockedRow.isOrganizationFolder()) {
            imageView.setImage(iconManager.getFolder20x20());
        } else {
            if (blockedRow.isFolder()) {
                imageView.setImage(iconManager.getBlockedFolder());
            } else {
                imageView.setImage(iconManager.getBlockedFile());
            }
        }

        return imageView;
    }

    // získání tlačítka na odstranení položky z listu
    public void getButtonDeleteListItem( Button button,
                                        TreeItem<BlockedFileTreeRow> rootItem,
                                        String leftRootPath, String rightRootPath,
                                        TreeTableCell<BlockedFileTreeRow, String> cell,
                                        List<String> blockList) {

        {
            button.setText("✘");
            button.getStyleClass().addAll("WindowTitle_L1_Dark", "button_NonBackground", "buttonModify_backgroundRed");


            button.setOnAction(event -> {
                String deletedPath = cell.getTableRow().getTreeItem().getValue().getPath();
                BlockedTreeTableService.removeItemAndChildren(deletedPath, blockList);
                rootItem.getChildren().clear();
                BlockedTreeTableService.buildTreeTableBlockedFiles(rootItem, blockList, leftRootPath, rightRootPath);
            });
        }
    }
}
