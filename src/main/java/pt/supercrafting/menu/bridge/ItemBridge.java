package pt.supercrafting.menu.bridge;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class ItemBridge {

    private ItemBridge() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    @NotNull
    public static ItemStack asQuantity(@NotNull ItemStack itemStack, int quantity) {
        if (quantity <= 0) {
            return empty();
        }
        ItemStack newItemStack = itemStack.clone();
        newItemStack.setAmount(quantity);
        return newItemStack;
    }

    public static boolean isEmpty(@Nullable ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0;
    }

    @NotNull
    public static ItemStack empty() {
        return new ItemStack(Material.AIR);
    }

}
