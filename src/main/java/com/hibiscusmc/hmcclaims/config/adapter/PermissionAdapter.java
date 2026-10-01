package com.hibiscusmc.hmcclaims.config.adapter;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.hypox.config.core.ConfigException;
import team.hypox.config.core.ConfigValue;
import team.hypox.config.core.adapter.Adapter;

import java.lang.reflect.Type;

public class PermissionAdapter implements Adapter<Permission> {

    @Override
    public Permission read(@NotNull Type type, @NotNull ConfigValue source) {
        if (source.isMap() || source.isList()) {
            throw new ConfigException(source, "Expected a permission id");
        }

        String raw = String.valueOf(source.unwrap());
        Permission permission = PermissionRegistry.getPermission(raw);

        if (permission == null) {
            throw new ConfigException(source, "Unknown permission '" + raw + "'");
        }

        return permission;
    }

    @Override
    public void write(@NotNull Type type, @Nullable Permission value, @NotNull ConfigValue target) {
        target.set(value == null ? null : RegistryUtil.serialize(value.key()));
    }
}
