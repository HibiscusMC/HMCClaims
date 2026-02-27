package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.util.TextUtil;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;

public class CustomItemSerializer implements TypeSerializer<ItemStack> {

    public final static CustomItemSerializer INSTANCE = new CustomItemSerializer();

    private final static String MATERIAL = "material";
    private final static String NAME = "name";
    private final static String LORE = "lore";

    private CustomItemSerializer() {
    }

    @Override
    public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.childrenMap().isEmpty()) {
            String rawStack = node.getString();
            if (rawStack == null || rawStack.isEmpty()) {
                throw new SerializationException("Missing ItemStack");
            }

            ItemStack stack = Hooks.getItem(rawStack);
            if (stack == null) {
                throw new SerializationException("Invalid id for item " + rawStack);
            }

            return stack;
        }

        String material = get(node.node(MATERIAL), String.class);
        if (material == null) {
            throw new SerializationException("Missing material for item");
        }

        ItemStack stack = Hooks.getItem(material);
        if (stack == null) {
            throw new SerializationException("Invalid id for item " + material);
        }

        ItemMeta meta = stack.getItemMeta();

        String name = get(node.node(NAME), String.class);
        if (name != null) {
            meta.customName(TextUtil.parseItem(name));
        }

        List<String> lore = getList(node.node(LORE), String.class);
        if (lore != null) {
            meta.lore(lore.stream().map(TextUtil::parseItem).toList());
        }

        stack.setItemMeta(meta);
        return stack;
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack stack, ConfigurationNode node) throws SerializationException {
        if (stack == null) {
            node.raw(null);
            throw new SerializationException("ItemStack is null");
        }

        String id = Hooks.getStringItem(stack);
        if (id.contains(":")) {
            node.set(id);
            return;
        }

        ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : null;
        if (meta == null) {
            node.set(id);
            return;
        }

        boolean setToRoot = true;

        String name = null;
        if (meta.hasCustomName()) {
            name = TextUtil.unparse(meta.customName());
            setToRoot = false;
        }

        List<String> lore = null;
        if (meta.hasLore()) { // noinspection DataFlowIssue
            lore = meta.lore().stream().map(TextUtil::unparse).toList();
            setToRoot = false;
        }

        if (setToRoot) {
            node.set(id);
            return;
        }

        node.node(MATERIAL).set(id);
        node.node(NAME).set(name);
        node.node(LORE).setList(String.class, lore);
    }

    private <T> List<T> getList(ConfigurationNode node, Class<T> clazz) throws SerializationException {
        return node.virtual() ? null : node.getList(clazz);
    }

    private <T> T get(ConfigurationNode node, Class<T> clazz) throws SerializationException {
        return node.virtual() ? null : node.get(clazz);
    }
}