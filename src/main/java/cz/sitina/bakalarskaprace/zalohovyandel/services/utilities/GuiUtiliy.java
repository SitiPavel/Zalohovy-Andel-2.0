package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.CompareStrategy;
import cz.sitina.bakalarskaprace.zalohovyandel.models.enums.FinishProcesStatus;
import javafx.scene.image.Image;

import javax.swing.text.IconView;

public  class GuiUtiliy {

    private static IconManagerUtility iconManager = new IconManagerUtility();

    public static Image findIconForStrategy(CompareStrategy strategy) {
        return  switch (strategy) {
            case BIDIRECTIONAL_SYNC -> iconManager.getBiDirectionalSync();
            case BACKUP_TO_RIGHT -> iconManager.getOneWayLeftToRight();
            case BACKUP_TO_LEFT -> iconManager.getOneWayRightToLeft();
            case MIRRORING_to_RIGHT -> iconManager.getMirroringLeftToRight();
            case MIRRORING_TO_lEFT -> iconManager.getMirroringRightToLeft();
        };
    }

    public static Image getIconProcessStatus(FinishProcesStatus status) {
        return switch (status) {
            case SUCCESS -> iconManager.getCheckMark();
            case FAILURE -> iconManager.getError();
            case INTERRUPTED -> iconManager.getCros();
        };
    }
}
