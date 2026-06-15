package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.gui.ClaimListConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberListConfig;
import com.hibiscusmc.hmcclaims.config.gui.MainClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.SubClaimManageConfig;
import com.hibiscusmc.hmcclaims.config.internal.ConfigFactory;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
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

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-list.yml")), ClaimListConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "member-list.yml")), ClaimMemberListConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "manage-main-claim.yml")), MainClaimManageConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "manage-sub-claim.yml")), SubClaimManageConfig.class, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bind(new TypeReference<ConfigHolder<Settings>>() {
        }).toInstance(ConfigFactory.getHolder(Settings.class));

        bind(new TypeReference<ConfigHolder<Messages>>() {
        }).toInstance(ConfigFactory.getHolder(Messages.class));

        bind(new TypeReference<ConfigHolder<DefaultRoles>>() {
        }).toInstance(ConfigFactory.getHolder(DefaultRoles.class));

        bind(new TypeReference<ConfigHolder<ClaimListConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimListConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimMemberListConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimMemberListConfig.class));

        bind(new TypeReference<ConfigHolder<MainClaimManageConfig>>() {
        }).toInstance(ConfigFactory.getHolder(MainClaimManageConfig.class));

        bind(new TypeReference<ConfigHolder<SubClaimManageConfig>>() {
        }).toInstance(ConfigFactory.getHolder(SubClaimManageConfig.class));
    }
}