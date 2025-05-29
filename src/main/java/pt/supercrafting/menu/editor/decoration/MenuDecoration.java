package pt.supercrafting.menu.editor.decoration;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.editor.MenuEditor;

public interface MenuDecoration extends MenuEditor {

    @NotNull
    static MenuDecoration full(@NotNull ItemStack icon) {
        return new MenuDecorations.Full(icon);
    }

    @NotNull
    static MenuDecoration corner(@NotNull ItemStack icon) {
        return new MenuDecorations.Corner(icon);
    }

    @NotNull
    static MenuDecoration flat(@NotNull ItemStack icon) {
        return new MenuDecorations.Flat(icon);
    }

}
