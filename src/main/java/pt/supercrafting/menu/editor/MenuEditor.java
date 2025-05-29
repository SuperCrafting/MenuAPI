package pt.supercrafting.menu.editor;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.Menu;

public interface MenuEditor {

    void edit(@NotNull Menu menu, @NotNull Inventory inventory);

}
