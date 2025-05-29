package pt.supercrafting.menu.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;
import pt.supercrafting.menu.MenuManager;

public final class MenuAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        MenuManager.init(this);

        getComponentLogger().info(Component.text("MenuAPI has been enabled!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        MenuManager.shutdown();

        getComponentLogger().info(Component.text("MenuAPI has been disabled!", NamedTextColor.RED));
    }

}
