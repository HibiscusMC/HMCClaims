package com.hibiscusmc.hmcclaims.hook.vault;

import com.hibiscusmc.hmcclaims.economy.EconomyProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Routes the plugin's money operations through the economy registered with Vault.
 */
final class VaultEconomyProvider implements EconomyProvider {

    private final Economy economy;

    VaultEconomyProvider(@NotNull Economy economy) {
        this.economy = economy;
    }

    @Override
    public double balance(@NotNull OfflinePlayer player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer player, double amount) {
        EconomyResponse response = economy.withdrawPlayer(player, amount);

        return response.transactionSuccess();
    }

    @Override
    public @NotNull String format(double amount) {
        return economy.format(amount);
    }
}
