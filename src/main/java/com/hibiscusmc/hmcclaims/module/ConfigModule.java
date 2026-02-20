package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.Guis;
import com.hibiscusmc.hmcclaims.config.internal.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import org.bukkit.plugin.Plugin;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.key.TypeReference;

import java.nio.file.Path;

public class ConfigModule extends AbstractModule {

    private final Plugin plugin;

    public ConfigModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure() {
        Path pluginPath = plugin.getDataPath();

        try {
            ConfigFactory.load(pluginPath.resolve("config.yml"), Settings.class);
            ConfigFactory.load(pluginPath.resolve("messages.yml"), Messages.class);
            ConfigFactory.load(pluginPath.resolve("default-roles.yml"), DefaultRoles.class);

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-list.yml")), Guis.ClaimList.class);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-info.yml")), Guis.ClaimInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bind(new TypeReference<ConfigHolder<Settings>>() {
        }).toInstance(ConfigFactory.getHolder(Settings.class));

        bind(new TypeReference<ConfigHolder<Messages>>() {
        }).toInstance(ConfigFactory.getHolder(Messages.class));

        bind(new TypeReference<ConfigHolder<DefaultRoles>>() {
        }).toInstance(ConfigFactory.getHolder(DefaultRoles.class));

        bind(new TypeReference<ConfigHolder<Guis.ClaimList>>() {
        }).toInstance(ConfigFactory.getHolder(Guis.ClaimList.class));

        bind(new TypeReference<ConfigHolder<Guis.ClaimInfo>>() {
        }).toInstance(ConfigFactory.getHolder(Guis.ClaimInfo.class));
    }
}