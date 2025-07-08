package pt.supercrafting.menu;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.*;
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
import pt.supercrafting.menu.bridge.PaperBridge;
import pt.supercrafting.menu.item.MenuItem;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ApiStatus.Internal
final class MenuClickProcessor {

    private static final IntList PLAYER_INVENTORY_SLOTS;
    private static final IntList REVERSED_PLAYER_INVENTORY_SLOTS;

    private static final Set<InventoryAction> DEFAULT_BEHAVIORS;

    static {

        boolean hasBundleActions;
        try {
            InventoryAction.valueOf("PICKUP_ALL_INTO_BUNDLE");
            hasBundleActions = true;
        } catch (Exception e){
            hasBundleActions = false;
        }

        Set<InventoryAction> defaultBehaviors = new HashSet<>(Arrays.asList(
                InventoryAction.NOTHING,
                InventoryAction.PICKUP_ALL,
                InventoryAction.PICKUP_HALF,
                InventoryAction.PICKUP_ONE,
                InventoryAction.PICKUP_SOME,
                InventoryAction.PLACE_ALL,
                InventoryAction.PLACE_ONE,
                InventoryAction.PLACE_SOME,
                InventoryAction.HOTBAR_SWAP,
                InventoryAction.SWAP_WITH_CURSOR
        ));

        if(hasBundleActions) {
            defaultBehaviors.add(getInventoryAction("PLACE_ALL_INTO_BUNDLE"));
            defaultBehaviors.add(getInventoryAction("PICKUP_ALL_INTO_BUNDLE"));
            defaultBehaviors.add(getInventoryAction("PICKUP_SOME_INTO_BUNDLE"));
            defaultBehaviors.add(getInventoryAction("PLACE_SOME_INTO_BUNDLE"));
        }

        DEFAULT_BEHAVIORS = Collections.unmodifiableSet(defaultBehaviors);

        IntList prioritySlots = new IntArrayList(9 * 4);
        // First 9 slots are the hotbar
        for (int i = 8; i >= 0; i--)
            prioritySlots.add(i);

        // Next 36 slots are the main inventory
        for (int i = 9; i < 9 * 4; i++)
            prioritySlots.add(i);

        PLAYER_INVENTORY_SLOTS = IntLists.unmodifiable(prioritySlots);

        IntList reversedPrioritySlots = new IntArrayList(prioritySlots);
        Collections.reverse(reversedPrioritySlots);
        REVERSED_PLAYER_INVENTORY_SLOTS = IntLists.unmodifiable(reversedPrioritySlots);
    }

    @NotNull
    private static InventoryAction getInventoryAction(@NotNull String name) {
        try {
            return InventoryAction.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid InventoryAction name: " + name, e);
        }
    }

    private final Menu menu;
    private final Cache<UUID, Boolean> closeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(250, TimeUnit.MILLISECONDS)
            .build();

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

        if(event.getHotbarButton() != -1) {
            clickHotBar(event);
            return;
        }

        Player player = (Player) event.getWhoClicked();

        int index = event.getSlot();
        MenuSlot slot = menu.getSlot(index);

        ClickType clickType = event.getClick();
        ItemStack cursor = event.getCursor();
        boolean isAdd = !PaperBridge.isEmpty(cursor) && !clickType.isShiftClick();

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

                IntList slots = new IntArrayList(PLAYER_INVENTORY_SLOTS.size());
                for (int i = 0; i < inventory.getSize(); i++) {
                    ItemStack itemStack = inventory.getItem(i);
                    if(itemStack != null && !PaperBridge.isEmpty(itemStack) && itemStack.isSimilar(result))
                        slots.add(i);
                }

                for (int i : PLAYER_INVENTORY_SLOTS)
                    if(!slots.contains(i))
                        slots.add(i);

                for (int playerSlot : slots) {

                    ItemStack playerItem = inventory.getItem(playerSlot);
                    if(playerItem == null)
                        playerItem = PaperBridge.empty();

                    if(!PaperBridge.isEmpty(playerItem) && !playerItem.isSimilar(result))
                        continue;

                    int allowedToAdd = PaperBridge.isEmpty(playerItem) ? result.getMaxStackSize() : playerItem.getMaxStackSize() - playerItem.getAmount();
                    if(allowedToAdd <= 0)
                        continue;

                    int toAdd = Math.min(allowedToAdd, result.getAmount());
                    ItemStack newPlayerItem = PaperBridge.asQuantity(playerItem, playerItem.getAmount() + toAdd);
                    inventory.setItem(playerSlot, newPlayerItem);

                    result = PaperBridge.asQuantity(result, result.getAmount() - toAdd);
                    if(result.getAmount() <= 0)
                        break;

                }

                if(!PaperBridge.isEmpty(result)) {
                    MenuSlot.Add add = new MenuSlot.PlayerAdd(result, result.getAmount(), player);
                    slot.add(add);

                    ItemStack remaining = add.getResult();
                    if(!PaperBridge.isEmpty(remaining)) // Drop overflow items
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

    private void clickHotBar(@NotNull InventoryClickEvent event) {

        int hotbarButton = event.getHotbarButton();
        int index = event.getSlot();

        Player player = (Player) event.getWhoClicked();
        MenuSlot slot = menu.getSlot(index);

        MenuSlot.Take take = new MenuSlot.PlayerTake(MenuSlot.Take.Type.ALL, player);
        slot.take(take);

        ItemStack result = take.getResult();
        Inventory inventory = player.getInventory();

        ItemStack hotbarItem =inventory.getItem(hotbarButton);
        if(hotbarItem != null && PaperBridge.isEmpty(hotbarItem)) {

            MenuSlot.Add add = new MenuSlot.PlayerAdd(hotbarItem, hotbarItem.getAmount(), player);
            slot.add(add);

            ItemStack remaining = add.getResult();
            if(PaperBridge.isEmpty(remaining))
                remaining = PaperBridge.empty();

            inventory.setItem(hotbarButton, remaining);
        }

        hotbarItem = inventory.getItem(hotbarButton); // Updated
        if(hotbarItem == null || PaperBridge.isEmpty(hotbarItem))
            player.getInventory().setItem(hotbarButton, result);
        else {
            //player.give(hotbarItem);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(hotbarItem);
            for (ItemStack drop : overflow.values()) {
                if(!PaperBridge.isEmpty(drop)) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        menu.refresh();
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
                    playerItem = PaperBridge.empty();
                if(PaperBridge.isEmpty(playerItem) || !playerItem.isSimilar(cursor))
                    continue;

                int allowedToAdd = cursor.getMaxStackSize() - cursor.getAmount();
                if(allowedToAdd <= 0)
                    continue;

                int toAdd = Math.min(allowedToAdd, playerItem.getAmount());
                ItemStack newCursor = PaperBridge.asQuantity(cursor, cursor.getAmount() + toAdd);

                ItemStack newPlayerItem = PaperBridge.asQuantity(playerItem, playerItem.getAmount() - toAdd);
                playerInventory.setItem(slot, newPlayerItem);
                cursor = newCursor;

            }

            player.setItemOnCursor(cursor);
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        if(currentItem == null || PaperBridge.isEmpty(currentItem))
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

            Inventory inventory = slot < view.getTopInventory().getSize() ? view.getTopInventory() : view.getBottomInventory();
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
        if(PaperBridge.isEmpty(cursor))
            return;

        event.setCancelled(false);

        boolean single = event.getType() == DragType.SINGLE;
        int amountPerSlot = single ? 1 : (int) Math.floor((double) cursor.getAmount() / draggedSlots.size());
        int remaining = single ? cursor.getAmount() - draggedSlots.size() : cursor.getAmount() % draggedSlots.size();

        Player player = (Player) event.getWhoClicked();
        List<ItemStack> overFlow = new ArrayList<>(draggedSlots.size());

        int giveBack = remaining;
        for (int slot : draggedSlots) {

            MenuSlot menuSlot = menu.getSlot(slot);

            MenuSlot.Add add = new MenuSlot.PlayerAdd(cursor, amountPerSlot, player);
            menuSlot.add(add);

            ItemStack result = add.getResult();
            if(!PaperBridge.isEmpty(result) && !result.isSimilar(cursor)) {
                overFlow.add(result);
            } else if(result.isSimilar(cursor) && !add.isSuccessful())
                giveBack += amountPerSlot;

        }

        ItemStack newCursor = PaperBridge.asQuantity(cursor, giveBack);
        event.setCursor(newCursor);

        Plugin plugin = MenuManager.instance.getPlugin();
        //player.getScheduler().runDelayed(plugin, (s) -> menu.refresh(), () -> {}, 3);

        if(!overFlow.isEmpty()) {
            //player.give(overFlow);
            for (ItemStack itemStack : overFlow) {
                if(!PaperBridge.isEmpty(itemStack)) {
                    player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, menu::refresh, 1); // Think about a better way to do this

    }

    public void close(@NotNull InventoryCloseEvent event) {

        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        if(closeCache.getIfPresent(playerId) != null)
            return;

        closeCache.put(playerId, true);

        menu.callHandler(menuHandler -> menuHandler.onClose(player));
    }

}
