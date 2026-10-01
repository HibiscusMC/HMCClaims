package com.hibiscusmc.hmcclaims.config;

import com.hibiscusmc.hmcclaims.util.Logger;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import lombok.AccessLevel;
import lombok.Getter;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An item defined in a config file.
 * <p>
 * The {@link ItemStack} is only built when {@link #stack()} is first called, because custom item
 * ids (ItemsAdder, Oraxen, ...) can't be resolved until their plugins have loaded.
 */
@Getter
public class ConfigItem {

    public final static Set<String> COMPONENTS = Set.of(
            "custom_model_data", "item_model", "enchantment_glint_override", "hide_tooltip", "unbreakable"
    );

    private final String material;

    @Nullable
    private final String name;

    @Nullable
    private final List<String> lore;

    private final List<String> flags;

    private final Map<String, Object> components;

    private final String path;

    @Getter(AccessLevel.NONE)
    private volatile ItemStack cached;

    public ConfigItem(
            @NotNull String material, @Nullable String name, @Nullable List<String> lore,
            @NotNull List<String> flags, @NotNull Map<String, Object> components, @NotNull String path
    ) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.flags = flags;
        this.components = components;
        this.path = path;
    }

    @NotNull
    @Contract("_ -> new")
    public static ConfigItem of(@NotNull Material material) {
        return of(material, null, null);
    }

    @NotNull
    @Contract("_, _ -> new")
    public static ConfigItem of(@NotNull Material material, @Nullable String name) {
        return of(material, name, null);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    public static ConfigItem of(@NotNull Material material, @Nullable String name, @Nullable List<String> lore) {
        return new ConfigItem(material.name().toLowerCase(), name, lore, List.of(), Map.of(), "");
    }

    /**
     * Whether only the material is set, so it can be written as a plain string.
     */
    public boolean simple() {
        return name == null && lore == null && flags.isEmpty() && components.isEmpty();
    }

    /**
     * The vanilla material of this item, or {@code null} if it comes from another plugin.
     */
    @Nullable
    public Material vanilla() {
        return Material.matchMaterial(material);
    }

    /**
     * Returns a fresh copy of the item. Falls back to a barrier when the id can't be resolved.
     */
    @NotNull
    public ItemStack stack() {
        ItemStack base = cached;

        if (base == null) {
            base = resolve();
            cached = base;
        }

        return base.clone();
    }

    @NotNull
    private ItemStack resolve() {
        Material vanilla = vanilla();
        ItemStack stack = vanilla != null ? ItemStack.of(vanilla) : Hooks.getItem(material);

        if (stack == null) {
            Logger.error("Invalid item id '{}' at '{}'", material, path);
            return ItemStack.of(Material.BARRIER);
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        if (name != null) {
            meta.itemName(TextUtil.parseItem(name));
        }

        if (lore != null) {
            meta.lore(lore.stream().map(TextUtil::parseItem).toList());
        }

        if (!flags.isEmpty()) {
            meta.setAttributeModifiers(stack.getType().getDefaultAttributeModifiers());

            for (String flag : flags) {
                if (!EnumUtils.isValidEnum(ItemFlag.class, flag)) {
                    Logger.warning("Unknown item flag '{}' at '{}'", flag, path);
                    continue;
                }

                meta.addItemFlags(ItemFlag.valueOf(flag));
            }
        }

        stack.setItemMeta(meta);

        components.forEach((key, value) -> applyComponent(stack, key, value));

        return stack;
    }

    private void applyComponent(ItemStack stack, String rawKey, Object value) {
        @Subst("namespace:key")
        String subst = rawKey;
        Key key = Key.key(subst);

        switch (key.value()) {
            case "custom_model_data" -> {
                CustomModelData.Builder builder = CustomModelData.customModelData();

                if (value instanceof List<?> list) {
                    list.forEach(element -> builder.addFloat(toFloat(element)));
                } else {
                    builder.addFloat(toFloat(value));
                }

                stack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build());
            }

            case "item_model" -> stack.setData(DataComponentTypes.ITEM_MODEL, Key.key(String.valueOf(value)));

            case "enchantment_glint_override" ->
                    stack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, toBoolean(value));

            case "hide_tooltip" -> stack.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                    .hideTooltip(toBoolean(value))
                    .build());

            case "unbreakable" -> {
                if (toBoolean(value)) {
                    stack.setData(DataComponentTypes.UNBREAKABLE);
                }
            }

            default -> Logger.warning("Unknown item component '{}' at '{}'", rawKey, path);
        }
    }

    private static float toFloat(Object value) {
        return value instanceof Number number ? number.floatValue() : Float.parseFloat(String.valueOf(value));
    }

    private static boolean toBoolean(Object value) {
        return value == null || Boolean.parseBoolean(String.valueOf(value));
    }
}