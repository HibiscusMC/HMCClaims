package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.permission.PermissionRegistry;
import com.hibiscusmc.hmcclaims.util.RegistryUtil;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Subst;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class PermissionSerializer implements TypeSerializer<Permission> {

    public final static PermissionSerializer INSTANCE = new PermissionSerializer();

    private PermissionSerializer() {
    }

    @Override
    public Permission deserialize(Type type, ConfigurationNode node) throws SerializationException {
        @Subst("permission_id")
        String rawPermission = node.getString();

        if (rawPermission == null) {
            return null;
        }

        Permission permission = PermissionRegistry.getPermission(rawPermission);

        if (permission == null) {
            throw new SerializationException("Could not find permission '" + rawPermission + "'");
        }

        return permission;
    }

    @Override
    public void serialize(Type type, @Nullable Permission permission, ConfigurationNode node) throws SerializationException {
        if (permission == null) {
            node.raw(null);
            throw new SerializationException("Permission is null");
        }

        Key key = permission.key();
        node.set(RegistryUtil.serialize(key));
    }
}