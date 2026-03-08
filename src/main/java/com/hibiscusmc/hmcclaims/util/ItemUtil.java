package com.hibiscusmc.hmcclaims.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Utility for streamlined {@link ItemStack} creation using the Adventure API.
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

    /**
     * Creates an {@link ItemStack} of a specific player's head.
     *
     * @param playerName The name of the player whose head is being created.
     * @return A new {@link Material#PLAYER_HEAD} ItemStack.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ItemStack buildHeadWithName(@NotNull String playerName) {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayerIfCached(playerName));
            head.setItemMeta(meta);
        }

        return head;
    }

    /**
     * Creates a custom head using a Base64 texture string.
     *
     * @param base64 The Base64 encoded texture string (from sites like Minecraft-Heads).
     * @return A new {@link Material#PLAYER_HEAD} ItemStack with the custom texture.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ItemStack buildHeadWithTextures(@NotNull String base64) {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", base64));

            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        }

        return head;
    }
}