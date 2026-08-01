package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.gui.ClaimBannedListConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimDeleteConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimListConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberListConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberPermissionsConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimMemberRoleConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimRoleManageConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimRolesConfig;
import com.hibiscusmc.hmcclaims.config.gui.ClaimSettingsConfig;
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
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-members.yml")), ClaimMemberListConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-banlist.yml")), ClaimBannedListConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-roles.yml")), ClaimRolesConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-settings.yml")), ClaimSettingsConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-manage.yml")), MainClaimManageConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "sub-claim-manage.yml")), SubClaimManageConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "role-manage.yml")), ClaimRoleManageConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "member-manage-role.yml")), ClaimMemberRoleConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "member-manage-permissions.yml")), ClaimMemberPermissionsConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("guis", "claim-delete-confirm.yml")), ClaimDeleteConfig.class, true);
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

        bind(new TypeReference<ConfigHolder<ClaimBannedListConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimBannedListConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimRolesConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimRolesConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimSettingsConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimSettingsConfig.class));

        bind(new TypeReference<ConfigHolder<MainClaimManageConfig>>() {
        }).toInstance(ConfigFactory.getHolder(MainClaimManageConfig.class));

        bind(new TypeReference<ConfigHolder<SubClaimManageConfig>>() {
        }).toInstance(ConfigFactory.getHolder(SubClaimManageConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimRoleManageConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimRoleManageConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimMemberRoleConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimMemberRoleConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimMemberPermissionsConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimMemberPermissionsConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimDeleteConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimDeleteConfig.class));
    }
}