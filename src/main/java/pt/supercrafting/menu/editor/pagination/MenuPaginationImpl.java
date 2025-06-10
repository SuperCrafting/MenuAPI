package pt.supercrafting.menu.editor.pagination;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pt.supercrafting.menu.Menu;
import pt.supercrafting.menu.item.MenuItem;
import pt.supercrafting.menu.slot.ForbiddenSlot;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.*;

final class MenuPaginationImpl implements MenuPagination {

    private int page;
    private int maxPage;

    private final PageChangerSupplier pageChanger;
    private final int previousPage, nextPage;

    private final IntList slots;
    private List<List<MenuSlot>> itemsPerPage;

    private final List<MenuSlot> items = new ArrayList<>();
    private List<MenuSlot> itemsView;

    public MenuPaginationImpl(int previousPage, int nextPage, PageChangerSupplier pageChanger, @NotNull IntList slots) {
        this.previousPage = previousPage;
        this.nextPage = nextPage;

        this.page = 0;
        this.slots = slots;
        this.pageChanger = Objects.requireNonNull(pageChanger);
        Objects.requireNonNull(slots);

    }

    static @NotNull IntList computeAvailableSlots(int maxLines, int itemsPerLine, int startSlot) {
        IntList list = new IntArrayList(maxLines * itemsPerLine);
        for (int i = 0; i < maxLines; i++) {
            int slot = startSlot + (i * 9);
            for (int j = 0; j < itemsPerLine ; j++)
                list.add(slot + j);
        }
        return list;
    }

    private void computeItemsPerPage(@NotNull List<MenuSlot> items) {

        int itemsPerPageCount = this.slots.size();
        int totalItems = items.size();
        this.maxPage = totalItems % itemsPerPageCount == 0 ?
                       totalItems / itemsPerPageCount :
                       (totalItems / itemsPerPageCount) + 1;

        this.itemsPerPage = new ArrayList<>(this.maxPage);
        for (int i = 0; i < this.maxPage; i++) {
            int startIndex = i * itemsPerPageCount;
            int endIndex = Math.min(startIndex + itemsPerPageCount, totalItems);
            this.itemsPerPage.add(new ArrayList<>(items.subList(startIndex, endIndex)));
        }

    }

    @Override
    public int getMaxPage() {
        return maxPage;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int page) {

        if(this.maxPage == 0) {
            this.page = 0; // No items, reset to page 0
            return;
        }

        if(page < 0 || page >= maxPage)
            throw new IllegalArgumentException("Page must be between 0 and " + (maxPage - 1));
        this.page = page;
    }

    @Override
    public void addItem(@NotNull MenuSlot item) {
        Objects.requireNonNull(item);
        addItems(Collections.singleton(item));
    }

    @Override
    public void addItems(@NotNull MenuSlot... items) {
        Objects.requireNonNull(items);
        addItems(Arrays.asList(items));
    }

    @Override
    public void addItems(@NotNull Collection<MenuSlot> items) {
        Objects.requireNonNull(items);
        if(items.isEmpty())
            return;

        this.items.addAll(items);
        this.itemsView = null; // Invalidate the view
        computeItemsPerPage(this.items);
        if(this.page >= this.maxPage)
            this.page = this.maxPage - 1; // Adjust page if it exceeds max
    }

    @Override
    public void removeItem(@NotNull MenuSlot item) {
        Objects.requireNonNull(item);
        removeItems(Collections.singleton(item));
    }

    @Override
    public void removeItems(@NotNull MenuSlot... items) {
        Objects.requireNonNull(items);
        removeItems(Arrays.asList(items));
    }

    @Override
    public void removeItems(@NotNull Collection<MenuSlot> items) {
        boolean success = this.items.removeAll(items);
        if(success) {
            this.itemsView = null; // Invalidate the view
            computeItemsPerPage(this.items);
            if(this.page >= this.maxPage)
                this.page = this.maxPage - 1; // Adjust page if it exceeds max
        }
    }

    @Override
    public void clear() {
        this.items.clear();
        this.itemsPerPage = null;
        this.page = 0;
        this.maxPage = 0;
    }

    @Override
    public @NotNull @Unmodifiable List<MenuSlot> getItems() {
        if(this.itemsView == null)
            this.itemsView = Collections.unmodifiableList(this.items);
        return this.itemsView;
    }

    @Override
    public @NotNull MenuPagination with(@NotNull PageChangerSupplier pageChanger) {
        Objects.requireNonNull(pageChanger, "PageChangerSupplier cannot be null");
        return new MenuPaginationImpl(this.previousPage, this.nextPage, pageChanger, this.slots);
    }

    @Override
    public void edit(@NotNull Menu menu, @NotNull Inventory inventory) {

        if(this.items.isEmpty())
            return;

        if(this.itemsPerPage == null || this.itemsPerPage.isEmpty())
            this.computeItemsPerPage(this.items);

        List<MenuSlot> items = this.itemsPerPage.get(this.page);
        for (int i = 0; i < this.slots.size(); i++) {
            if(i >= items.size()) {
                menu.setSlot(this.slots.getInt(i), ForbiddenSlot.INSTANCE);
                continue;
            }

            int slot = this.slots.getInt(i);
            MenuSlot item = items.get(i);

            menu.setSlot(slot, item);
        }

        placePageChanger(menu, this, this.pageChanger, this.previousPage, this.page, this.maxPage, PageChangerSupplier.Type.PREVIOUS);
        placePageChanger(menu, this, this.pageChanger, this.nextPage, this.page, this.maxPage, PageChangerSupplier.Type.NEXT);

    }

    private static void placePageChanger(Menu menu, MenuPagination pagination, PageChangerSupplier pageChanger, int slot, int page, int maxPage, PageChangerSupplier.Type type) {
        MenuSlot oldSlot = menu.getSlot(slot);
        if(!(oldSlot == ForbiddenSlot.INSTANCE || oldSlot instanceof PageChangerSupplier.PageChangerItem))
            return;

        MenuSlot newSlot;
        int futurePage = type == PageChangerSupplier.Type.PREVIOUS ? page - 1 : page + 1;
        if(futurePage < 0 || futurePage >= maxPage) {
            newSlot = null;
        } else {
            newSlot = pageChanger.get(menu, pagination, type, futurePage, maxPage);
        }

        menu.setSlot(slot, Objects.requireNonNullElse(newSlot, ForbiddenSlot.INSTANCE));
    }

    public static class PageChangerSupplierImpl implements PageChangerSupplier {

        @Override
        public <T extends MenuSlot & PageChangerSupplier.PageChangerItem> @Nullable T get(@NotNull Menu menu, @NotNull MenuPagination pagination, @NotNull Type type, int page, int maxPage) {

            int futurePage = type == Type.PREVIOUS ? page - 1 : page + 1;
            futurePage = pagination.clamp(futurePage);

            int visiblePage = Math.max(1, futurePage);
            ItemStack itemStack = new ItemStack(Material.ARROW, visiblePage);

            return (T) new PageChangerItem(itemStack, menu, pagination, futurePage);
        }

        private static class PageChangerItem extends MenuItem implements PageChangerSupplier.PageChangerItem {

            private final Menu menu;
            private final MenuPagination pagination;
            private final int page;

            public PageChangerItem(@NotNull ItemStack itemStack, @NotNull Menu menu, @NotNull MenuPagination pagination, int page) {
                super(itemStack);
                this.menu = Objects.requireNonNull(menu);
                this.pagination = pagination;
                this.page = page;
            }

            @Override
            public void click(Click click) {
                pagination.setPage(page);
                menu.refresh();
            }

        }

    }

}
