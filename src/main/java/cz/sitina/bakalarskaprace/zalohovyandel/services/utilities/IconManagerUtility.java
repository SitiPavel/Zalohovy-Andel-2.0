package cz.sitina.bakalarskaprace.zalohovyandel.services.utilities;

import javafx.scene.image.Image;

import java.util.Objects;

public class IconManagerUtility {

    //**********************************/
    // Cesty
    private final String path_visualObjects = "/cz/sitina/bakalarskaprace/zalohovyandel/images/visualObjects/";
    private final String path_icons = "/cz/sitina/bakalarskaprace/zalohovyandel/images/icons/";
    private String path_iconsSmall =  "/cz/sitina/bakalarskaprace/zalohovyandel/images/iconsSmall/";

    //**********************************/
    // typy záloh
    private final Image biDirectionalSync =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "biDirectionalSync.png")));
    private final Image oneWayLeftToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "oneWayLeftToRight.png")));
    private final Image oneWayRightToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "oneWayRightToLeft.png")));
    private final Image mirroringLeftToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "mirroringLeftToRight.png")));
    private final Image mirroringRightToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "mirroringRightToLeft.png")));

    //**********************************/
    // typy práce u zálohy
    private final Image backupWork =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "backupWork.png")));
    private final Image recoveryWork =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_visualObjects + "recoveryWork.png")));


    //**********************************/
    // FILTRY
    private final Image icon_addItem_Left =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "addItem_left.png")));
    private final Image icon_modified_left  =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "modified_left.png")));
    private final Image icon_delete_left    =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "delete_left.png")));
    private final Image icon_block          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "block.png")));
    private final Image icon_equal          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "equal.png")));
    private final Image icon_unknown   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "unknown.png")));
    private final Image icon_delete_right   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "delete_right.png")));
    private final Image icon_modified_right =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "modified_right.png")));
    private final Image icon_addItem_right =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "addItem_right.png")));

    private final Image icon_addItem_Left_OFF =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "addItem_left_OFF.png")));
    private final Image icon_modified_left_OFF  =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "modified_left_OFF.png")));
    private final Image icon_delete_left_OFF    =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "delete_left_OFF.png")));
    private final Image icon_block_OFF          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "block_OFF.png")));
    private final Image icon_equal_OFF          =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "equal_OFF.png")));
    private final Image icon_unknown_OFF   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "unknown_OFF.png")));
    private final Image icon_delete_right_OFF   =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "delete_right_OFF.png")));
    private final Image icon_modified_right_OFF =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "modified_right_OFF.png")));
    private final Image icon_addItem_right_OFF =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "addItem_right_OFF.png")));

    //**********************************/
    // koláče
    private final Image pieChartYellow_ToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "pieChar-Yellow_Left.png")));
    private final Image pieChartRed_ToLeft =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "pieChar-Red_left.png")));
    private final Image pieChartYellow_ToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "pieChar-Yellow_Right.png")));
    private final Image pieChartRed_ToRight =
            new Image(Objects.requireNonNull(getClass().getResourceAsStream(path_icons + "pieChar-Red_Right.png")));

    //**********************************/
    // task kategorie
    private final Image manualSync_TabbleIcon =
            new Image(getClass().getResourceAsStream(path_iconsSmall + "manualSync_TabbleIcon.png"));
    private final Image backupTask_TableIcon =
            new Image(getClass().getResourceAsStream(path_iconsSmall + "backupTask_TableIcon.png"));
    private final Image backupTask_TableIcon_Com_Enc =
            new Image(getClass().getResourceAsStream(path_iconsSmall + "backupTask_TableIcon_Com_Enc.png"));
    private final Image backupTask_TableIcon_Com_X =
            new Image(getClass().getResourceAsStream(path_iconsSmall + "backupTask_TableIcon_Com_X.png"));
    private final Image backupTask_TableIcon_X_Enc =
            new Image(getClass().getResourceAsStream(path_iconsSmall + "backupTask_TableIcon_X_Enc.png"));


    //**********************************/
    // status = výsledek procesu
    private final Image cros =
            new Image(getClass().getResourceAsStream(path_visualObjects + "cros.png"));
    private final Image checkMark =
            new Image(getClass().getResourceAsStream(path_visualObjects + "checkMark.png"));
    private final Image error =
            new Image(getClass().getResourceAsStream(path_visualObjects + "error.png"));




    //**********************************/
    // nějaké GUI prvky - mají funkčnost
    private final Image entryIcon =
            new Image(getClass().getResourceAsStream(path_iconsSmall + "Entry.png"));

    //**********************************/
    // univerzal ukazatelé a ikonky

    private final Image file20x20 =
            new Image(getClass().getResourceAsStream(path_visualObjects + "file20x20.png"));
    private final Image folder20x20 =
            new Image(getClass().getResourceAsStream(path_visualObjects + "folder20x20.png"));
    private final Image blockedFolder =
            new Image(getClass().getResourceAsStream(path_visualObjects + "blockedFolder.png"));
    private final Image blockedFile =
            new Image(getClass().getResourceAsStream(path_visualObjects + "blockedFile.png"));



    //**********************************/
    // ikony operací
    private final Image iconEquals       = new Image(getClass().getResourceAsStream(path_iconsSmall + "EQUALS.png"));
    private final Image iconIgnore       = new Image(getClass().getResourceAsStream(path_iconsSmall + "IGNORE.png"));

    private final Image iconAddLeft      = new Image(getClass().getResourceAsStream(path_iconsSmall + "ADD_LEFT.png"));
    private final Image iconAddRight     = new Image(getClass().getResourceAsStream(path_iconsSmall + "ADD_RIGHT.png"));

    private final Image iconDeleteBoth   = new Image(getClass().getResourceAsStream(path_iconsSmall + "DELETE_BOTH.png"));
    private final Image iconDeleteLeft   = new Image(getClass().getResourceAsStream(path_iconsSmall + "DELETE_LEFT.png"));
    private final Image iconDeleteRight  = new Image(getClass().getResourceAsStream(path_iconsSmall + "DELETE_RIGHT.png"));
    private final Image iconUnknown      = new Image(getClass().getResourceAsStream(path_iconsSmall + "UNKNOWN.png"));

    private final Image iconUpdateLeft   = new Image(getClass().getResourceAsStream(path_iconsSmall + "UPDATE_LEFT.png"));
    private final Image iconUpdateRight  = new Image(getClass().getResourceAsStream(path_iconsSmall + "UPDATE_RIGHT.png"));
    // SLOŽKY
    private final Image iconFolder  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER.png"));
    private final Image iconFOLDER_UNEVEN  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_UNEVEN.png"));
    private final Image iconFOLDER_EQUALS  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_EQUALS.png"));

    private final Image iconFOLDER_DELETE_LEFT  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_DELETE_LEFT.png"));
    private final Image iconFOLDER_DELETE_RIGHT  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_DELETE_RIGHT.png"));
    private final Image iconFOLDER_DELETE_BOTH  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_DELETE_BOTH.png"));

    private final Image iconFOLDER_UPDATE_LEFT  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_UPDATE_LEFT.png"));
    private final Image iconFOLDER_UPDATE_RIGHT  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_UPDATE_RIGHT.png"));

    private final Image iconFOLDER_ADD_LEFT  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_ADD_LEFT.png"));
    private final Image iconFOLDER_ADD_RIGHT  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_ADD_RIGHT.png"));

    private final Image iconFOLDER_UNKNOWN = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_UNKNOWN.png"));
    private final Image iconFOLDER_IGNORE  = new Image(getClass().getResourceAsStream(path_iconsSmall + "FOLDER_IGNORE.png"));





    ///  getry

    public Image getBiDirectionalSync() {
        return biDirectionalSync;
    }

    public Image getOneWayLeftToRight() {
        return oneWayLeftToRight;
    }

    public Image getOneWayRightToLeft() {
        return oneWayRightToLeft;
    }

    public Image getMirroringLeftToRight() {
        return mirroringLeftToRight;
    }

    public Image getMirroringRightToLeft() {
        return mirroringRightToLeft;
    }

    public Image getIcon_addItem_Left() {
        return icon_addItem_Left;
    }

    public Image getIcon_modified_left() {
        return icon_modified_left;
    }

    public Image getIcon_delete_left() {
        return icon_delete_left;
    }

    public Image getIcon_block() {
        return icon_block;
    }

    public Image getIcon_equal() {
        return icon_equal;
    }

    public Image getIcon_unknown() {
        return icon_unknown;
    }

    public Image getIcon_delete_right() {
        return icon_delete_right;
    }

    public Image getIcon_modified_right() {
        return icon_modified_right;
    }

    public Image getIcon_addItem_right() {
        return icon_addItem_right;
    }

    public Image getIcon_addItem_Left_OFF() {
        return icon_addItem_Left_OFF;
    }

    public Image getIcon_modified_left_OFF() {
        return icon_modified_left_OFF;
    }

    public Image getIcon_delete_left_OFF() {
        return icon_delete_left_OFF;
    }

    public Image getIcon_block_OFF() {
        return icon_block_OFF;
    }

    public Image getIcon_equal_OFF() {
        return icon_equal_OFF;
    }

    public Image getIcon_unknown_OFF() {
        return icon_unknown_OFF;
    }

    public Image getIcon_delete_right_OFF() {
        return icon_delete_right_OFF;
    }

    public Image getIcon_modified_right_OFF() {
        return icon_modified_right_OFF;
    }

    public Image getIcon_addItem_right_OFF() {
        return icon_addItem_right_OFF;
    }

    public Image getPieChartYellow_ToLeft() {
        return pieChartYellow_ToLeft;
    }

    public Image getPieChartRed_ToLeft() {
        return pieChartRed_ToLeft;
    }

    public Image getPieChartYellow_ToRight() {
        return pieChartYellow_ToRight;
    }

    public Image getPieChartRed_ToRight() {
        return pieChartRed_ToRight;
    }

    public Image getEntryIcon() {
        return entryIcon;
    }



    public Image getManualSync_TabbleIcon() {
        return manualSync_TabbleIcon;
    }

    public Image getIconEquals() {
        return iconEquals;
    }

    public Image getIconIgnore() {
        return iconIgnore;
    }

    public Image getIconAddLeft() {
        return iconAddLeft;
    }

    public Image getIconAddRight() {
        return iconAddRight;
    }

    public Image getIconDeleteBoth() {
        return iconDeleteBoth;
    }

    public Image getIconDeleteLeft() {
        return iconDeleteLeft;
    }

    public Image getIconDeleteRight() {
        return iconDeleteRight;
    }

    public Image getIconUnknown() {
        return iconUnknown;
    }

    public Image getIconUpdateLeft() {
        return iconUpdateLeft;
    }

    public Image getIconUpdateRight() {
        return iconUpdateRight;
    }

    public Image getIconFolder() {
        return iconFolder;
    }

    public Image getIconFOLDER_UNEVEN() {
        return iconFOLDER_UNEVEN;
    }

    public Image getIconFOLDER_EQUALS() {
        return iconFOLDER_EQUALS;
    }

    public Image getIconFOLDER_DELETE_LEFT() {
        return iconFOLDER_DELETE_LEFT;
    }

    public Image getIconFOLDER_DELETE_RIGHT() {
        return iconFOLDER_DELETE_RIGHT;
    }

    public Image getIconFOLDER_DELETE_BOTH() {
        return iconFOLDER_DELETE_BOTH;
    }

    public Image getIconFOLDER_UPDATE_LEFT() {
        return iconFOLDER_UPDATE_LEFT;
    }

    public Image getIconFOLDER_UPDATE_RIGHT() {
        return iconFOLDER_UPDATE_RIGHT;
    }

    public Image getIconFOLDER_ADD_LEFT() {
        return iconFOLDER_ADD_LEFT;
    }

    public Image getIconFOLDER_ADD_RIGHT() {
        return iconFOLDER_ADD_RIGHT;
    }

    public Image getIconFOLDER_UNKNOWN() {
        return iconFOLDER_UNKNOWN;
    }

    public Image getIconFOLDER_IGNORE() {
        return iconFOLDER_IGNORE;
    }

    public Image getFolder20x20() {
        return folder20x20;
    }

    public Image getFile20x20() {
        return file20x20;
    }

    public Image getBackupWork() {
        return backupWork;
    }

    public Image getRecoveryWork() {
        return recoveryWork;
    }

    public Image getBackupTask_TableIcon() {
        return backupTask_TableIcon;
    }

    public String getPath_visualObjects() {
        return path_visualObjects;
    }

    public String getPath_icons() {
        return path_icons;
    }

    public String getPath_iconsSmall() {
        return path_iconsSmall;
    }

    public Image getBlockedFolder() {
        return blockedFolder;
    }

    public Image getBlockedFile() {
        return blockedFile;
    }

    public Image getCros() {
        return cros;
    }

    public Image getCheckMark() {
        return checkMark;
    }

    public Image getError() {
        return error;
    }

    public Image getBackup_TableIcon() {
        return backupTask_TableIcon;
    }

    public Image getBackupTask_TableIcon_Com_Enc() {
        return backupTask_TableIcon_Com_Enc;
    }

    public Image getBackupTask_TableIcon_Com_X() {
        return backupTask_TableIcon_Com_X;
    }

    public Image getBackupTask_TableIcon_X_Enc() {
        return backupTask_TableIcon_X_Enc;
    }
}
