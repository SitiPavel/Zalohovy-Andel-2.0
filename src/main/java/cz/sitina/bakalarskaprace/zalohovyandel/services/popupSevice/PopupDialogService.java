package cz.sitina.bakalarskaprace.zalohovyandel.services.popupSevice;

import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeFolderOrFileOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.controllers.popupControllers.ChangeGroupOperationController;
import cz.sitina.bakalarskaprace.zalohovyandel.models.ComparedItemOperItemRowModel;
import cz.sitina.bakalarskaprace.zalohovyandel.services.utilities.ErrorUtility;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.List;

public class PopupDialogService {
    private static final String POPUP_PATH = "/cz/sitina/bakalarskaprace/zalohovyandel/fxml/popup/";
    private static final double TOLERANCE_PX = 10.0;
    private static final double CURSOR_INSIDE_PADDING = 8.0;

    /**
     * Zpětně kompatibilní přetížení (pokud nejsou předány souřadnice, použije se aktuální pozice myši).
     */
    public void showOperationPopupWindow(TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isBackupTask,
                                         Runnable onSuccess) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        showOperationPopupWindow(treeItem, isBackupTask, p.getX(), p.getY(), onSuccess);
    }

    public void showOperationPopupWindow(TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isBackupTask,
                                         double screenX, double screenY,
                                         Runnable onSuccess) {
        String fxmlPath;
        boolean isFolder = false;

        if (treeItem.getValue().getOperation().toString().contains("FOLDER")) {
            fxmlPath = POPUP_PATH + "changeFolderOperation.fxml";
            isFolder = true;
        } else {
            fxmlPath = POPUP_PATH + "changeFileOperation.fxml";
        }

        openDialog(fxmlPath, treeItem, isFolder, isBackupTask, screenX, screenY, onSuccess);
    }

    /**
     * Zpětně kompatibilní přetížení pro skupinové operace.
     */
    public void showOperationPopupWindow_group(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems,
                                               boolean isBackupTask, Runnable onSuccess) {
        Point p = MouseInfo.getPointerInfo().getLocation();
        showOperationPopupWindow_group(selectedItems, isBackupTask, p.getX(), p.getY(), onSuccess);
    }

    public void showOperationPopupWindow_group(List<TreeItem<ComparedItemOperItemRowModel>> selectedItems,
                                               boolean isBackupTask,
                                               double screenX, double screenY,
                                               Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(POPUP_PATH + "changeGroupOperation.fxml"));
            Parent root = loader.load();
            ChangeGroupOperationController controller = loader.getController();
            controller.setSelectedItems(selectedItems);
            controller.setIsAllOperChanged(onSuccess);
            controller.setIsBackupTask(isBackupTask);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));

            setupPopupStage(stage, screenX, screenY);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání okna",
                    "Nepodařilo se načíst obsah vyskakovacího okna.",
                    e.getMessage());
        }
    }

    private void openDialog(String fxmlPath, TreeItem<ComparedItemOperItemRowModel> treeItem, boolean isFolder,
                            boolean isBackupTask,
                            double screenX, double screenY,
                            Runnable onSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            ChangeFolderOrFileOperationController controller = loader.getController();
            controller.setFolder(isFolder);
            controller.setComparedItemOperItemRowModel(treeItem, isBackupTask);
            controller.setOnUpdateSuccess(onSuccess);

            Stage stage = new Stage();
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));

            setupPopupStage(stage, screenX, screenY);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            ErrorUtility.getInstance("Kritická chyba při načítání okna",
                    "Nepodařilo se načíst obsah vyskakovacího okna.",
                    e.getMessage());
        }
    }

    /**
     * Univerzální nastavení chování popup okna:
     * 1. Pozicování okna na kurzor s ošetřením hranic monitoru (aby nelezlo ven a kurzor byl stále uvnitř okna).
     * 2. Automatické zavření při opuštění okna myší přes toleranci (10 px) přes Timeline.
     * 3. Automatické zavření při ztrátě fokusu.
     *
     * @param stage   Stage popup okna
     * @param screenX Cílová X souřadnice kurzoru
     * @param screenY Cílová Y souřadnice kurzoru
     */
    public static void setupPopupStage(Stage stage, double screenX, double screenY) {
        // OPRAVENO: Aktivní periodické sledování kurzoru myši přes Timeline s tolerancí 10px
        Timeline mouseTracker = new Timeline(new KeyFrame(Duration.millis(50), event -> {
            try {
                Point p = MouseInfo.getPointerInfo().getLocation();
                double cursorX = p.getX();
                double cursorY = p.getY();

                double minX = stage.getX() - TOLERANCE_PX;
                double maxX = stage.getX() + stage.getWidth() + TOLERANCE_PX;
                double minY = stage.getY() - TOLERANCE_PX;
                double maxY = stage.getY() + stage.getHeight() + TOLERANCE_PX;

                if (cursorX < minX || cursorX > maxX || cursorY < minY || cursorY > maxY) {
                    stage.close();
                }
            } catch (Exception e) {
                stage.close();
            }
        }));
        mouseTracker.setCycleCount(Animation.INDEFINITE);

        stage.setOnShown(event -> {
            Platform.runLater(() -> {
                double width = stage.getWidth();
                double height = stage.getHeight();

                // Detekce správného monitoru podle pozice kurzoru
                ObservableList<Screen> screens = Screen.getScreensForRectangle(screenX, screenY, 1, 1);
                Screen targetScreen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
                Rectangle2D bounds = targetScreen.getVisualBounds();

                // OPRAVENO: Výchozí pozicování: střed horního okraje na kurzor s mírným překrytím (10 px)
                double targetX = screenX - (width / 2.0);
                double targetY = screenY - 10.0;

                // OPRAVENO: Kontrola a posun tak, aby okno nepřeteklo hranice monitoru
                if (targetX < bounds.getMinX()) {
                    targetX = bounds.getMinX();
                } else if (targetX + width > bounds.getMaxX()) {
                    targetX = bounds.getMaxX() - width;
                }

                if (targetY < bounds.getMinY()) {
                    targetY = bounds.getMinY();
                } else if (targetY + height > bounds.getMaxY()) {
                    targetY = bounds.getMaxY() - height;
                }

                // OPRAVENO: Zajištění, že kurzor zůstane uvnitř okna i po přizpůsobení okrajům obrazovky
                if (screenX < targetX) {
                    targetX = Math.max(bounds.getMinX(), screenX - CURSOR_INSIDE_PADDING);
                } else if (screenX > targetX + width) {
                    targetX = Math.min(bounds.getMaxX() - width, screenX - width + CURSOR_INSIDE_PADDING);
                }

                if (screenY < targetY) {
                    targetY = Math.max(bounds.getMinY(), screenY - CURSOR_INSIDE_PADDING);
                } else if (screenY > targetY + height) {
                    targetY = Math.min(bounds.getMaxY() - height, screenY - height + CURSOR_INSIDE_PADDING);
                }

                stage.setX(targetX);
                stage.setY(targetY);
                mouseTracker.play();
            });
        });

        // Zastavení trackeru při zavření okna nebo ztrátě fokusu (prevence memory leaků)
        stage.setOnHidden(event -> mouseTracker.stop());

        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                mouseTracker.stop();
                stage.close();
            }
        });
    }
}