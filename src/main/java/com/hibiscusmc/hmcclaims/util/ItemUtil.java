package com.hibiscusmc.hmcclaims.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
     * Creates an {@link ItemStack} of a specific player's head.
     * <p>
     * The profile only carries the id and name; the server fills in the skin when the item
     * is sent to a client, so this works for players the server's user cache has dropped.
     *
     * @param playerId   The id of the player whose head is being created.
     * @param playerName The name of the player whose head is being created.
     * @return A new {@link Material#PLAYER_HEAD} ItemStack.
     * @noinspection UnstableApiUsage
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static ItemStack buildHead(@NotNull UUID playerId, @NotNull String playerName) {
        ItemStack head = ItemStack.of(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        meta.setPlayerProfile(Bukkit.createProfile(playerId, playerName.isEmpty() ? null : playerName));
        head.setItemMeta(meta);

        TooltipDisplay display = TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.PROFILE)
                .build();
        head.setData(DataComponentTypes.TOOLTIP_DISPLAY, display);

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

    /**
     * Applies a custom display name to an existing {@link ItemStack}.
     *
     * @param item The item to modify.
     * @param name The name to be parsed as a {@link Component}.
     */
    public static void applyDisplay(@NotNull ItemStack item, @NotNull Component name) {
        applyDisplay(item, name, null);
    }

    /**
     * Applies a custom display name and lore to an existing {@link ItemStack}.
     *
     * @param item The item to modify.
     * @param name The name to be parsed as a {@link Component}.
     * @param lore A list of raw strings to be parsed into lore, or {@code null}.
     */
    public static void applyDisplay(@NotNull ItemStack item, @NotNull Component name, @Nullable List<Component> lore) {
        item.editMeta(meta -> {
            meta.customName(name);
            meta.itemName(name);

            if (lore != null) {
                meta.lore(lore);
            }
        });
    }
}