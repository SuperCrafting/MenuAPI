package pt.supercrafting.menu.slot;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import pt.supercrafting.menu.bridge.ItemBridge;

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
        if(ItemBridge.isEmpty(itemStack)) {
            take.cancel();
            return;
        }

        int toRemove = Math.min(itemStack.getAmount(), itemStack.getMaxStackSize());
        if(take.getType() == Take.Type.HALF)
            toRemove = (int) Math.ceil(toRemove / 2.0d);

        ItemStack result = ItemBridge.asQuantity(itemStack, toRemove);
        take.setResult(result);

        ItemStack newItemStack = ItemBridge.asQuantity(itemStack, itemStack.getAmount() - toRemove);
        itemStack(newItemStack);

    }

    sealed class Take permits PlayerTake {

        private final Type type;
        private ItemStack result;
        private boolean successful = false;

        public Take(@NotNull Type type) {
            this.type = Objects.requireNonNull(type);
            this.result = ItemBridge.empty();
        }

        public void cancel() {
            setResult(ItemBridge.empty());
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
        return ItemBridge.isEmpty(current) || current.isSimilar(itemStack);
    }

    default void add(Add add) {

        ItemStack itemStack = add.getItemStack();
        if(!accept(itemStack)) {
            add.cancel();
            return;
        }

        int amount = add.getAmount();
        ItemStack current = itemStack();

        if(ItemBridge.isEmpty(current)) {

            ItemStack newItemStack = ItemBridge.asQuantity(itemStack, amount);
            itemStack(newItemStack);

            ItemStack result = ItemBridge.asQuantity(itemStack, itemStack.getAmount() - amount);
            add.setResult(result);
            return;
        }

        int canAdd = Math.min(current.getMaxStackSize() - current.getAmount(), amount);
        if(canAdd <= 0) {
            add.cancel();
            return;
        }

        ItemStack newItemStack = ItemBridge.asQuantity(current, current.getAmount() + canAdd);
        itemStack(newItemStack);

        ItemStack result = ItemBridge.asQuantity(itemStack, itemStack.getAmount() - canAdd);
        if(ItemBridge.isEmpty(result))
            result = ItemBridge.empty();

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
            this.result = ItemBridge.empty();
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
