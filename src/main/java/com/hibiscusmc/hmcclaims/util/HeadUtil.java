package com.hibiscusmc.hmcclaims.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Utility for creating and manipulating player skull {@link ItemStack}s.
 * <p>
 * This class provides methods to retrieve heads based on player {@link UUID}s
 * or custom Base64 texture strings using the Paper Profile API.
 */
public class HeadUtil {

    /**
     * Creates an {@link ItemStack} of a specific player's head.
     * <p>
     * This method uses the Bukkit API to fetch profile data. If the player has not
     * played on the server before, the server may perform a blocking web request
     * to fetch the skin.
     *
     * @param uuid The {@link UUID} of the player whose head is being created.
     * @return A new {@link Material#PLAYER_HEAD} ItemStack.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static ItemStack getPlayerHead(@NotNull UUID uuid) {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
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
    public static ItemStack getCustomHead(@NotNull String base64) {
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