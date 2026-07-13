package com.hibiscusmc.hmcclaims.config.internal;

import com.hibiscusmc.hmcclaims.claim.permission.Permission;
import com.hibiscusmc.hmcclaims.claim.role.ClaimRole;
import com.hibiscusmc.hmcclaims.claim.setting.Setting;
import com.hibiscusmc.hmcclaims.config.internal.serializer.ActionSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.ClaimRoleSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.CustomItemSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.PermissionSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.RangeSerializer;
import com.hibiscusmc.hmcclaims.config.internal.serializer.SettingSerializer;
import com.hibiscusmc.hmcclaims.gui.Action;
import com.hibiscusmc.hmcclaims.util.RangeUtil;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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

/**
 * A central factory for loading, saving, and managing configuration files.
 */
public class ConfigFactory {

    /**
     * Registry of all managed configuration classes and their respective holders.
     */
    private final static Map<Class<?>, ConfigHolder<?>> CONFIG_FILES
            = new HashMap<>();

    /**
     * Reloads an existing configuration file from its stored path.
     *
     * @param clazz The configuration class to reload.
     * @throws Exception If the file is missing or the YAML is malformed.
     */
    public static <T> void reload(@NotNull Class<T> clazz) throws Exception {
        if (!CONFIG_FILES.containsKey(clazz)) {
            throw new NullPointerException("No registered config file found for class " + clazz);
        }

        // noinspection unchecked
        ConfigHolder<T> holder = (ConfigHolder<T>) CONFIG_FILES.get(clazz);
        load(holder.path(), clazz);
    }

    /**
     * Loads a configuration file into memory, forcefully performing the initial I/O.
     *
     * @param path  The physical location of the .yml file.
     * @param clazz The class to map the YAML data into.
     * @throws Exception If serialization fails.
     */
    public static <T> void load(Path path, Class<T> clazz) throws Exception {
        load(path, clazz, false);
    }

    /**
     * Loads a configuration file into memory.
     *
     * @param path      The physical location of the .yml file.
     * @param clazz     The class to map the YAML data into.
     * @param loadLater If true, stores the path without performing the initial I/O.
     * @throws Exception If serialization fails.
     */
    public static <T> void load(Path path, Class<T> clazz, boolean loadLater) throws Exception {
        if (loadLater) {
            // noinspection unchecked
            ConfigHolder<T> holder = (ConfigHolder<T>) CONFIG_FILES.computeIfAbsent(clazz, k -> new ConfigHolder<>());
            holder.path(path);

            return;
        }

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
                                .register(RangeUtil.class, RangeSerializer.INSTANCE)
                                .register(Setting.class, SettingSerializer.INSTANCE)
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

    /**
     * Retrieves the holder for a specific config class.
     *
     * @return The {@link ConfigHolder}, or {@code null} if not yet loaded.
     */
    public static <T> ConfigHolder<T> getHolder(Class<T> clazz) {
        // noinspection unchecked
        return (ConfigHolder<T>) CONFIG_FILES.get(clazz);
    }

    /**
     * Retrieves every currently stored config class.
     *
     * @param deferred {@code true} if should only return the config files that are not yet loaded.
     * @return An array of {@link Class}es associated to every config file.
     */
    public static Class<?>[] all(boolean deferred) {
        return CONFIG_FILES.entrySet().stream()
                .filter(entry -> !deferred || entry.getValue().get() == null)
                .map(Map.Entry::getKey)
                .toArray(Class<?>[]::new);
    }
}