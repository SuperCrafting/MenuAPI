package pt.supercrafting.menu;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pt.supercrafting.menu.editor.MenuEditor;
import pt.supercrafting.menu.editor.decoration.MenuDecoration;
import pt.supercrafting.menu.slot.ForbiddenSlot;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class Menu implements InventoryHolder {

    private final UUID id = UUID.randomUUID();

    private final Int2ObjectMap<MenuSlot> slots;
    private Int2ObjectMap<MenuSlot> slotView;

    private final Map<UUID, MenuEditor> editors = new HashMap<>();
    private UUID decoratorId;

    final MenuClickProcessor clickProcessor;
    private final Inventory handle;

    public Menu(@NotNull Component title, int rows) {
        this(title, InventoryType.CHEST, rows * 9);
    }

    public Menu(@NotNull Component title, @NotNull InventoryType type) {
        this(title, type, Objects.requireNonNull(type, "type").getDefaultSize());
    }

    @ApiStatus.Internal
    protected Menu(@NotNull Component title, @NotNull InventoryType type, int size) {

        this.slots = new Int2ObjectArrayMap<>(size);
        for (int i = 0; i < size; i++)
            this.slots.put(i, ForbiddenSlot.INSTANCE);

        this.clickProcessor = new MenuClickProcessor(this);

        if(type == InventoryType.CHEST)
            this.handle = Bukkit.createInventory(this, size, title);
        else
            this.handle = Bukkit.createInventory(this, type, title);
    }

    public void refresh() {
        this.handle.clear();

        for (MenuEditor editor : this.editors.values())
            editor.edit(this, this.handle);

        for (Int2ObjectMap.Entry<MenuSlot> entry : this.slots.int2ObjectEntrySet()) {

            int index = entry.getIntKey();
            MenuSlot slot = entry.getValue();
            if (slot != null)
                this.handle.setItem(index, slot.icon());
            else
                this.handle.setItem(index, ForbiddenSlot.INSTANCE.icon());
        }

    }

    public boolean open(@NotNull Player player) {
        Objects.requireNonNull(player, "player cannot be null");
        if (!player.isOnline())
            return false;

        refresh();

        player.openInventory(this.handle);
        return true;
    }

    public void setSlot(int index, @Nullable MenuSlot slot) {
        checkOutOfBounds(index);
        this.slots.put(index, slot);
    }

    public @NotNull MenuSlot getSlot(int index) {
        checkOutOfBounds(index);
        return this.slots.getOrDefault(index, ForbiddenSlot.INSTANCE);
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

    public int size() {
        return this.getInventory().getSize();
    }

    @Override
    public final @NotNull Inventory getInventory() {
        return handle;
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
