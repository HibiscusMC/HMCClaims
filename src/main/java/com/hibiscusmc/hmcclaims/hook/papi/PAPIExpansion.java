package com.hibiscusmc.hmcclaims.hook.papi;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

import java.util.Arrays;
import java.util.UUID;

public class PAPIExpansion extends PlaceholderExpansion {

    @Inject
    private Plugin plugin;

    @Inject
    private UserManager userManager;

    @Inject
    private ClaimManager claimManager;

    @NotNull
    @Override
    public String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @NotNull
    @Override
    public String getAuthor() {
        return plugin.getPluginMeta().getAuthors().getFirst();
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] args = params.split("_");
        if (args.length == 0) {
            return "<missing args>";
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return switch (args[0].toLowerCase()) {
            case "claim" -> handleClaimPlaceholders(player, subArgs);
            case "user" -> handleUserPlaceholders(player, subArgs);
            default -> "<invalid arg " + args[0] + ">";
        };
    }

    /**
     * Placeholder list:
     * %hmcclaims_claim_name% -> The name of the claim this player is standing on
     */
    @NotNull
    @Contract(pure = true)
    private String handleClaimPlaceholders(OfflinePlayer offlinePlayer, String[] args) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "<player not found>";
        }

        if (args.length == 0) {
            return "<missing args>";
        }

        Player player = (Player) offlinePlayer;

        Claim claim = claimManager.getClaimAt(player.getLocation())
                .orElse(null);

        if (claim == null) {
            return "<claim not found>";
        }

        String arg = String.join("_", args).toLowerCase();
        return switch (arg) {
            case "name" -> claim.name();
            default -> "<invalid arg " + arg + ">";
        };
    }

    /**
     * Placeholder list:
     * %hmcclaims_user_claims% -> Amount of claims this user has
     * %hmcclaims_user_claim_blocks% -> Amount of claim blocks this user has
     */
    @NotNull
    @Contract(pure = true)
    private String handleUserPlaceholders(OfflinePlayer offlinePlayer, String[] args) {
        if (args.length == 0) {
            return "<missing args>";
        }

        if (offlinePlayer == null) {
            return "<player not found>";
        }

        UUID playerId = offlinePlayer.getUniqueId();
        String arg = String.join("_", args).toLowerCase();
        return switch (arg) {
            case "claims" -> claimManager.getPlayerClaims(playerId).size() + "";
            case "claim_blocks" -> userManager.getUser(playerId).map(User::claimBlocks).orElse(0L) + "";
            default -> "<invalid arg " + arg + ">";
        };
    }
}