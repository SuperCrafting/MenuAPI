package pt.supercrafting.menu;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.item.MenuItem;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.*;

@ApiStatus.Internal
final class MenuClickProcessor {

    private static final IntList PLAYER_INVENTORY_SLOTS;
    private static final IntList REVERSED_PLAYER_INVENTORY_SLOTS;

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

    static {

        IntList prioritySlots = new IntArrayList(9 * 4);
        // First 9 slots are the hotbar
        for (int i = 9; i > 0; i--)
            prioritySlots.add(i);

        // Next 36 slots are the main inventory
        for (int i = 9; i < 9 * 4; i++)
            prioritySlots.add(i);

        PLAYER_INVENTORY_SLOTS = IntLists.unmodifiable(prioritySlots);

        IntList reversedPrioritySlots = new IntArrayList(prioritySlots);
        Collections.reverse(reversedPrioritySlots);
        REVERSED_PLAYER_INVENTORY_SLOTS = IntLists.unmodifiable(reversedPrioritySlots);
    }

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

        if(event.getAction() == InventoryAction.CLONE_STACK)
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        int index = event.getSlot();
        MenuSlot slot = menu.getSlot(index);

        ClickType clickType = event.getClick();
        ItemStack cursor = event.getCursor();
        boolean isAdd = !cursor.isEmpty() && !clickType.isShiftClick();

        boolean handled;
        if(isAdd) {

            int amount = clickType.isLeftClick() ? cursor.getAmount() : 1;
            MenuSlot.Add add = new MenuSlot.PlayerAdd(cursor, amount, player);
            slot.add(add);

            cursor = add.getResult();

            handled = add.isSuccessful();

        } else {

            MenuSlot.Take.Type type = clickType.isLeftClick() || clickType.isShiftClick() ? MenuSlot.Take.Type.ALL : MenuSlot.Take.Type.HALF;
            MenuSlot.Take take = new MenuSlot.PlayerTake(type, player);
            slot.take(take);

            boolean toCursor = !clickType.isShiftClick();
            if(toCursor) {
                cursor = take.getResult();
            } else {

                ItemStack result = take.getResult();
                Inventory inventory = player.getInventory();
                for (int playerSlot : PLAYER_INVENTORY_SLOTS) {

                    ItemStack playerItem = inventory.getItem(playerSlot);
                    if(playerItem == null)
                        playerItem = ItemStack.empty();

                    if(!playerItem.isEmpty() && !playerItem.isSimilar(result))
                        continue;


                    int allowedToAdd = playerItem.isEmpty() ? result.getMaxStackSize() : playerItem.getMaxStackSize() - playerItem.getAmount();
                    if(allowedToAdd <= 0)
                        continue;

                    int toAdd = Math.min(allowedToAdd, result.getAmount());
                    ItemStack newPlayerItem = result.asQuantity(playerItem.getAmount() + toAdd);
                    inventory.setItem(playerSlot, newPlayerItem);

                    result = result.asQuantity(result.getAmount() - toAdd);
                    if(result.getAmount() <= 0)
                        break;

                }

                if(!result.isEmpty()) {
                    MenuSlot.Add add = new MenuSlot.PlayerAdd(result, result.getAmount(), player);
                    slot.add(add);

                    ItemStack remaining = add.getResult();
                    if(!remaining.isEmpty()) // Drop overflow items
                        player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                }

            }

            handled = take.isSuccessful();

        }

        if(handled) {
            player.setItemOnCursor(cursor);
            menu.refresh();
        } else if(slot instanceof MenuItem menuItem) {

            MenuItem.Click click = MenuItem.Click.from(event);
            menuItem.click(click);

        }

    }

    private void clickInventory(@NotNull InventoryClickEvent event) {

        InventoryAction action = event.getAction();
        ClickType clickType = event.getClick();

        if(clickType == ClickType.DOUBLE_CLICK && action == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            Inventory playerInventory = player.getInventory();

            ItemStack cursor = event.getCursor();
            for (int slot : REVERSED_PLAYER_INVENTORY_SLOTS) {

                ItemStack playerItem = playerInventory.getItem(slot);
                if(playerItem == null)
                    playerItem = ItemStack.empty();
                if(playerItem.isEmpty() || !playerItem.isSimilar(cursor))
                    continue;

                int allowedToAdd = cursor.getMaxStackSize() - cursor.getAmount();
                if(allowedToAdd <= 0)
                    continue;

                int toAdd = Math.min(allowedToAdd, playerItem.getAmount());
                ItemStack newCursor = cursor.asQuantity(cursor.getAmount() + toAdd);

                ItemStack newPlayerItem = playerItem.asQuantity(playerItem.getAmount() - toAdd);
                playerInventory.setItem(slot, newPlayerItem);
                cursor = newCursor;

            }

            player.setItemOnCursor(cursor);
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if(currentItem == null || currentItem.isEmpty())
            return;

        if(DEFAULT_BEHAVIORS.contains(action))
            return;

        if(action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);

            int toMove = currentItem.getAmount();
            if(toMove <= 0)
                return;

            ItemStack itemStack = currentItem.clone();
            Player player = (Player) event.getWhoClicked();
            boolean handled = false;

            for (Int2ObjectMap.Entry<MenuSlot> entry : menu.getSlots().int2ObjectEntrySet()) {

                MenuSlot slot = entry.getValue();

                MenuSlot.Add add = new MenuSlot.PlayerAdd(itemStack, toMove, player);
                slot.add(add);

                handled = handled || add.isSuccessful();

                ItemStack remaining = add.getResult();

                itemStack = remaining;
                toMove = remaining.getAmount();

                if(toMove <= 0)
                    break;

            }

            if(handled)
                menu.refresh();

            event.setCurrentItem(itemStack);
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

        event.setCancelled(true);

        Inventory playerInventory = view.getBottomInventory();
        if(slotsByInventory.containsKey(playerInventory) && slotsByInventory.containsKey(menuInventory))
            return;

        IntList draggedSlots = new IntArrayList(slotsByInventory.get(menuInventory));
        ItemStack cursor = event.getOldCursor();
        if(cursor.isEmpty())
            return;

        event.setCancelled(false);

        int amountPerSlot = (int) Math.floor((double) cursor.getAmount() / draggedSlots.size());
        int remaining = cursor.getAmount() % draggedSlots.size();

        Player player = (Player) event.getWhoClicked();
        List<ItemStack> overFlow = new ArrayList<>(draggedSlots.size());

        int giveBack = remaining;
        for (int slot : draggedSlots) {

            MenuSlot menuSlot = menu.getSlot(slot);

            MenuSlot.Add add = new MenuSlot.PlayerAdd(cursor, amountPerSlot, player);
            menuSlot.add(add);

            ItemStack result = add.getResult();
            if(!result.isEmpty() && !result.isSimilar(cursor)) {
                overFlow.add(result);
            } else if(result.isSimilar(cursor) && !add.isSuccessful())
                giveBack += amountPerSlot;

        }

        ItemStack newCursor = cursor.asQuantity(giveBack);
        event.setCursor(newCursor);

        Plugin plugin = MenuManager.instance.getPlugin();
        player.getScheduler().runDelayed(plugin, (s) -> {}, menu::refresh, 1);
        //Bukkit.getScheduler().runTaskLater(MenuManager.instance.getPlugin(), menu::refresh, 1);

    }

    public void close(@NotNull InventoryCloseEvent event) {

    }

}
