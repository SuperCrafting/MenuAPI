package pt.supercrafting.menu.handler;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface MenuHandler {

    void onOpen(@NotNull Player player);
    void onClose(@NotNull Player player);

}
