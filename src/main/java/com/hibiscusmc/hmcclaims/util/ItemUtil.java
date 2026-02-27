package com.hibiscusmc.hmcclaims.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Utility for streamlined {@link ItemStack} creation using the Adventure API.
 * <p>
 * This class serves as a bridge between raw configuration strings (MiniMessage)
 * and the modern Paper {@link net.kyori.adventure.text.Component} system.
 */
public class ItemUtil {

    /**
     * Builds a basic {@link ItemStack} with a custom display name.
     *
     * @param material The {@link Material} type for the item.
     * @param name     The raw string name to be parsed (supports MiniMessage).
     * @return A newly constructed {@link ItemStack}.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static ItemStack build(@NotNull Material material, @NotNull String name) {
        return build(material, name, null);
    }

    /**
     * Builds an {@link ItemStack} with a custom display name and multi-line lore.
     *
     * @param material The {@link Material} type for the item.
     * @param name     The raw string name to be parsed.
     * @param lore     A list of raw strings to be parsed into the item's lore, or {@code null}.
     * @return A newly constructed {@link ItemStack} with the specified metadata.
     */
    @NotNull
    @Contract(value = "_, _, _ -> new", pure = true)
    public static ItemStack build(@NotNull Material material, @NotNull String name, @Nullable List<String> lore) {
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.customName(TextUtil.parseItem(name));

            if (lore != null) {
                meta.lore(lore.stream().map(TextUtil::parseItem).toList());
            }

            item.setItemMeta(meta);
        }

        return item;
    }
}