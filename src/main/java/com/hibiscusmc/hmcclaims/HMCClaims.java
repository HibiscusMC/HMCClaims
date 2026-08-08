package com.hibiscusmc.hmcclaims;

import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.hook.Hook;
import com.hibiscusmc.hmcclaims.input.InputManager;
import com.hibiscusmc.hmcclaims.marker.BlockMarker;
import com.hibiscusmc.hmcclaims.module.CommandModule;
import com.hibiscusmc.hmcclaims.module.ConfigModule;
import com.hibiscusmc.hmcclaims.module.HookModule;
import com.hibiscusmc.hmcclaims.module.ListenerModule;
import com.hibiscusmc.hmcclaims.module.ServiceModule;
import com.hibiscusmc.hmcclaims.selection.SelectionManager;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.storage.StorageHolder;
import com.hibiscusmc.hmcclaims.user.UserManager;
import com.hibiscusmc.hmcclaims.util.PlaceholderUtil;
import com.hibiscusmc.hmcclaims.util.SchedulerUtil;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import team.unnamed.inject.Binder;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;
import team.unnamed.inject.Module;

import java.util.Set;

public final class HMCClaims extends JavaPlugin implements Module {

    @Inject
    private Set<Service> services;

    @Inject
    private Set<Hook> hooks;

    @Override
    public void onEnable() {
        Injector injector = Injector.create(this);
        injector.injectMembers(this);

        HMCClaimsAPI.instance(injector.getInstance(HMCClaimsAPI.class));

        for (Service service : services) {
            service.start();
        }

        Bukkit.getScheduler().runTask(this, () -> {
            PluginManager plugMan = getServer().getPluginManager();
            for (Hook hook : hooks) {
                if (hook.loadStrategy() != Hook.LoadStrategy.PLUGIN_ENABLE) {
                    hook.registerLoader();
                    continue;
                }

                if (plugMan.getPlugin(hook.dependsOn()) == null) {
                    continue;
                }

                if (!plugMan.isPluginEnabled(hook.dependsOn())) {
                    continue;
                }

                hook.register();
            }
        });
    }

    @Override
    public void onDisable() {
        for (Service service : services) {
            service.stop();
        }

        for (Hook hook : hooks) {
            hook.unregister();
        }
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(HMCClaims.class).toInstance(this);
        binder.bind(JavaPlugin.class).to(HMCClaims.class);
        binder.bind(Plugin.class).to(HMCClaims.class);

        binder.bind(PlaceholderUtil.class).to(PlaceholderUtil.class);
        binder.bind(SchedulerUtil.class).to(SchedulerUtil.class);
        binder.bind(TextUtil.class).to(TextUtil.class);

        binder.install(
                new ServiceModule(),
                new CommandModule(),
                new ListenerModule(),
                new ConfigModule(this),
                new HookModule()
        );

        binder.bind(PermissionRegistry.class).to(PermissionRegistry.class);
        binder.bind(SettingRegistry.class).to(SettingRegistry.class);
        binder.bind(GuiRegistry.class).to(GuiRegistry.class);

        binder.bind(StorageHolder.class).to(StorageHolder.class);
        binder.bind(BlockMarker.class).to(BlockMarker.class);

        binder.bind(SelectionManager.class).to(SelectionManager.class);
        binder.bind(ClaimManager.class).to(ClaimManager.class);
        binder.bind(UserManager.class).to(UserManager.class);

        binder.bind(InputManager.class);
    }
}