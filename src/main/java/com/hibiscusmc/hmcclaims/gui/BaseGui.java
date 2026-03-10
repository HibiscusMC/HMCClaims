package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The core interface for all GUIs within the plugin.
 */
public interface BaseGui {

    /**
     * Instructs the GUI to (re)load its visual components, such as item icons,
     * titles, and layout from the configuration files.
     */
    void loadConfig();

    /**
     * Opens the standard version of this interface for the specified player.
     *
     * @param player The player who will view the GUI.
     */
    default void open(@NotNull Player player) {
    }

    /**
     * Opens a context-specific version of this interface for the player.
     *
     * @param player The player who will view the GUI.
     * @param args   Optional contextual arguments (e.g., Claim objects, role data).
     */
    default void open(@NotNull Player player, Object... args) {
    }

    /**
     * Builds the layout of a paginated gui adding air to the unused slots and adding the page icons
     *
     * @param gui              The {@link PaginatedGui} instance of the gui to build
     * @param slots            The list of slots available for use
     * @param previousPageIcon The previous page icon
     * @param nextPageIcon     The next page icon
     */
    default void buildPageLayout(@NotNull PaginatedGui gui, @NotNull List<Integer> slots, @Nullable GuiTemplate.SimpleIcon previousPageIcon, @Nullable GuiTemplate.SimpleIcon nextPageIcon) {
        GuiItem air = new GuiItem(ItemStack.of(Material.AIR));

        for (int i = 0; i < gui.getRows() * 9; i++) {
            if (!slots.contains(i)) {
                gui.setItem(i, air);
            }
        }

        if (previousPageIcon != null) {
            gui.setItem(previousPageIcon.slot(), new GuiItem(previousPageIcon.item(), action -> gui.previous()));
        }

        if (nextPageIcon != null) {
            gui.setItem(nextPageIcon.slot(), new GuiItem(nextPageIcon.item(), action -> gui.next()));
        }
    }

}