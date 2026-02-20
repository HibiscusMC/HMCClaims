package com.hibiscusmc.hmcclaims.config.internal;

import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.config.internal.serializer.ActionSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.ClaimRoleSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.CustomItemSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.PermissionSerializer;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.permission.Permission;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.objectmapping.meta.Processor;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.yaml.internal.snakeyaml.DumperOptions;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigFactory {

    private final static Map<Class<?>, ConfigHolder<?>> CONFIG_FILES
            = new HashMap<>();

    public static <T> void reload(Class<T> clazz) throws Exception {
        if (!CONFIG_FILES.containsKey(clazz)) {
            throw new NullPointerException("No registered config file found for class " + clazz);
        }

        // noinspection unchecked
        ConfigHolder<T> holder = (ConfigHolder<T>) CONFIG_FILES.get(clazz);
        load(holder.path(), clazz);
    }

    public static <T> void load(Path path, Class<T> clazz) throws Exception {
        ObjectMapper.Factory factory = ObjectMapper.factoryBuilder()
                .addNodeResolver(NodeResolver.nodeFromParent())
                .addProcessor(Comment.class, Processor.comments())
                .build();

        YamlConfigurationLoader.Builder builder = YamlConfigurationLoader.builder()
                .path(path)
                .defaultOptions(opts -> opts
                        .shouldCopyDefaults(true)
                        .serializers(build -> build
                                .registerAnnotatedObjects(factory)
                                .register(ItemStack.class, CustomItemSerializer.INSTANCE)
                                .register(Permission.class, PermissionSerializer.INSTANCE)
                                .register(ClaimRole.class, ClaimRoleSerializer.INSTANCE)
                                .register(Action.class, ActionSerializer.INSTANCE)
                        )
                )
                .indent(2)
                .nodeStyle(NodeStyle.BLOCK);

        Field optsField = builder.getClass().getDeclaredField("options");
        optsField.setAccessible(true);

        DumperOptions dumperOptions = (DumperOptions) optsField.get(builder);
        dumperOptions.setWidth(Integer.MAX_VALUE);

        YamlConfigurationLoader loader = builder.build();

        CommentedConfigurationNode node = loader.load();
        T instance = node.get(clazz);

        loader.save(node);

        // noinspection unchecked
        ConfigHolder<T> holder = (ConfigHolder<T>) CONFIG_FILES.computeIfAbsent(clazz, k -> new ConfigHolder<>());
        holder.update(instance);
        holder.path(path);
    }

    public static <T> ConfigHolder<T> getHolder(Class<T> clazz) {
        // noinspection unchecked
        return (ConfigHolder<T>) CONFIG_FILES.get(clazz);
    }
}