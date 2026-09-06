package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.config.DefaultRoles;
import com.hibiscusmc.hmcclaims.config.DefaultSettings;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.form.ClaimBannedListFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimDeleteFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimListFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberListFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberPermissionsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimMemberRoleFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimRoleManageFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimRolesFormConfig;
import com.hibiscusmc.hmcclaims.config.form.ClaimSettingsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.DialogsFormConfig;
import com.hibiscusmc.hmcclaims.config.form.MainClaimManageFormConfig;
import com.hibiscusmc.hmcclaims.config.form.SubClaimManageFormConfig;
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
            ConfigFactory.load(pluginPath.resolve("default-settings.yml"), DefaultSettings.class);

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

            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-list.yml")), ClaimListFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-members.yml")), ClaimMemberListFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-banlist.yml")), ClaimBannedListFormConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-roles.yml")), ClaimRolesFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-settings.yml")), ClaimSettingsFormConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-manage.yml")), MainClaimManageFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "sub-claim-manage.yml")), SubClaimManageFormConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "role-manage.yml")), ClaimRoleManageFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "member-manage-role.yml")), ClaimMemberRoleFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "member-manage-permissions.yml")), ClaimMemberPermissionsFormConfig.class, true);

            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "claim-delete-confirm.yml")), ClaimDeleteFormConfig.class, true);
            ConfigFactory.load(pluginPath.resolve(Path.of("forms", "dialogs.yml")), DialogsFormConfig.class, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bind(new TypeReference<ConfigHolder<Settings>>() {
        }).toInstance(ConfigFactory.getHolder(Settings.class));

        bind(new TypeReference<ConfigHolder<Messages>>() {
        }).toInstance(ConfigFactory.getHolder(Messages.class));

        bind(new TypeReference<ConfigHolder<DefaultRoles>>() {
        }).toInstance(ConfigFactory.getHolder(DefaultRoles.class));

        bind(new TypeReference<ConfigHolder<DefaultSettings>>() {
        }).toInstance(ConfigFactory.getHolder(DefaultSettings.class));

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

        bind(new TypeReference<ConfigHolder<ClaimListFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimListFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimMemberListFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimMemberListFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimBannedListFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimBannedListFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimRolesFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimRolesFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimSettingsFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimSettingsFormConfig.class));

        bind(new TypeReference<ConfigHolder<MainClaimManageFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(MainClaimManageFormConfig.class));

        bind(new TypeReference<ConfigHolder<SubClaimManageFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(SubClaimManageFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimRoleManageFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimRoleManageFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimMemberRoleFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimMemberRoleFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimMemberPermissionsFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimMemberPermissionsFormConfig.class));

        bind(new TypeReference<ConfigHolder<ClaimDeleteFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(ClaimDeleteFormConfig.class));

        bind(new TypeReference<ConfigHolder<DialogsFormConfig>>() {
        }).toInstance(ConfigFactory.getHolder(DialogsFormConfig.class));
    }
}