package pt.supercrafting.menu.slot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.bridge.ItemBridge;

public record ForbiddenSlot(@NotNull ItemStack icon) implements MenuSlot {

    public static final ForbiddenSlot INSTANCE = new ForbiddenSlot();

    private ForbiddenSlot() {
        this(ItemBridge.empty());
    }

    @Override
    public @NotNull ItemStack icon() {
        return icon;
    }

    @Override
    public @NotNull ItemStack itemStack() {
        return ItemBridge.empty();
    }

    @Override
    public void itemStack(@NotNull ItemStack itemStack) {
        throw new UnsupportedOperationException("Cannot set itemStack on a ForbiddenSlot");
    }

    @Override
    public void take(Take take) {
        take.cancel();
    }

    @Override
    public boolean accept(@NotNull ItemStack itemStack) {
        return false;
    }

    @Override
    public void add(Add add) {
        add.cancel();
    }

}
