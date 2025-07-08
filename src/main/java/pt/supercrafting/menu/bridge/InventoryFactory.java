package pt.supercrafting.menu.bridge;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentEncoder;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InventoryFactory {

    @NotNull Inventory createInventory(@Nullable InventoryHolder holder, @NotNull Component title, @NotNull InventoryType type);
    @NotNull Inventory createInventory(@Nullable InventoryHolder holder, @NotNull Component title, int size);

    @SuppressWarnings("deprecation")
    record Legacy(@NotNull ComponentEncoder<Component, String> encoder) implements InventoryFactory {

        @Override
        public @NotNull Inventory createInventory(@Nullable InventoryHolder holder, @NotNull Component title, @NotNull InventoryType type) {
            return Bukkit.createInventory(holder, type, encoder.serialize(title));
        }

        @Override
        public @NotNull Inventory createInventory(@Nullable InventoryHolder holder, @NotNull Component title, int size) {
            return Bukkit.createInventory(holder, size, encoder.serialize(title));
        }

    }

    record Paper() implements InventoryFactory {

        @Override
        public @NotNull Inventory createInventory(@Nullable InventoryHolder holder, @NotNull Component title, @NotNull InventoryType type) {
            return Bukkit.createInventory(holder, type, title);
        }

        @Override
        public @NotNull Inventory createInventory(@Nullable InventoryHolder holder, @NotNull Component title, int size) {
            return Bukkit.createInventory(holder, size, title);
        }

    }

}
