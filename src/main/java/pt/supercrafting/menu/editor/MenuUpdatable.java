package pt.supercrafting.menu.editor;

public interface MenuUpdatable {

    void update();

    default int updateRate(){
        return 20;
    }

}
