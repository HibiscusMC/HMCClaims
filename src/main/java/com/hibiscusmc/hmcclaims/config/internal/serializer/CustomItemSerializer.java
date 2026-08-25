package com.hibiscusmc.hmcclaims.config.internal.serializer;

import com.hibiscusmc.hmcclaims.util.TextUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.intellij.lang.annotations.Subst;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class CustomItemSerializer implements TypeSerializer<ItemStack> {

    public final static CustomItemSerializer INSTANCE = new CustomItemSerializer();

    private final static String MATERIAL = "material";
    private final static String NAME = "name";
    private final static String LORE = "lore";
    private final static String COMPONENTS = "components";
    private final static String ITEM_FLAGS = "item-flags";

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
            meta.itemName(TextUtil.parseItem(name));
        }

        List<String> lore = getList(node.node(LORE), String.class);
        if (lore != null) {
            meta.lore(lore.stream().map(TextUtil::parseItem).toList());
        }

        List<String> flags = getList(node.node(ITEM_FLAGS), String.class);
        if (flags != null) {
            meta.setAttributeModifiers(stack.getType().getDefaultAttributeModifiers());
            for (String itemFlag : flags) {
                if (!EnumUtils.isValidEnum(ItemFlag.class, itemFlag)) continue;
                meta.addItemFlags(ItemFlag.valueOf(itemFlag));
            }
        }

        stack.setItemMeta(meta);

        ConfigurationNode componentsNode = node.node(COMPONENTS);
        if (!componentsNode.virtual() && componentsNode.isMap()) {
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : componentsNode.childrenMap().entrySet()) {
                @Subst("namespace:key")
                String rawKey = entry.getKey().toString();
                Key key = Key.key(rawKey);
                ConfigurationNode componentVal = entry.getValue();

                switch (key.value()) {
                    case "custom_model_data" -> {
                        if (componentVal.isList()) {
                            List<Float> floats = getList(componentVal, Float.class);
                            if (floats != null) {
                                CustomModelData.Builder builder = CustomModelData.customModelData();
                                floats.forEach(builder::addFloat);
                                stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build());
                            }
                        } else {
                            int cmdInt = componentVal.getInt();
                            stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(cmdInt).build());
                        }
                    }
                    case "item_model" -> {
                        String itemModel = componentVal.getString();
                        if (itemModel != null) {
                            stack.setData(DataComponentTypes.ITEM_MODEL, Key.key(itemModel));
                        }
                    }
                    case "enchantment_glint_override" ->
                            stack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, componentVal.getBoolean(true));
                    case "hide_tooltip" ->
                            stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                                    .hideTooltip(componentVal.getBoolean(true))
                                    .build());
                    case "unbreakable" -> {
                        if (componentVal.getBoolean(true)) {
                            stack.setData(DataComponentTypes.UNBREAKABLE);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + key);
                }
            }
        }

        return stack;
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack stack, ConfigurationNode node) throws SerializationException {
        if (stack == null) {
            node.raw(null);
            throw new SerializationException("ItemStack is null");
        }

        String id = Hooks.getStringItem(stack);

        ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : null;
        if (meta == null && !stack.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)
                && !stack.hasData(DataComponentTypes.ITEM_NAME)
                && !stack.hasData(DataComponentTypes.ITEM_MODEL)
                && !stack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)
                && !stack.hasData(DataComponentTypes.TOOLTIP_DISPLAY)
                && !stack.hasData(DataComponentTypes.UNBREAKABLE)) {
            node.node(MATERIAL).set(id);
            return;
        }

        String name = null;
        if (meta != null && meta.hasItemName()) {
            name = TextUtil.unparse(meta.itemName());
        }

        List<String> lore = null;
        if (meta != null && meta.hasLore()) { // noinspection DataFlowIssue
            lore = TextUtil.unparse(meta.lore());
        }

        node.node(MATERIAL).set(id);

        if (name != null) {
            node.node(NAME).set(name);
        }

        if (lore != null) {
            node.node(LORE).setList(String.class, lore);
        }

        boolean hasComponents = false;
        ConfigurationNode componentsNode = node.node(COMPONENTS);

        if (stack.hasData(DataComponentTypes.CUSTOM_MODEL_DATA)) {
            CustomModelData cmd = stack.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (cmd != null && !cmd.floats().isEmpty()) {
                componentsNode.node("custom_model_data").setList(Float.class, cmd.floats());
                hasComponents = true;
            }
        }

        if (stack.hasData(DataComponentTypes.ITEM_MODEL)) {
            Key modelKey = stack.getData(DataComponentTypes.ITEM_MODEL);
            if (modelKey != null && !modelKey.asString().equals(stack.getType().getKey().asString())) {
                componentsNode.node("item_model").set(modelKey.asString());
                hasComponents = true;
            }
        }

        if (stack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE)) {
            Boolean glint = stack.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
            if (glint != null) {
                componentsNode.node("enchantment_glint_override").set(glint);
                hasComponents = true;
            }
        }

        if (stack.hasData(DataComponentTypes.TOOLTIP_DISPLAY) && stack.getData(DataComponentTypes.TOOLTIP_DISPLAY).hideTooltip()) {
            componentsNode.node("hide_tooltip").set(true);
            hasComponents = true;
        }

        if (stack.hasData(DataComponentTypes.UNBREAKABLE)) {
            componentsNode.node("unbreakable").set(true);
            hasComponents = true;
        }

        if (!hasComponents) {
            node.removeChild(COMPONENTS);
        }
    }

    private <T> List<T> getList(ConfigurationNode node, Class<T> clazz) throws SerializationException {
        return node.virtual() ? null : node.getList(clazz);
    }

    private <T> T get(ConfigurationNode node, Class<T> clazz) throws SerializationException {
        return node.virtual() ? null : node.get(clazz);
    }
}