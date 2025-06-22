package pt.supercrafting.menu;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pt.supercrafting.menu.editor.MenuEditor;
import pt.supercrafting.menu.editor.decoration.MenuDecoration;
import pt.supercrafting.menu.handler.MenuHandler;
import pt.supercrafting.menu.slot.ForbiddenSlot;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Menu {

    private final UUID id = UUID.randomUUID();

    private final Player player;

    protected final InventoryView view;
    //private final MenuView menuView;

    private final Inventory inventory;
    private boolean opened;

    private final Int2ObjectMap<MenuSlot> slots;
    private Int2ObjectMap<MenuSlot> slotView;

    private final Map<UUID, MenuEditor> editors = new HashMap<>(2);
    private UUID decoratorId;

    private final Map<UUID, MenuHandler> handler = new HashMap<>(2);

    final MenuClickProcessor clickProcessor;

    protected Menu(@NotNull Player player, @NotNull Component title, @NotNull MenuType type) {
        this.player = Objects.requireNonNull(player, "player cannot be null");

        this.view = Objects.requireNonNull(type, "type cannot be null")
                .create(
                    player,
                    Objects.requireNonNull(title, "title cannot be null")
                );
        //this.menuView = new MenuView(this.view, this);
        this.inventory = this.view.getTopInventory();

        int size = this.inventory.getSize();
        this.slots = new Int2ObjectArrayMap<>(size); // largest size

        MenuSlot fallBackSlot = fallBackSlot();
        for (int i = 0; i < size; i++)
            this.slots.put(i, fallBackSlot);

        this.clickProcessor = new MenuClickProcessor(this);
    }

    @NotNull
    protected MenuSlot fallBackSlot(){
        return ForbiddenSlot.INSTANCE;
    }

    public void refresh() {
        //this.handle.clear();

        this.inventory.clear();

        for (MenuEditor editor : this.editors.values())
            editor.edit(this, inventory, this.view);

        for (Int2ObjectMap.Entry<MenuSlot> entry : this.slots.int2ObjectEntrySet()) {

            int index = entry.getIntKey();
            MenuSlot slot = Objects.requireNonNullElse(entry.getValue(), fallBackSlot());
            inventory.setItem(index, slot.icon());
        }

    }

    @Nullable
    public InventoryView open() {

        if(!player.isOnline())
            return null;

        refresh();

        boolean wasViewer = this.opened;
        MenuManager.instance.currentMenus.put(player.getUniqueId(), this);
        //player.openInventory(this.menuView);

        InventoryView currentView = player.getOpenInventory();
        if(!currentView.equals(this.view))
            player.openInventory(this.view);

        if(!wasViewer)
            callHandler(handler -> handler.onOpen(player));
        this.opened = true;

        return this.view;
    }

    void close() {
        if (!this.opened)
            return;

        this.opened = false;

        callHandler(handler -> handler.onClose(this.player));
    }

    public void setSlot(int index, @Nullable MenuSlot slot) {
        checkOutOfBounds(index);
        this.slots.put(index, slot);
    }

    public @NotNull MenuSlot getSlot(int index) {
        checkOutOfBounds(index);
        return this.slots.getOrDefault(index, fallBackSlot());
    }

    public @NotNull @Unmodifiable Int2ObjectMap<MenuSlot> getSlots() {
        if(this.slotView == null)
            this.slotView = Int2ObjectMaps.unmodifiable(this.slots);
        return this.slotView;
    }

    public UUID registerEditor(@NotNull MenuEditor editor) {
        Objects.requireNonNull(editor);
        UUID id = UUID.randomUUID();
        this.editors.put(id, editor);
        return id;
    }

    public void unregisterEditor(@NotNull UUID id) {
        Objects.requireNonNull(id);
        this.editors.remove(id);
    }

    protected void decorate(@Nullable MenuDecoration decoration) {
        if(this.decoratorId != null) {
            unregisterEditor(this.decoratorId);
            this.decoratorId = null;
        }

        if (decoration != null)
            this.decoratorId = registerEditor(decoration);
    }

    @NotNull
    public UUID registerHandler(@NotNull MenuHandler handler) {
        Objects.requireNonNull(handler, "handler cannot be null");
        UUID id = UUID.randomUUID();
        this.handler.put(id, handler);
        return id;
    }

    public void unregisterHandler(@NotNull UUID id) {
        Objects.requireNonNull(id, "id cannot be null");
        this.handler.remove(id);
    }

    void callHandler(@NotNull Consumer<MenuHandler> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");

        for (MenuHandler handler : this.handler.values())
            consumer.accept(handler);
    }

    public int size() {
        return this.inventory.getSize();
    }

    private void checkOutOfBounds(int index) {
        if (index < 0 || index >= this.slots.size())
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for menu with size " + this.slots.size());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Menu menu)) return false;
        return Objects.equals(id, menu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
