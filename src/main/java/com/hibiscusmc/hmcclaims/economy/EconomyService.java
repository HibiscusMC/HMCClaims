package com.hibiscusmc.hmcclaims.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.inject.Singleton;

/**
 * Plugin-facing access to money.
 * <p>
 * Backed by whatever {@link EconomyProvider} a hook installs (currently the Vault hook).
 * Without one, every operation reports the economy as unavailable, so callers only have
 * to check {@link #available()} before charging a player.
 */
@Singleton
public class EconomyService {

    @Nullable
    private EconomyProvider provider;

    /**
     * Activates money support. Called by the hook that found an economy plugin.
     *
     * @param provider The economy to route operations through.
     */
    public void enable(@NotNull EconomyProvider provider) {
        this.provider = provider;
    }

    /**
     * Deactivates money support.
     */
    public void disable() {
        this.provider = null;
    }

    /**
     * @return {@code true} if an economy plugin is hooked.
     */
    public boolean available() {
        return provider != null;
    }

    /**
     * @param player The account owner.
     * @return The player's balance, or {@code 0} without an economy.
     */
    public double balance(@NotNull OfflinePlayer player) {
        return provider == null ? 0 : provider.balance(player);
    }

    /**
     * Takes money from a player's account.
     *
     * @param player The account owner.
     * @param amount The amount to take.
     * @return {@code true} if the money was withdrawn, always {@code false} without an economy.
     */
    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        return provider != null && provider.withdraw(player, amount);
    }

    /**
     * @param amount The amount to display.
     * @return The amount formatted by the economy plugin, or as a plain number without one.
     */
    @NotNull
    public String format(double amount) {
        return provider == null ? String.valueOf(amount) : provider.format(amount);
    }
}
