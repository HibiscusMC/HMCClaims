package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.config.gui.GuiTemplate;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimManageGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimMemberListGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimRolesGui;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimSettingsGui;
import com.hibiscusmc.hmcclaims.gui.impl.SubClaimManageGui;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.util.TriConsumer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Parses a character list into an array of row strings.
     *
     * @param structure the character list containing row data
     * @param rows      the number of rows to parse
     * @return an array containing each row as a string
     */
    @NotNull
    @Contract(pure = true)
    default String[] parseStructure(@NotNull CharList structure, int rows) {
        String[] structureArray = new String[rows];
        for (int r = 0; r < rows; r++) {
            CharList rowList = structure.subList(r * 9, (r + 1) * 9);
            structureArray[r] = new String(rowList.toCharArray());
        }

        return structureArray;
    }

    /**
     * Truncates the claim name to the title's maximum length, appending "..." if exceeded.
     *
     * @param claimName the original claim name to parse
     * @param maxLength the maximum allowed length for the claim name
     * @return the potentially truncated claim name, or the original if within limits
     */
    @NotNull
    @Contract(pure = true)
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
     * @param claim      the claim this menu belongs to
     * @param metadata   the metadata related to this set of GUIs
     * @param tabs       the map of tabs set in the config
     * @return a {@link TriConsumer} configured to handle the layout and click actions for the tabs
     */
    @NotNull
    default TriConsumer<Gui.Builder<?, ?>, GuiRegistry, Player> buildTabs(
            @NotNull CharList structure, @NotNull Class<? extends BaseGui> currentTab, @NotNull Claim claim, @NotNull GuiMetadata metadata,
            @NotNull Map<String, GuiTemplate.SimpleIcon> tabs
    ) {
        Set<TabIcon> icons = new HashSet<>();

        if (tabs.containsKey("members-tab")) {
            GuiTemplate.SimpleIcon tab = tabs.get("members-tab");
            icons.add(new TabIcon(ClaimMemberListGui.class, tab.item(), tab.slot(), metadata));
        }

        if (tabs.containsKey("roles-tab")) {
            GuiTemplate.SimpleIcon tab = tabs.get("roles-tab");
            icons.add(new TabIcon(ClaimRolesGui.class, tab.item(), tab.slot(), metadata));
        }

        if (tabs.containsKey("settings-tab")) {
            GuiTemplate.SimpleIcon tab = tabs.get("settings-tab");
            icons.add(new TabIcon(ClaimSettingsGui.class, tab.item(), tab.slot(), metadata));
        }

        if (tabs.containsKey("manage-tab")) {
            GuiTemplate.SimpleIcon tab = tabs.get("manage-tab");
            icons.add(new TabIcon(claim.main() == null ? ClaimManageGui.class : SubClaimManageGui.class, tab.item(), tab.slot(), metadata));
        }

        Char2ObjectMap<TabIcon> map = new Char2ObjectOpenHashMap<>();

        int i = 0;
        for (TabIcon tab : icons) {
            map.put((char) (++i), tab);
            structure.set(tab.slot(), (char) i);
        }

        return (gui, guiRegistry, player) -> {
            for (Char2ObjectMap.Entry<TabIcon> entry : map.char2ObjectEntrySet()) {
                TabIcon tab = entry.getValue();

                gui.addIngredient(entry.getCharKey(), Item.builder()
                        .setItemProvider((p) -> new ItemWrapper(TextUtil.parseItemPlaceholders(tab.item(), player)))
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
     * Converts a map of template icons into a map of interactive items.
     *
     * @param icons the map of icons to parse
     * @return a map of built items with mapped click actions and the slot they belong to
     */
    @NotNull
    @Contract(pure = true)
    default Int2ObjectMap<Item> parseExtraItems(@NotNull Map<String, GuiTemplate.Icon> icons) {
        Int2ObjectMap<Item> items = new Int2ObjectArrayMap<>();

        for (GuiTemplate.Icon icon : icons.values()) {
            items.put(icon.slot(), Item.builder()
                    .setItemProvider(player -> new ItemWrapper(TextUtil.parseItemPlaceholders(icon.item(), player)))
                    .addClickHandler(click -> (switch (click.clickType()) {
                        case LEFT -> icon.leftClickActions();
                        case RIGHT -> icon.rightClickActions();
                        default -> List.<Action>of();
                    }).forEach(action -> action.execute(click.player())))
                    .build());
        }

        return items;
    }

    /**
     * Checks if the icon exists in the config and if it's on a valid slot before setting it in the structure.
     *
     * @param icon the icon to validate
     * @return {@code true} if it's a valid icon, {@code false} otherwise.
     */
    default boolean isValidIcon(@Nullable GuiTemplate.SimpleIcon icon) {
        return icon != null && icon.item() != null && icon.slot() > -1;
    }

    /**
     * Represents a navigation tab icon within a GUI.
     *
     * @param iconTab  the target GUI class this tab opens
     * @param item     the visual item stack representing the tab
     * @param slot     the inventory slot index for the tab icon
     * @param metadata optional context arguments passed when opening the target GUI
     */
    record TabIcon(Class<? extends BaseGui> iconTab, ItemStack item, int slot, GuiMetadata metadata) {
    }
}