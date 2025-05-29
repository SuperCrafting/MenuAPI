package pt.supercrafting.menu.slot;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class InventorySlot implements MenuSlot {

    private final Inventory inventory;
    private final int index;

    public InventorySlot(@NotNull Inventory inventory, int index) {
        this.inventory = Objects.requireNonNull(inventory);
        this.index = index;
    }

    @Override
    public @NotNull ItemStack itemStack() {
        ItemStack itemStack = inventory.getItem(index);
        return Objects.requireNonNullElse(itemStack, ItemStack.empty());
    }

    @Override
    public void itemStack(@Nullable ItemStack itemStack) {
        inventory.setItem(index, Objects.requireNonNullElse(itemStack, ItemStack.empty()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InventorySlot that)) return false;
        return index == that.index && Objects.equals(inventory, that.inventory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inventory, index);
    }

}
