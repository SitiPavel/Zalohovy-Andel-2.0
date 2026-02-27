package cz.sitina.bakalarskaprace.zalohovyandel.models.datPacks;

public class GuiOperationFilters_TaskData {

    private boolean isActive_EQUALS = true;
    private boolean isActive_IGNORE = true;
    private boolean isActive_DELETE_LEFT = true;
    private boolean isActive_DELETE_RIGHT = true;
    private boolean isActive_UPDATE_LEFT = true;
    private boolean isActive_UPDATE_RIGHT = true;
    private boolean isActive_ADD_LEFT = false;
    private boolean isActive_ADD_RIGHT = false;
    private boolean isActive_UNKNOWN = true;


    public GuiOperationFilters_TaskData() {
    }

    // Getry
    public boolean isActive_EQUALS() {
        return isActive_EQUALS;
    }

    public boolean isActive_IGNORE() {
        return isActive_IGNORE;
    }

    public boolean isActive_DELETE_LEFT() {
        return isActive_DELETE_LEFT;
    }

    public boolean isActive_DELETE_RIGHT() {
        return isActive_DELETE_RIGHT;
    }

    public boolean isActive_UPDATE_LEFT() {
        return isActive_UPDATE_LEFT;
    }

    public boolean isActive_UPDATE_RIGHT() {
        return isActive_UPDATE_RIGHT;
    }

    public boolean isActive_ADD_LEFT() {
        return isActive_ADD_LEFT;
    }

    public boolean isActive_ADD_RIGHT() {
        return isActive_ADD_RIGHT;
    }

    public boolean isActive_UNKNOWN() {
        return isActive_UNKNOWN;
    }

    // Sertry
    public void setActive_EQUALS(boolean active_EQUALS) {
        isActive_EQUALS = active_EQUALS;
    }

    public void setActive_IGNORE(boolean active_IGNORE) {
        isActive_IGNORE = active_IGNORE;
    }

    public void setActive_DELETE_LEFT(boolean active_DELETE_LEFT) {
        isActive_DELETE_LEFT = active_DELETE_LEFT;
    }

    public void setActive_DELETE_RIGHT(boolean active_DELETE_RIGHT) {
        isActive_DELETE_RIGHT = active_DELETE_RIGHT;
    }

    public void setActive_UPDATE_LEFT(boolean active_UPDATE_LEFT) {
        isActive_UPDATE_LEFT = active_UPDATE_LEFT;
    }

    public void setActive_UPDATE_RIGHT(boolean active_UPDATE_RIGHT) {
        isActive_UPDATE_RIGHT = active_UPDATE_RIGHT;
    }

    public void setActive_ADD_LEFT(boolean active_ADD_LEFT) {
        isActive_ADD_LEFT = active_ADD_LEFT;
    }

    public void setActive_ADD_RIGHT(boolean active_ADD_RIGHT) {
        isActive_ADD_RIGHT = active_ADD_RIGHT;
    }

    public void setActive_UNKNOWN(boolean active_UNKNOWN) {
        isActive_UNKNOWN = active_UNKNOWN;
    }

}
