package com.hibiscusmc.hmcclaims.economy;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * The money operations the plugin needs, independent of the economy plugin behind them.
 */
public interface EconomyProvider {

    /**
     * @param player The account owner.
     * @return The player's current balance.
     */
    double balance(@NotNull OfflinePlayer player);

    /**
     * Takes money from a player's account.
     *
     * @param player The account owner.
     * @param amount The amount to take.
     * @return {@code true} if the money was withdrawn.
     */
    boolean withdraw(@NotNull OfflinePlayer player, double amount);

    /**
     * @param amount The amount to display.
     * @return The amount formatted the way the economy plugin presents money.
     */
    @NotNull
    String format(double amount);
}
