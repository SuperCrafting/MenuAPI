# MenuAPI

MenuAPI is a Java library designed for creating and managing interactive and dynamic menus in applications using Minecraft. This API includes advanced features such as pagination, decorators, editors, and interactive items.

## Key Features

- **Pagination**: Efficiently manage large numbers of items with a paging system.
- **Decorators**: Add aesthetic elements to your menus.
- **Editors**: Dynamically modify menu content.
- **Interactive Slots**: Create slots linked to another inventory or interactive items.

## Examples

### Basic Menu Example

A simple menu with a title and rows:

```java
public class SimpleMenu extends Menu {

    public SimpleMenu() {
        super(Component.text("My Simple Menu"), 3); // 3 rows (27 slots)
    }
}
```

To open the menu for a player:

```java
SimpleMenu menu = new SimpleMenu();
menu.open(player);
```

### MenuItem Examples

MenuItem allows you to create interactive items with custom click behaviors.

#### Example 1: Close Button

```java
setSlot(26, new MenuItem(new ItemStack(Material.BARRIER)) {
    @Override
    public void click(Click click) {
        click.player().closeInventory();
    }
});
```

#### Example 2: Teleport Button

```java
ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
ItemMeta meta = teleportItem.getItemMeta();
meta.displayName(Component.text("Teleport to Spawn"));
teleportItem.setItemMeta(meta);

setSlot(13, new MenuItem(teleportItem) {
    @Override
    public void click(Click click) {
        Player player = click.player();
        player.teleport(player.getWorld().getSpawnLocation());
        player.sendMessage(Component.text("Teleported to spawn!"));
        player.closeInventory();
    }
});
```

#### Example 3: Item Giving Button

```java
ItemStack giveItem = new ItemStack(Material.DIAMOND);
ItemMeta meta = giveItem.getItemMeta();
meta.displayName(Component.text("Receive Diamonds"));
giveItem.setItemMeta(meta);

setSlot(14, new MenuItem(giveItem) {
    @Override
    public void click(Click click) {
        Player player = click.player();
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 5));
        player.sendMessage(Component.text("You received 5 diamonds!"));
    }
});
```

### MenuEditor Example

MenuEditor allows you to dynamically modify menu content. Here's an example of a custom editor that displays player statistics:

```java
public class PlayerStatsEditor implements MenuEditor {
    
    private final Player player;
    
    public PlayerStatsEditor(Player player) {
        this.player = player;
    }
    
    @Override
    public void edit(@NotNull Menu menu, @NotNull Inventory inventory) {
        // Create health indicator
        ItemStack healthItem = new ItemStack(Material.RED_DYE, 
            (int) Math.ceil(player.getHealth()));
        ItemMeta healthMeta = healthItem.getItemMeta();
        healthMeta.displayName(Component.text("Health: " + player.getHealth()));
        healthItem.setItemMeta(healthMeta);
        inventory.setItem(0, healthItem);
        
        // Create food indicator
        ItemStack foodItem = new ItemStack(Material.COOKED_BEEF, 
            Math.max(1, player.getFoodLevel()));
        ItemMeta foodMeta = foodItem.getItemMeta();
        foodMeta.displayName(Component.text("Food: " + player.getFoodLevel()));
        foodItem.setItemMeta(foodMeta);
        inventory.setItem(1, foodItem);
        
        // Create experience indicator
        ItemStack expItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta expMeta = expItem.getItemMeta();
        expMeta.displayName(Component.text("Level: " + player.getLevel()));
        expItem.setItemMeta(expMeta);
        inventory.setItem(2, expItem);
    }
}
```

Usage in a menu:

```java
public class StatsMenu extends Menu {
    
    public StatsMenu(Player player) {
        super(Component.text("Player Stats"), 1);
        
        // Register the custom editor
        registerEditor(new PlayerStatsEditor(player));
    }
}
```

### MenuPagination Examples

MenuPagination helps manage large collections of items across multiple pages.

#### Example 1: Simple Pagination with Items

```java
public class ItemListMenu extends Menu {
    
    public ItemListMenu(List<ItemStack> items) {
        super(Component.text("Item List"), 6);
        
        // Create pagination that fills the entire menu
        MenuPagination pagination = MenuPagination.full(this);
        
        // Add all items as forbidden slots (display only)
        for (ItemStack item : items) {
            pagination.addItem(new ForbiddenSlot(item));
        }
        
        // Register the pagination
        registerEditor(pagination);
    }
}
```

#### Example 2: Middle Pagination (for larger menus)

```java
public class PlayerListMenu extends Menu {
    
    public PlayerListMenu(Collection<Player> players) {
        super(Component.text("Online Players"), 6);
        
        // Use middle pagination to leave space for decorations
        MenuPagination pagination = MenuPagination.middle(this);
        
        for (Player p : players) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            meta.displayName(Component.text(p.getName()));
            playerHead.setItemMeta(meta);
            
            pagination.addItem(new MenuItem(playerHead) {
                @Override
                public void click(Click click) {
                    click.player().sendMessage(
                        Component.text("You clicked on " + p.getName())
                    );
                }
            });
        }
        
        registerEditor(pagination);
    }
}
```

#### Example 3: Custom Pagination Slots

```java
public class CustomPaginationMenu extends Menu {
    
    public CustomPaginationMenu() {
        super(Component.text("Custom Layout"), 5);
        
        // Define custom slots for pagination (center area only)
        IntList customSlots = IntList.of(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        );
        
        // Custom page changer at specific positions
        MenuPagination pagination = MenuPagination.create(
            39, // Previous page button slot
            41, // Next page button slot
            MenuPagination.PAGE_CHANGER,
            customSlots
        );
        
        // Add items
        for (int i = 0; i < 50; i++) {
            ItemStack item = new ItemStack(Material.STONE, i + 1);
            pagination.addItem(new ForbiddenSlot(item));
        }
        
        registerEditor(pagination);
        
        // Add border decoration
        decorate(MenuDecoration.flat(
            new ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        ));
    }
}
```

### Complete Example: Menu with All Features

The following example demonstrates a menu utilizing multiple features of the API.

```java
private static class ExampleMenu extends Menu {

    public ExampleMenu(Inventory inventory) {
        super(Component.text("Example menu"), 5);

        // Adding decoration to the menu
        decorate(MenuDecoration.flat(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));

        // Setting up pagination
        MenuPagination pagination = MenuPagination.full(this);
        for (int i = 0; i < 100; i++) {
            ItemStack itemStack = new ItemStack(Material.STONE, i + 1);
            itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, Math.min(99, i + 1));
            pagination.addItem(new ForbiddenSlot(itemStack));
        }
        registerEditor(pagination);

        // Slots linked to another inventory
        setSlot(4, new InventorySlot(inventory, 0));
        setSlot(5, new InventorySlot(inventory, 1));

        // Interactive item
        setSlot(9 * 4 - 1, new MenuItem(new ItemStack(Material.BARRIER)) {
            @Override
            public void click(Click click) {
                click.player().closeInventory();
            }
        });
    }
}
```

### Decorators

Decorators allow you to add aesthetic elements to your menus. In the example above, `MenuDecoration.flat` is used to fill menu slots with gray stained glass panes.

```java
decorate(MenuDecoration.flat(new ItemStack(Material.GRAY_STAINED_GLASS_PANE)));
```

### Pagination

Pagination simplifies the management of a large number of items in a menu. It automatically divides items into multiple pages.

```java
MenuPagination pagination = MenuPagination.full(this);
for (int i = 0; i < 100; i++) {
    ItemStack itemStack = new ItemStack(Material.STONE, i + 1);
    itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, Math.min(99, i + 1));
    pagination.addItem(new ForbiddenSlot(itemStack));
}
registerEditor(pagination);
```

### Slots Linked to Another Inventory

Slots can be linked to specific positions in another inventory, allowing direct interaction between multiple menus or inventories.

```java
setSlot(4, new InventorySlot(inventory, 0));
setSlot(5, new InventorySlot(inventory, 1));
```

### Interactive Items

Interactive items define actions to be executed upon interaction, such as a button to close an inventory:

```java
setSlot(9 * 4 - 1, new MenuItem(new ItemStack(Material.BARRIER)) {
    @Override
    public void click(Click click) {
        click.player().closeInventory();
    }
});
```
