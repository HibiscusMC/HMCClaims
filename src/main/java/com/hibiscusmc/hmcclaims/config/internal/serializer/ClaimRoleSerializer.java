package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ClaimRoleSerializer implements TypeSerializer<ClaimRole> {

    private final static String NAME = "name";
    private final static String PERMISSIONS = "permissions";

    public final static ClaimRoleSerializer INSTANCE = new ClaimRoleSerializer();

    private ClaimRoleSerializer() {
    }

    @Override
    public ClaimRole deserialize(Type type, ConfigurationNode node) throws SerializationException {
        ConfigurationNode nameNode = node.node(NAME);
        ConfigurationNode permissionsNode = node.node(PERMISSIONS);

        if (nameNode.virtual()) {
            return null;
        }

        if (permissionsNode.virtual()) {
            return null;
        }

        String name = nameNode.getString();
        List<Permission> permissions = permissionsNode.getList(Permission.class);

        return new ClaimRole(null, name, permissions == null ? new HashSet<>() : new HashSet<>(permissions));
    }

    @Override
    public void serialize(Type type, @Nullable ClaimRole role, ConfigurationNode node) throws SerializationException {
        if (role == null) {
            node.raw(null);
            throw new SerializationException("Claim role is null");
        }

        node.node(NAME).set(role.name());
        node.node(PERMISSIONS).setList(Permission.class, new ArrayList<>(role.permissions()));
    }

}