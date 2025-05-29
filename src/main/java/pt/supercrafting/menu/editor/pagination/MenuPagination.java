package pt.supercrafting.menu.editor.pagination;

import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pt.supercrafting.menu.Menu;
import pt.supercrafting.menu.editor.MenuEditor;
import pt.supercrafting.menu.slot.MenuSlot;

import java.util.Collection;
import java.util.List;

public interface MenuPagination extends MenuEditor {

    PageChangerSupplier PAGE_CHANGER = new MenuPaginationImpl.PageChangerSupplierImpl();

    static @NotNull MenuPagination full(Menu menu) {
        int size = menu.size();
        IntList slots = MenuPaginationImpl.computeAvailableSlots((size / 9) - 2, 7, 10);
        return create(
                size - 6, size - 4,
                PAGE_CHANGER,
                slots
        );
    }

    static @NotNull MenuPagination middle(Menu menu) {

        int size = menu.size();
        if (size / 9 <= 4)
            return full(menu);

        IntList slots = MenuPaginationImpl.computeAvailableSlots((size / 9) - 4, 5, 20);
        return create(
                size - 6, size - 4,
                PAGE_CHANGER,
                slots
        );
    }

    static @NotNull MenuPagination create(
            int previousPage,
            int nextPage,
            @NotNull PageChangerSupplier pageChanger,
            @NotNull IntList slots) {
        return new MenuPaginationImpl(previousPage, nextPage, pageChanger, slots);
    }

    int getPage();
    void setPage(int page);

    int getMaxPage();

    void addItem(@NotNull MenuSlot item);
    void addItems(@NotNull MenuSlot... items);
    void addItems(@NotNull Collection<MenuSlot> items);

    void removeItem(@NotNull MenuSlot item);
    void removeItems(@NotNull MenuSlot... items);
    void removeItems(@NotNull Collection<MenuSlot> items);

    void clear();

    default int clamp(int page) {
        return Math.max(0, Math.min(page, getMaxPage() - 1));
    }

    @NotNull
    @Unmodifiable
    List<MenuSlot> getItems();

    @Contract("_ -> new")
    @NotNull
    MenuPagination with(@NotNull PageChangerSupplier pageChanger);

    interface PageChangerSupplier {

        interface PageChangerItem {}

        @Nullable
        <T extends MenuSlot & PageChangerItem> T get(@NotNull Menu menu, @NotNull MenuPagination pagination, @NotNull Type type, int page, int maxPage);

        enum Type {
            PREVIOUS,
            NEXT
        }

    }


}
