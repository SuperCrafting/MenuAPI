package pt.supercrafting.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class MenuManager implements Listener, Runnable {

    static MenuManager instance;

    private final Plugin plugin;
    private BukkitTask task;

    private MenuManager(@NotNull Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, this, 1L, 1L);
    }

    public static void init(@NotNull Plugin plugin) {
        if(instance != null)
            return;

        instance = new MenuManager(plugin);
    }

    public static void shutdown() {
        if(instance == null)
            return;

        HandlerList.unregisterAll(instance);

        instance.task.cancel();
        instance.task = null;

        instance = null;
    }

    @ApiStatus.Internal
    @NotNull
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void run() {

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {

        Menu menu = fromView(event.getView());
        if(menu == null)
            return;

        menu.clickProcessor.click(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {

        Menu menu = fromView(event.getView());
        if(menu == null)
            return;

        menu.clickProcessor.drag(event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {

        Menu menu = fromView(event.getView());
        if(menu == null)
            return;

        menu.clickProcessor.close(event);
    }

    private static @Nullable Menu fromView(@NotNull InventoryView view) {
        return view.getTopInventory().getHolder() instanceof Menu menu ? menu : null;
    }

}
