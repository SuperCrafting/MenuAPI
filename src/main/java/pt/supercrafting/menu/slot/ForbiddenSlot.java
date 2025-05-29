package pt.supercrafting.menu.slot;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record ForbiddenSlot(@NotNull ItemStack icon) implements MenuSlot {

    public static final ForbiddenSlot INSTANCE = new ForbiddenSlot();

    private ForbiddenSlot() {
        this(ItemStack.empty());
    }

    @Override
    public @NotNull ItemStack icon() {
        return icon;
    }

    @Override
    public void icon(@NotNull ItemStack itemStack) {

    }

    @Override
    public @NotNull ItemStack itemStack() {
        return ItemStack.empty();
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
