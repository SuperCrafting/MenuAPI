package pt.supercrafting.menu.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import pt.supercrafting.menu.MenuManager;

public final class MenuPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        MenuManager.init(this);

        getLogger().info("MenuAPI has been enabled!");
    }

    @Override
    public void onDisable() {
        MenuManager.shutdown();

        getLogger().info("MenuAPI has been disabled!");
    }

}
