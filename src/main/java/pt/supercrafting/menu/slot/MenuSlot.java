package pt.supercrafting.menu.slot;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface MenuSlot {

    // Content item (behind icon)

    @Contract("-> new")
    @NotNull
    ItemStack itemStack();
    void itemStack(@NotNull ItemStack itemStack);

    // Displayed item

    @NotNull
    default ItemStack icon() {
        return itemStack();
    }

    // Take

    default void take(Take take) {

        ItemStack itemStack = itemStack();
        if(itemStack.isEmpty()) {
            take.cancel();
            return;
        }

        int toRemove = Math.min(itemStack.getAmount(), itemStack.getMaxStackSize());
        if(take.getType() == Take.Type.HALF)
            toRemove = (int) Math.ceil(toRemove / 2.0d);

        ItemStack result = itemStack.asQuantity(toRemove);
        take.setResult(result);

        ItemStack newItemStack = itemStack.asQuantity(itemStack.getAmount() - toRemove);
        itemStack(newItemStack);

    }

    sealed class Take permits PlayerTake {

        private final Type type;
        private ItemStack result;
        private boolean successful = false;

        public Take(@NotNull Type type) {
            this.type = Objects.requireNonNull(type);
            this.result = ItemStack.empty();
        }

        public void cancel() {
            setResult(ItemStack.empty());
            successful = false;
        }

        @NotNull
        public Type getType() {
            return type;
        }

        @Contract("-> new")
        @NotNull
        public ItemStack getResult() {
            return result.clone();
        }

        public void setResult(@NotNull ItemStack result) {
            this.result = Objects.requireNonNull(result).clone();
            this.successful = true;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public enum Type {
            ALL,
            HALF,
        }

    }

    final class PlayerTake extends Take {

        private final Player player;

        public PlayerTake(@NotNull Type type, @NotNull Player player) {
            super(type);
            this.player = Objects.requireNonNull(player);
        }

        @NotNull
        public Player getPlayer() {
            return player;
        }

    }

    // Add

    default boolean accept(@NotNull ItemStack itemStack) {
        ItemStack current = itemStack();
        return current.isEmpty() || current.isSimilar(itemStack);
    }

    default void add(Add add) {

        ItemStack itemStack = add.getItemStack();
        if(!accept(itemStack)) {
            add.cancel();
            return;
        }

        int amount = add.getAmount();
        ItemStack current = itemStack();

        if(current.isEmpty()) {

            ItemStack newItemStack = itemStack.asQuantity(amount);
            itemStack(newItemStack);

            ItemStack result = ItemStack.empty();
            add.setResult(result);
            return;
        }

        int canAdd = Math.min(current.getMaxStackSize() - current.getAmount(), amount);
        if(canAdd <= 0) {
            add.cancel();
            return;
        }

        ItemStack newItemStack = current.asQuantity(current.getAmount() + canAdd);
        itemStack(newItemStack);

        ItemStack result = itemStack.asQuantity(itemStack.getAmount() - canAdd);
        if(result.isEmpty())
            result = ItemStack.empty();

        add.setResult(result);

    }

    sealed class Add permits PlayerAdd {

        private final ItemStack itemStack;
        private final int amount;
        private ItemStack result;
        private boolean successful;

        public Add(@NotNull ItemStack itemStack, int amount) {
            this.itemStack = Objects.requireNonNull(itemStack);
            this.amount = amount;
            this.result = ItemStack.empty();
        }

        public void cancel() {
            setResult(itemStack);
            successful = false;
        }

        @Contract("-> new")
        @NotNull
        public ItemStack getItemStack() {
            return itemStack.clone();
        }

        public int getAmount() {
            return amount;
        }

        @Contract("-> new")
        @NotNull
        public ItemStack getResult() {
            return result.clone();
        }

        public void setResult(@NotNull ItemStack result) {
            this.result = Objects.requireNonNull(result).clone();
            this.successful = true;
        }

        public boolean isSuccessful() {
            return successful;
        }

    }

    final class PlayerAdd extends Add {

        private final Player player;

        public PlayerAdd(@NotNull ItemStack itemStack, int amount, Player player) {
            super(itemStack, amount);
            this.player = Objects.requireNonNull(player);
        }

        @NotNull
        public Player getPlayer() {
            return player;
        }

    }

}
