package pt.supercrafting.menu.bridge;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class PaperBridge {

    private static final boolean PAPER;
    private static final InventoryFactory INVENTORY_FACTORY;

    private PaperBridge() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    static {
        boolean paper;
        try {
            ItemStack.class.getMethod("isEmpty");
            paper = true;
        } catch (NoSuchMethodException e) {
            paper = false;
        }

        PAPER = paper;
        if (paper) {
            INVENTORY_FACTORY = new InventoryFactory.Paper();
        } else {
            INVENTORY_FACTORY = new InventoryFactory.Legacy(LegacyComponentSerializer.legacySection());
        }
    }

    @NotNull
    public static ItemStack asQuantity(@NotNull ItemStack itemStack, int quantity) {

        if(PAPER)
            return itemStack.asQuantity(quantity);

        if (quantity <= 0) {
            return empty();
        }
        ItemStack newItemStack = itemStack.clone();
        newItemStack.setAmount(quantity);
        return newItemStack;
    }

    public static boolean isEmpty(@Nullable ItemStack itemStack) {
        if(itemStack != null && PAPER)
            return itemStack.isEmpty();
        return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() <= 0;
    }

    @NotNull
    public static ItemStack empty() {
        if(PAPER)
            return ItemStack.empty();
        return new ItemStack(Material.AIR);
    }

    public static @NotNull InventoryFactory getInventoryFactory() {
        return INVENTORY_FACTORY;
    }

}
