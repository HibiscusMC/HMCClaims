package com.hibiscusmc.hmcclaims.gui;

import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the icons of the registry-driven lists (permissions and settings).
 */
public final class EntryIcon {

    private EntryIcon() {
    }

    /**
     * Builds an icon for a permission or a setting.
     *
     * @param type        {@code permission} or {@code setting}, which decides the placeholders that are replaced
     * @param entryName   the display name of the entry
     * @param description the description lines of the entry, which replace the {@code <type_description>} lore line
     * @param values      extra placeholders, such as {@code permission_value}
     */
    @NotNull
    public static ItemStack build(
            @NotNull ConfigItem item, @NotNull String name, @NotNull List<String> lore,
            @NotNull String type, @NotNull String entryName, @NotNull List<String> description,
            @NotNull Map<String, Object> values
    ) {
        Map<String, Object> placeholders = new HashMap<>(values);
        placeholders.put(type + "_name", entryName);

        List<String> expanded = TextUtil.expandLines(lore, "<" + type + "_description>", description);
        ItemStack stack = item.stack();

        stack.editMeta(meta -> {
            meta.itemName(TextUtil.parse(name, placeholders));
            meta.lore(TextUtil.parseItemLore(expanded, placeholders));
        });

        return stack;
    }
}
