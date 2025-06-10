package pt.supercrafting.menu.item;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.Objects;

public class MenuItem implements MenuSlot {

    private final ItemStack icon;

    public MenuItem(@NotNull ItemStack icon) {
        this.icon = icon.clone();
    }

    public void click(Click click) {

    }

    public record Click(@NotNull Player player, int slot, @NotNull ItemStack cursor, @NotNull ClickType type) {

        public Click(@NotNull Player player, int slot, @NotNull ItemStack cursor, @NotNull ClickType type) {
            this.player = Objects.requireNonNull(player);
            this.slot = slot;
            this.cursor = Objects.requireNonNull(cursor);
            this.type = Objects.requireNonNull(type);
        }

        public static @NotNull Click from(@NotNull InventoryClickEvent event) {

            Player player = (Player) event.getWhoClicked();
            int slot = event.getSlot();
            ItemStack cursor = event.getCursor();
            ClickType type = event.getClick();

            return new Click(player, slot, cursor, type);
        }

    }

    @Override
    public @NotNull ItemStack icon() {
        return icon.clone();
    }

    @Override
    public @NotNull ItemStack itemStack() {
        return ItemStack.empty();
    }

    @Override
    public void itemStack(@NotNull ItemStack itemStack) {

    }

    protected final void superTake(@NotNull Take take) {
        MenuSlot.super.take(take);
    }

    @Override
    public final void take(Take take) {
        take.cancel();
    }

    protected final void superAccept(@NotNull ItemStack itemStack) {
        MenuSlot.super.accept(itemStack);
    }

    @Override
    public boolean accept(@NotNull ItemStack itemStack) {
        return false;
    }

    protected final void superAdd(@NotNull Add add) {
        MenuSlot.super.add(add);
    }

    @Override
    public void add(Add add) {
        add.cancel();
    }

}
