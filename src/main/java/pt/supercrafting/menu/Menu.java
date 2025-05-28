package pt.supercrafting.menu;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.Objects;
import java.util.UUID;

public abstract class Menu implements InventoryHolder {

    private final UUID id = UUID.randomUUID();

    private final Int2ObjectMap<MenuSlot> slots;
    private Int2ObjectMap<MenuSlot> slotView;

    final MenuClickProcessor clickProcessor;

    private final Inventory handle;

    public Menu(@NotNull Component title, int rows) {
        this(title, InventoryType.CHEST, rows * 9);
    }

    public Menu(@NotNull Component title, @NotNull InventoryType type) {
        this(title, type, Objects.requireNonNull(type, "type").getDefaultSize());
    }

    private Menu(@NotNull Component title, @NotNull InventoryType type, int size) {

        this.slots = new Int2ObjectArrayMap<>(size);

        this.clickProcessor = new MenuClickProcessor(this);

        if(type == InventoryType.CHEST)
            this.handle = Bukkit.createInventory(this, size, title);
        else
            this.handle = Bukkit.createInventory(this, type, title);
    }

    public void setSlot(int index, @Nullable MenuSlot slot) {
        checkOutOfBounds(index);
        this.slots.put(index, slot);
    }

    public @Nullable MenuSlot getSlot(int index) {
        checkOutOfBounds(index);
        return this.slots.get(index);
    }

    public @NotNull @Unmodifiable Int2ObjectMap<MenuSlot> getSlots() {
        if(this.slotView == null)
            this.slotView = Int2ObjectMaps.unmodifiable(this.slots);
        return this.slotView;
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
