package com.hibiscusmc.hmcclaims.config.internal.serializer;

import me.lojosho.hibiscuscommons.hooks.Hooks;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class CustomItemSerializer implements TypeSerializer<ItemStack> {

    public final static CustomItemSerializer INSTANCE = new CustomItemSerializer();

    private CustomItemSerializer() {
    }

    @Override
    public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String rawStack = node.getString();
        if (rawStack == null || rawStack.isEmpty()) {
            return null;
        }

        return Hooks.getItem(rawStack);
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack stack, ConfigurationNode node) throws SerializationException {
        if (stack == null) {
            node.raw(null);
            throw new SerializationException("ItemStack is null");
        }

        node.set(Hooks.getStringItem(stack));
    }
}