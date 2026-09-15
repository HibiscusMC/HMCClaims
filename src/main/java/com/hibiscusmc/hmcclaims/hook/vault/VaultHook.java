package com.hibiscusmc.hmcclaims.hook.vault;

import com.hibiscusmc.hmcclaims.economy.EconomyService;
import com.hibiscusmc.hmcclaims.hook.Hook;
import com.hibiscusmc.hmcclaims.util.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;

/**
 * Activates claim block purchases once Vault and an economy plugin are confirmed to be
 * installed.
 * <p>
 * Nothing outside this package touches Vault classes, so a server without Vault never has
 * to resolve them.
 */
public class VaultHook implements Hook {

    @Inject
    private EconomyService economy;

    @Override
    public @NotNull String dependsOn() {
        return "Vault";
    }

    @Override
    public @NotNull LoadStrategy loadStrategy() {
        return LoadStrategy.PLUGIN_ENABLE;
    }

    @Override
    public void register() {
        RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);

        if (registration == null) {
            Logger.warning("Vault detected, but no economy plugin registered with it. Claim block purchases are disabled.");
            return;
        }

        economy.enable(new VaultEconomyProvider(registration.getProvider()));

        Logger.log("Vault detected, claim block purchases are active through {}.", registration.getProvider().getName());
    }

    @Override
    public void unregister() {
        economy.disable();
    }
}
