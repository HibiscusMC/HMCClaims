package com.hibiscusmc.hmcclaims.gui;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharList;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.util.TriConsumer;

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
     * @param player   The player who will view the GUI.
     * @param metadata Contextual metadata required to render the GUI.
     */
    default void open(@NotNull Player player, @NotNull GuiMetadata metadata) {
    }

    /**
     * Truncates the claim name to the title's maximum length, appending "..." if exceeded.
     *
     * @param claimName the original claim name to parse
     * @param maxLength the maximum allowed length for the claim name
     * @return the potentially truncated claim name, or the original if within limits
     */
    default String parseName(@NotNull String claimName, int maxLength) {
        if (maxLength < 0 || claimName.length() <= maxLength) {
            return claimName;
        }

        return claimName.substring(0, maxLength).trim() + "...";
    }

    /**
     * Builds a consumer that applies tab navigation items to a GUI builder based on a grid structure.
     *
     * @param structure  the character grid layout of the GUI
     * @param currentTab the class of the currently active GUI tab
     * @param tabs       the navigation icons to map and render
     * @return a {@link TriConsumer} configured to handle the layout and click actions for the tabs
     */
    default TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> buildTabs(@NotNull CharList structure, @NotNull Class<? extends BaseGui> currentTab, @NotNull TabIcon... tabs) {
        Char2ObjectMap<TabIcon> map = new Char2ObjectOpenHashMap<>();

        int i = 0;
        for (TabIcon tab : tabs) {
            map.put((char) (++i), tab);
            structure.set(tab.slot(), (char) i);
        }

        return (gui, guiRegistry, player) -> {
            for (Char2ObjectMap.Entry<TabIcon> entry : map.char2ObjectEntrySet()) {
                TabIcon tab = entry.getValue();

                gui.addIngredient(entry.getCharKey(), Item.builder()
                        .setItemProvider(tab.item())
                        .addClickHandler(click -> {
                            if (tab.iconTab().equals(currentTab)) {
                                return;
                            }

                            guiRegistry.get(tab.iconTab())
                                    .open(player, tab.metadata());
                        })
                        .build());
            }
        };
    }

    /**
     * Represents a navigation tab icon within a GUI.
     *
     * @param iconTab the target GUI class this tab opens
     * @param item    the visual item stack representing the tab
     * @param slot    the inventory slot index for the tab icon
     * @param metadata    optional context arguments passed when opening the target GUI
     */
    record TabIcon(Class<? extends BaseGui> iconTab, ItemStack item, int slot, GuiMetadata metadata) {
    }
}