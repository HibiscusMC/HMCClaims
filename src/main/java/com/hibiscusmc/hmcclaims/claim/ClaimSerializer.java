package com.hibiscusmc.hmcclaims.claim;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRoleRegistry;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.claim.setting.SettingHolder;
import com.hibiscusmc.hmcclaims.claim.setting.SettingRegistry;
import com.hibiscusmc.hmcclaims.proto.MemberData;
import com.hibiscusmc.hmcclaims.proto.MembersMessage;
import com.hibiscusmc.hmcclaims.proto.RoleData;
import com.hibiscusmc.hmcclaims.proto.RolesMessage;
import com.hibiscusmc.hmcclaims.proto.SettingsMessage;
import com.hibiscusmc.hmcclaims.util.ByteUtil;
import com.hibiscusmc.hmcclaims.util.Logger;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class responsible for serializing and deserializing claim sub-components into
 * highly compressed Protobuf binary payloads and vice versa.
 */
public class ClaimSerializer {

    /**
     * Serializes a claim's role registry structure into a compressed binary layout.
     *
     * @param roleRegistry The authority managing role hierarchies for a claim.
     * @return A sequential binary representation of the serialized roles' data.
     */
    public static byte @NotNull [] serialize(@NotNull ClaimRoleRegistry roleRegistry) {
        RolesMessage.Builder rolesMessageBuilder = RolesMessage.newBuilder();

        List<ClaimRole> roles = roleRegistry.allRoles();
        for (ClaimRole role : roles) {
            RoleData roleData = RoleData.newBuilder()
                    .setName(role.name())
                    .setPosition(roles.indexOf(role))
                    .addAllPermissions(role.permissions().stream().map(permission -> permission.key().asString()).toList())
                    .setCreationDate(role.creationTimestamp().toEpochMilli())
                    .build();

            rolesMessageBuilder.putRoles(role.id().toString(), roleData);
        }

        return rolesMessageBuilder.build().toByteArray();
    }

    /**
     * Serializes an active collection of claim members into a compressed binary layout.
     *
     * @param members A set representing all the members stored in the claim.
     * @return A sequential binary representation of the serialized members' data.
     */
    public static byte @NotNull [] serialize(@NotNull Set<ClaimMember> members) {
        MembersMessage.Builder membersMessageBuilder = MembersMessage.newBuilder();

        for (ClaimMember member : members) {
            MemberData.Builder memberDataBuilder = MemberData.newBuilder()
                    .setRoleId(ByteUtil.UUIDtoByteString(member.role().id()))
                    .setBanned(member.banned())
                    .setJoinDate(System.currentTimeMillis());

            for (Object2BooleanMap.Entry<Permission> entry : member.permissions().object2BooleanEntrySet()) {
                memberDataBuilder.putPermissions(entry.getKey().key().asString(), entry.getBooleanValue());
            }

            membersMessageBuilder.putMembers(member.uuid().toString(), memberDataBuilder.build());
        }

        return membersMessageBuilder.build().toByteArray();
    }

    /**
     * Serializes claim flags and configuration values into a compressed binary layout.
     *
     * @param settings The current claim flags and configuration values assigned to this claim instance.
     * @return A sequential binary representation of the serialized settings configuration.
     */
    public static byte @NotNull [] serialize(@NotNull Map<Setting<?>, SettingHolder<?>> settings) {
        SettingsMessage.Builder settingsMessageBuilder = SettingsMessage.newBuilder();

        for (Map.Entry<Setting<?>, SettingHolder<?>> settingEntry : settings.entrySet()) {
            settingsMessageBuilder.putSettings(
                    settingEntry.getKey().key().asString(),
                    settingEntry.getValue().value().toString()
            );
        }

        return settingsMessageBuilder.build().toByteArray();
    }

    /**
     * Deserializes a raw byte array back into a list of claim roles.
     */
    public static @NotNull List<ClaimRole> deserializeRoles(byte @NotNull [] bytes) {
        List<ClaimRole> roles = new ArrayList<>();
        if (bytes.length == 0) {
            return roles;
        }

        try {
            RolesMessage message = RolesMessage.parseFrom(bytes);
            message.getRolesMap().forEach((uuidStr, data) -> {
                UUID roleId = UUID.fromString(uuidStr);

                Set<Permission> permissions = data.getPermissionsList().stream()
                        .map(PermissionRegistry::getPermission)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                ClaimRole role = new ClaimRole(roleId, data.getName(), permissions);
                role.position(data.getPosition());
                role.creationTimestamp(Instant.ofEpochMilli(data.getCreationDate()));

                roles.add(role);
            });

            roles.sort(Comparator.comparingInt(ClaimRole::position));
        } catch (InvalidProtocolBufferException e) {
            Logger.error("Couldn't deserialize claim roles", e);
        }

        return roles;
    }

    /**
     * Deserializes a raw byte array back into a set of active claim members.
     */
    public static @NotNull Set<ClaimMember> deserializeMembers(byte @NotNull [] bytes, @NotNull Claim claim, @NotNull ClaimRoleRegistry roleRegistry) {
        Set<ClaimMember> members = new HashSet<>();
        if (bytes.length == 0) {
            return members;
        }

        try {
            MembersMessage message = MembersMessage.parseFrom(bytes);
            message.getMembersMap().forEach((uuidStr, data) -> {
                UUID playerId = UUID.fromString(uuidStr);
                UUID roleId = ByteUtil.byteStringToUUID(data.getRoleId());

                Optional<ClaimRole> role = roleRegistry.find(roleId);

                Object2BooleanMap<Permission> overrides = new Object2BooleanArrayMap<>();
                data.getPermissionsMap().forEach((permStr, status) -> {
                    Permission perm = PermissionRegistry.getPermission(permStr);

                    if (perm != null) {
                        overrides.put(perm, (boolean) status);
                    }
                });

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);

                ClaimMember member = new ClaimMember(
                        playerId, claim, offlinePlayer.hasPlayedBefore() ? offlinePlayer.getName() : "",
                        role.orElseThrow(), overrides);
                member.banned(data.getBanned());

                members.add(member);
            });
        } catch (InvalidProtocolBufferException e) {
            Logger.error("Couldn't deserialize claim members", e);
        }

        return members;
    }

    /**
     * Deserializes a raw byte array back into a claim's settings registry map.
     */
    public static @NotNull Map<Setting<?>, SettingHolder<?>> deserializeSettings(byte @NotNull [] bytes) {
        Map<Setting<?>, SettingHolder<?>> settingsMap = new HashMap<>();
        if (bytes.length == 0) {
            return settingsMap;
        }

        try {
            SettingsMessage message = SettingsMessage.parseFrom(bytes);
            message.getSettingsMap().forEach((keyStr, valStr) -> {
                Setting<?> setting = SettingRegistry.getSetting(keyStr);
                if (setting != null) {
                    // noinspection unchecked
                    SettingHolder<Object> holder = (SettingHolder<Object>) SettingHolder.from(setting);
                    holder.value(setting.parser().apply(valStr));

                    settingsMap.put(setting, holder);
                }
            });
        } catch (InvalidProtocolBufferException e) {
            Logger.error("Couldn't deserialize claim settings", e);
        }

        return settingsMap;
    }
}