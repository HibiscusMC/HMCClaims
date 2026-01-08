package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.config.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import org.bukkit.plugin.Plugin;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.key.TypeReference;

import java.nio.file.Path;

public class ConfigModule extends AbstractModule {

    private Plugin plugin;

    public ConfigModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure() {
        Path pluginPath = plugin.getDataPath();

        try {
            ConfigFactory.load(pluginPath.resolve("config.yml"), Settings.class);
            ConfigFactory.load(pluginPath.resolve("messages.yml"), Messages.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bind(new TypeReference<ConfigHolder<Settings>>() {
        }).toInstance(ConfigFactory.getHolder(Settings.class));

        bind(new TypeReference<ConfigHolder<Messages>>() {
        }).toInstance(ConfigFactory.getHolder(Messages.class));
    }
}