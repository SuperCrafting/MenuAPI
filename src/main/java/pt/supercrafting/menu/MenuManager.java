package pt.supercrafting.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pt.supercrafting.menu.editor.MenuUpdatable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuManager implements Listener, Runnable {

    static MenuManager instance;

    private final Plugin plugin;
    private BukkitTask task;

    final Map<UUID, Menu> currentMenus = new ConcurrentHashMap<>();

    private int ticks = 0;

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

        this.ticks++;
        for (Player player : Bukkit.getOnlinePlayers()) {

            InventoryView view = player.getOpenInventory();
            Menu menu = fromView(view);
            if(!(menu instanceof MenuUpdatable updatable))
                continue;

            int rate = updatable.updateRate();
            if(rate <= 0 || ticks % rate != 0)
                continue;

            updatable.update();
        }

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

        HumanEntity player = event.getPlayer();
        currentMenus.remove(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {

        Player player = event.getPlayer();
        Menu menu = currentMenus.remove(player.getUniqueId());
        if(menu != null)
            menu.clickProcessor.close(new InventoryCloseEvent(menu.view, InventoryCloseEvent.Reason.DISCONNECT));

    }

    private static @Nullable Menu fromView(@NotNull InventoryView view) {

        HumanEntity player = view.getPlayer();
        UUID playerId = player.getUniqueId();

        Menu menu = instance.currentMenus.get(playerId);
        return menu != null && menu.view.equals(view) ? menu : null;

        //return view instanceof Menu.MenuView menuView ? menuView.menu() : null;
        //return view.getTopInventory().getHolder() instanceof Menu menu ? menu : null;
    }

}
