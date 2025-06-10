# MenuAPI

MenuAPI is a Java library designed for creating and managing interactive and dynamic menus in applications using Minecraft. This API includes advanced features such as pagination, decorators, editors, and interactive items.

## Key Features

- **Pagination**: Efficiently manage large numbers of items with a paging system.
- **Decorators**: Add aesthetic elements to your menus.
- **Editors**: Dynamically modify menu content.
- **Interactive Slots**: Create slots linked to another inventory or interactive items.

## Examples

### Example of a menu with pagination, decoration, and interactive slots

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
