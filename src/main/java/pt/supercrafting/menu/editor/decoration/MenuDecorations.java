package pt.supercrafting.menu.editor.decoration;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.Menu;
import pt.supercrafting.menu.slot.ForbiddenSlot;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.Objects;
import java.util.stream.IntStream;

abstract class MenuDecorations implements MenuDecoration {

    private final MenuSlot slot;

    public MenuDecorations(@NotNull ItemStack icon) {
        Objects.requireNonNull(icon);

        ItemStack noHover = icon.clone();
        noHover.editMeta(meta -> meta.setHideTooltip(true));
        this.slot = new ForbiddenSlot(noHover);
    }

    protected abstract int @NotNull[] generateSlots(int rows);

    @Override
    public final void edit(@NotNull Menu menu, @NotNull Inventory inventory) {

        if(inventory.getType() != InventoryType.CHEST)
            throw new IllegalArgumentException("Inventory type must be CHEST");

        int rows = inventory.getSize() / 9;
        int[] slots = generateSlots(rows);

        for (int index : slots) {

            MenuSlot oldSlot = menu.getSlot(index);
            if(oldSlot != ForbiddenSlot.INSTANCE)
                continue;

            menu.setSlot(index, this.slot);

        }

    }

    public static class Full extends MenuDecorations {

        public Full(@NotNull ItemStack icon) {
            super(icon);
        }

        @Override
        protected int @NotNull [] generateSlots(int rows) {
            return IntStream.range(0, rows * 9)
                    .filter(i -> i < 9 || i % 9 == 0 || i % 9 == 8 || i >= (rows - 1) * 9)
                    .toArray();
        }

    }

    public static class Corner extends MenuDecorations {

        public Corner(@NotNull ItemStack icon) {
            super(icon);
        }

        @Override
        protected int @NotNull [] generateSlots(int rows) {
            int size = rows * 9;
            return IntStream.range(0, size).filter(i -> i < 2 || (i > 6 && i < 10)
                    || i == 17 || i == size - 18
                    || (i > size - 11 && i < size - 7) || i > size - 3).toArray();
        }

    }

    public static class Flat extends MenuDecorations {

        private static final int SIZE = 2;

        public Flat(@NotNull ItemStack icon) {
            super(icon);
        }

        @Override
        protected int @NotNull [] generateSlots(int rows) {

            int size = rows * 9;
            boolean singleLine = size < 9;
            int[] slots = new int[!singleLine ? SIZE  * 2 * 4 : 6];

            int index = 0;

            // Top
            for (int i = 0; i < SIZE; i++)
                slots[index++] = i;
            for (int i = 1; i <= SIZE; i++)
                slots[index++] = 9 - i;

            if(!singleLine) {

                // Bottom
                for (int i = 1; i <= SIZE; i++)
                    slots[index++] = size - i;

                for (int i = 0; i < SIZE; i++)
                    slots[index++] = size - 9 + i;

            }

            return slots;
        }

    }

}
