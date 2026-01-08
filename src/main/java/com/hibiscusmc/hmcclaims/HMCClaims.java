package com.hibiscusmc.hmcclaims;

import com.hibiscusmc.hmcclaims.manager.ClaimManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.module.CommandModule;
import com.hibiscusmc.hmcclaims.module.ConfigModule;
import com.hibiscusmc.hmcclaims.module.ListenerModule;
import com.hibiscusmc.hmcclaims.module.ServiceModule;
import com.hibiscusmc.hmcclaims.manager.SelectionManager;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.util.Text;
import lombok.extern.java.Log;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.inject.Binder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;
import team.unnamed.inject.Module;

import java.util.Set;

@Log(topic = "HMCClaims")
public final class HMCClaims extends JavaPlugin implements Module {

    @Inject
    private Set<Service> services;

    @Override
    public void onEnable() {
        Injector.create(this)
                .injectMembers(this);

        for (Service service : services) {
            service.start();
        }

        log.info("HMCClaims enabled!");
    }

    @Override
    public void onDisable() {
        for (Service service : services) {
            service.stop();
        }

        log.info("HMCClaims disabled!");
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(HMCClaims.class).toInstance(this);
        binder.bind(JavaPlugin.class).to(HMCClaims.class);
        binder.bind(Plugin.class).to(HMCClaims.class);

        binder.install(new ServiceModule());
        binder.install(new CommandModule());
        binder.install(new ListenerModule());
        binder.install(new ConfigModule(this));

        binder.bind(StorageHolder.class).to(StorageHolder.class);
        binder.bind(BlockMarker.class).to(BlockMarker.class);

        binder.bind(SelectionManager.class).to(SelectionManager.class);
        binder.bind(ClaimManager.class).to(ClaimManager.class);

        binder.bind(Text.class).to(Text.class);
    }
}