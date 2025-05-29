package pt.supercrafting.menu.plugin;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.Menu;
import pt.supercrafting.menu.MenuManager;
import pt.supercrafting.menu.editor.decoration.MenuDecoration;
import pt.supercrafting.menu.editor.pagination.MenuPagination;
import pt.supercrafting.menu.item.MenuItem;
import pt.supercrafting.menu.slot.ForbiddenSlot;
import pt.supercrafting.menu.slot.InventorySlot;

public final class MenuAPI extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        MenuManager.init(this);

        getCommand("test").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);

        getComponentLogger().info(Component.text("MenuAPI has been enabled!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        MenuManager.shutdown();

        getComponentLogger().info(Component.text("MenuAPI has been disabled!", NamedTextColor.RED));
    }

    Inventory inventory = Bukkit.createInventory(null, 9);

    @EventHandler
    public void onClick(PlayerInteractEvent event) {

        if(!event.hasBlock())
            return;

        Block block = event.getClickedBlock();
        BlockState state = block.getState();
        if(!(state instanceof InventoryHolder holder))
            return;

        inventory = holder.getInventory();
        event.getPlayer().sendMessage("Linking inventory");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {

        if(!(sender instanceof Player player))
            return false;


        Menu menu = new ExampleMenu(inventory);
        menu.open(player);

        return true;
    }

    private static class ExampleMenu extends Menu {

        public ExampleMenu(Inventory inventory) {
            super(Component.text("Example menu"), 5);
            decorate(MenuDecoration.flat(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

            MenuPagination pagination = MenuPagination.full(this);
            for (int i = 0; i < 100; i++) {
                ItemStack itemStack = new ItemStack(Material.STONE, i + 1);
                itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, Math.min(99, i + 1));
                pagination.addItem(new ForbiddenSlot(itemStack));
            }
            registerEditor(pagination);

            setSlot(4, new InventorySlot(inventory, 0));
            setSlot(5, new InventorySlot(inventory, 1));

            setSlot(9 * 4 - 1, new MenuItem(new ItemStack(Material.BARRIER)){
                @Override
                public void click(Click click) {
                    click.player().closeInventory();
                }
            });
        }

    }

}
