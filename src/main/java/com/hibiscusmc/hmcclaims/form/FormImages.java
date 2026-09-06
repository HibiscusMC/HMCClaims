package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.config.form.FormTemplate;
import com.hibiscusmc.hmcclaims.form.spec.FormImage;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Resolves the icon shown next to a Bedrock form button.
 */
public final class FormImages {

    /**
     * Bedrock texture paths for the materials the shipped GUI configs use, plus the ones
     * whose Bedrock name differs enough from the Java name that the generic rule below
     * would miss them.
     */
    private final static Map<Material, String> OVERRIDES = Map.ofEntries(
            Map.entry(Material.GRASS_BLOCK, "textures/blocks/grass_side_carried"),
            Map.entry(Material.DIRT, "textures/blocks/dirt"),
            Map.entry(Material.HOPPER, "textures/blocks/hopper_outside"),
            Map.entry(Material.BARRIER, "textures/blocks/barrier"),
            Map.entry(Material.OAK_SIGN, "textures/items/sign"),
            Map.entry(Material.BOOK, "textures/items/book_normal"),
            Map.entry(Material.WRITABLE_BOOK, "textures/items/book_writable"),
            Map.entry(Material.SPYGLASS, "textures/items/spyglass"),
            Map.entry(Material.ARROW, "textures/items/arrow"),
            Map.entry(Material.PLAYER_HEAD, "textures/items/skull_steve"),
            Map.entry(Material.LIME_DYE, "textures/items/dye_powder_lime"),
            Map.entry(Material.GRAY_DYE, "textures/items/dye_powder_gray"),
            Map.entry(Material.RED_DYE, "textures/items/dye_powder_red"),
            Map.entry(Material.GREEN_DYE, "textures/items/dye_powder_green"),
            Map.entry(Material.LIME_STAINED_GLASS_PANE, "textures/blocks/glass_pane_top_lime"),
            Map.entry(Material.GRAY_STAINED_GLASS_PANE, "textures/blocks/glass_pane_top_gray"),
            Map.entry(Material.RED_STAINED_GLASS_PANE, "textures/blocks/glass_pane_top_red"),
            Map.entry(Material.GOLDEN_SHOVEL, "textures/items/gold_shovel"),
            Map.entry(Material.NETHER_STAR, "textures/items/nether_star"),
            Map.entry(Material.PAPER, "textures/items/paper"),
            Map.entry(Material.CHEST, "textures/blocks/chest_front"),
            Map.entry(Material.IRON_DOOR, "textures/items/door_iron")
    );

    private FormImages() {
    }

    /**
     * Resolves a configured image.
     *
     * @param config           The image block from a form config; may be {@code null}.
     * @param fallbackMaterial The material to derive a texture from when the config sets
     *                         no image; may be {@code null}.
     * @param materialFallback Whether material-derived textures are enabled.
     * @return The image, or {@code null} for a text-only button.
     */
    @Nullable
    @Contract(pure = true)
    public static FormImage resolve(
            @Nullable FormTemplate.Image config, @Nullable Material fallbackMaterial, boolean materialFallback
    ) {
        if (config != null && config.type() != FormTemplate.ImageType.NONE) {
            String data = config.data();

            if (data != null && !data.isBlank()) {
                return config.type() == FormTemplate.ImageType.URL
                        ? FormImage.url(data)
                        : FormImage.path(data);
            }
        }

        if (!materialFallback || fallbackMaterial == null) {
            return null;
        }

        String path = texture(fallbackMaterial);

        return path == null ? null : FormImage.path(path);
    }

    /**
     * Resolves a configured image, substituting {@code <name>} and {@code <uuid>} in the
     * data first. Lets member buttons point at an avatar service without the plugin
     * hard-coding one.
     *
     * @param config           The image block from a form config; may be {@code null}.
     * @param name             The player name to substitute.
     * @param uuid             The player uuid to substitute.
     * @param fallbackMaterial The material to derive a texture from; may be {@code null}.
     * @param materialFallback Whether material-derived textures are enabled.
     * @return The image, or {@code null} for a text-only button.
     */
    @Nullable
    @Contract(pure = true)
    public static FormImage resolvePlayer(
            @Nullable FormTemplate.Image config, @NotNull String name, @NotNull String uuid,
            @Nullable Material fallbackMaterial, boolean materialFallback
    ) {
        if (config == null || config.type() == FormTemplate.ImageType.NONE || config.data() == null) {
            return resolve(config, fallbackMaterial, materialFallback);
        }

        String data = config.data()
                .replace("<name>", name)
                .replace("<uuid>", uuid)
                .replace("<clean_uuid>", uuid.replace("-", ""));

        System.out.println(config.type());
        System.out.println(data);

        return config.type() == FormTemplate.ImageType.URL
                ? FormImage.url(data)
                : FormImage.path(data);
    }

    /**
     * Guesses the Bedrock texture path of a material.
     *
     * @param material The material to translate.
     * @return The texture path, or {@code null} for materials with no icon (such as air).
     */
    @Nullable
    @Contract(pure = true)
    public static String texture(@NotNull Material material) {
        if (material.isAir()) {
            return null;
        }

        String override = OVERRIDES.get(material);
        if (override != null) {
            return override;
        }

        String name = material.name().toLowerCase();

        return (material.isBlock() ? "textures/blocks/" : "textures/items/") + name;
    }
}