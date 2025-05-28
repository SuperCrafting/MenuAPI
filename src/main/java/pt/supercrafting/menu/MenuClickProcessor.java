package pt.supercrafting.menu;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

@ApiStatus.Internal
final class MenuClickProcessor {

    private static final Set<InventoryAction> DEFAULT_BEHAVIORS = Set.of(
            InventoryAction.NOTHING,
            InventoryAction.PICKUP_ALL,
            InventoryAction.PICKUP_ALL_INTO_BUNDLE,
            InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_ONE,
            InventoryAction.PICKUP_SOME,
            InventoryAction.PICKUP_SOME_INTO_BUNDLE,
            InventoryAction.PLACE_ALL,
            InventoryAction.PLACE_ALL_INTO_BUNDLE,
            InventoryAction.PLACE_ONE,
            InventoryAction.PLACE_SOME,
            InventoryAction.PLACE_SOME_INTO_BUNDLE,
            InventoryAction.HOTBAR_SWAP,
            InventoryAction.SWAP_WITH_CURSOR
    );

    private final Menu menu;

    public MenuClickProcessor(@NotNull Menu menu) {
        this.menu = Objects.requireNonNull(menu);
    }

    public void click(@NotNull InventoryClickEvent event) {

        Inventory clickedInventory = event.getClickedInventory();
        if(clickedInventory == null)
            return;

        if(clickedInventory.getHolder() instanceof Menu)
            clickMenu(event);
        else if(clickedInventory instanceof PlayerInventory) // Player click on their own inventory
            clickInventory(event);
    }

    private void clickMenu(@NotNull InventoryClickEvent event) {

    }

    private void clickInventory(@NotNull InventoryClickEvent event) {

        ItemStack currentItem = event.getCurrentItem();
        if(currentItem == null || currentItem.isEmpty())
            return;

        InventoryAction action = event.getAction();
        ClickType clickType = event.getClick();

        if(DEFAULT_BEHAVIORS.contains(action))
            return;

        if(action == InventoryAction.COLLECT_TO_CURSOR) {
            // Todo: recode this behavior
            return;
        }

    }

    public void drag(@NotNull InventoryDragEvent event) {

        IntSet slots = new IntArraySet(event.getRawSlots());
        Multimap<Inventory, Integer> slotsByInventory = HashMultimap.create();

        InventoryView view = event.getView();
        for (int slot : slots) {

            Inventory inventory = view.getInventory(slot);
            if (inventory == null)
                continue;

            slotsByInventory.put(inventory, slot);
        }

        Inventory menuInventory = view.getTopInventory();
        if(!slotsByInventory.containsKey(menuInventory)) // Only drags player inventory
            return;

    }

    public void close(@NotNull InventoryCloseEvent event) {

    }

}
