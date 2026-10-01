package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.config.ClaimSettings;
import com.hibiscusmc.hmcclaims.config.ConfigItem;
import com.hibiscusmc.hmcclaims.config.ConfigRegistry;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Permissions;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.adapter.ActionAdapter;
import com.hibiscusmc.hmcclaims.config.adapter.ConfigItemAdapter;
import com.hibiscusmc.hmcclaims.config.adapter.PermissionAdapter;
import com.hibiscusmc.hmcclaims.config.adapter.RangeAdapter;
import com.hibiscusmc.hmcclaims.config.adapter.RegistryMapAdapter;
import com.hibiscusmc.hmcclaims.config.adapter.SettingAdapter;
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
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import org.bukkit.plugin.Plugin;
import team.hypox.config.core.ConfigHolder;
import team.hypox.config.core.ConfigLoader;
import team.hypox.config.yaml.YamlFormat;
import team.unnamed.inject.AbstractModule;
import team.unnamed.inject.key.TypeReference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigModule extends AbstractModule {

    private final static String HEADER = """
            ┌─────────────────────────────────────────────────────────────────────┐
            │                                                                     │
            │       |   |   \\  |   ___|   ___|  |        _)                       │
            │       |   |  |\\/ |  |      |      |   _` |  |  __ `__ \\    __|      │
            │       ___ |  |   |  |      |      |  (   |  |  |   |   | \\__ \\      │
            │      _|  _| _|  _| \\____| \\____| _| \\__,_| _| _|  _|  _| ____/      │
            │                                                                     │
            │                A modern claims engine designed for                  │
            │             performance, flexibility, and reliability.              │
            │                                                                     │
            │                    © Hibiscus Creative Studios                      │
            │                                                                     │
            └─────────────────────────────────────────────────────────────────────┘""";

    private final static List<String> LEGACY_FILES = List.of(
            "config.yml", "messages.yml", "default-roles.yml", "default-settings.yml", "guis", "forms"
    );

    private final Plugin plugin;

    private final List<ConfigHolder<?>> holders = new ArrayList<>();

    private ConfigLoader loader;

    public ConfigModule(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void configure() {
        Path path = plugin.getDataPath();

        RegistryMapAdapter<Permission, Permissions.Entry> permissionMap = new RegistryMapAdapter<>(
                "permission",
                type -> type == Permission.class,
                PermissionRegistry::getPermission,
                permission -> RegistryUtil.serialize(permission.key()),
                PermissionRegistry::getAllPermissions,
                Permissions.Entry::of
        );

        RegistryMapAdapter<Setting<?>, ClaimSettings.Entry> settingMap = new RegistryMapAdapter<>(
                "setting",
                SettingAdapter::matches,
                SettingRegistry::getSetting,
                setting -> RegistryUtil.serialize(setting.key()),
                SettingRegistry::getAllSettings,
                ClaimSettings.Entry::of
        );

        loader = ConfigLoader.builder()
                .format(YamlFormat.create())
                .directory(path)
                .header(HEADER.split("\n"))
                .adapters(adapters -> adapters
                        .register(Action.class, new ActionAdapter())
                        .register(RangeUtil.class, new RangeAdapter())
                        .register(ConfigItem.class, new ConfigItemAdapter())
                        .register(Permission.class, new PermissionAdapter())
                        .register(SettingAdapter::matches, new SettingAdapter())
                        .register(permissionMap::matches, permissionMap)
                        .register(settingMap::matches, settingMap)
                )
                .build();

        load(Settings.class, "config.yml");
        load(Messages.class, "messages.yml");
        load(Permissions.class, "permissions.yml");
        load(ClaimSettings.class, "claim-settings.yml");

        load(ClaimListConfig.class, "guis/claim-list.yml");
        load(ClaimMemberListConfig.class, "guis/claim-members.yml");
        load(ClaimBannedListConfig.class, "guis/claim-banlist.yml");

        load(ClaimRolesConfig.class, "guis/claim-roles.yml");
        load(ClaimSettingsConfig.class, "guis/claim-settings.yml");

        load(MainClaimManageConfig.class, "guis/claim-manage.yml");
        load(SubClaimManageConfig.class, "guis/sub-claim-manage.yml");

        load(ClaimRoleManageConfig.class, "guis/role-manage.yml");
        load(ClaimMemberRoleConfig.class, "guis/member-manage-role.yml");
        load(ClaimMemberPermissionsConfig.class, "guis/member-manage-permissions.yml");

        load(ClaimDeleteConfig.class, "guis/claim-delete-confirm.yml");

        load(ClaimListFormConfig.class, "forms/claim-list.yml");
        load(ClaimMemberListFormConfig.class, "forms/claim-members.yml");
        load(ClaimBannedListFormConfig.class, "forms/claim-banlist.yml");

        load(ClaimRolesFormConfig.class, "forms/claim-roles.yml");
        load(ClaimSettingsFormConfig.class, "forms/claim-settings.yml");

        load(MainClaimManageFormConfig.class, "forms/claim-manage.yml");
        load(SubClaimManageFormConfig.class, "forms/sub-claim-manage.yml");

        load(ClaimRoleManageFormConfig.class, "forms/role-manage.yml");
        load(ClaimMemberRoleFormConfig.class, "forms/member-manage-role.yml");
        load(ClaimMemberPermissionsFormConfig.class, "forms/member-manage-permissions.yml");

        load(ClaimDeleteFormConfig.class, "forms/claim-delete-confirm.yml");
        load(DialogsFormConfig.class, "forms/dialogs.yml");

        bind(ConfigRegistry.class).toInstance(new ConfigRegistry(holders));
    }

    private <T> void load(Class<T> type, String file) {
        ConfigHolder<T> holder = loader.load(type, file);

        holders.add(holder);

        bind(TypeReference.<ConfigHolder<T>>of(ConfigHolder.class, type)).toInstance(holder);
    }
}