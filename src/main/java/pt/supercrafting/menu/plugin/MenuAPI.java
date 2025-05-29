package pt.supercrafting.menu.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.Menu;
import pt.supercrafting.menu.MenuManager;
import pt.supercrafting.menu.item.MenuItem;
import pt.supercrafting.menu.slot.InventorySlot;

public final class MenuAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        MenuManager.init(this);

        getCommand("test").setExecutor(this);

        getComponentLogger().info(Component.text("MenuAPI has been enabled!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        MenuManager.shutdown();

        getComponentLogger().info(Component.text("MenuAPI has been disabled!", NamedTextColor.RED));
    }

    Inventory inventory = Bukkit.createInventory(null, 9);

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        if(!(sender instanceof Player player))
            return false;


        Menu menu = new ExampleMenu(inventory);
        menu.open(player);

        return true;
    }

    private static class ExampleMenu extends Menu {

        private boolean working = false;

        public ExampleMenu(Inventory inventory) {
            super(Component.text("Example menu"), 4);
            setSlot(4, new InventorySlot(inventory, 0));
            setSlot(5, new InventorySlot(inventory, 1));

            setSlot(9 * 4 - 1, new MenuItem(new ItemStack(Material.BARRIER)){
                @Override
                public void click(Click click) {
                    click.player().closeInventory();
                }
            });

            setSlot(9 * 4 - 2, new MenuItem(new ItemStack(Material.LEVER)){
                @Override
                public void click(Click click) {
                    working = !working;
                    click.player().sendMessage(Component.text("Working: " + working, NamedTextColor.YELLOW));
                }
            });
        }

    }

}
