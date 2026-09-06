package com.hibiscusmc.hmcclaims.form;

import com.hibiscusmc.hmcclaims.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts the plugin's MiniMessage strings into text a Bedrock client can render.
 */
public final class FormText {

    /**
     * Serializer that emits section-sign codes. Hex support is deliberately left off so
     * Adventure downsamples RGB colors and gradients to the sixteen legacy colors,
     * which is the full palette Bedrock forms can display.
     */
    private final static LegacyComponentSerializer LEGACY
            = LegacyComponentSerializer.legacySection();

    /**
     * Tags that render as an image or a font glyph on Java and as nothing (or garbage) on
     * Bedrock. Stripped before parsing so their arguments never leak into the output.
     */
    private final static Pattern UNSUPPORTED_TAGS
            = Pattern.compile("</?(?:sprite|head|font|key|translate|lang|nbt|selector|score)(?::[^<>]*)?>",
            Pattern.CASE_INSENSITIVE);

    /**
     * Interaction tags. The wrapped text is kept, the interactivity is dropped.
     */
    private final static Pattern INTERACTION_TAGS
            = Pattern.compile("</?(?:hover|click|insert|insertion)(?::[^<>]*)?>", Pattern.CASE_INSENSITIVE);

    private FormText() {
    }

    /**
     * Converts a single configured line.
     *
     * @param raw The MiniMessage string from a form config.
     * @return Bedrock-ready text.
     */
    @NotNull
    @Contract(pure = true)
    public static String line(@Nullable String raw) {
        return line(raw, null, Map.of());
    }

    /**
     * Converts a single configured line.
     *
     * @param raw    The MiniMessage string from a form config.
     * @param player The viewer, used for PlaceholderAPI substitution.
     * @return Bedrock-ready text.
     */
    @NotNull
    @Contract(pure = true)
    public static String line(@Nullable String raw, @Nullable Player player) {
        return line(raw, player, Map.of());
    }

    /**
     * Converts a single configured line.
     *
     * @param raw    The MiniMessage string from a form config.
     * @param player The viewer, used for PlaceholderAPI substitution.
     * @param data   Placeholders made available as MiniMessage tags.
     * @return Bedrock-ready text.
     */
    @NotNull
    @Contract(pure = true)
    public static String line(@Nullable String raw, @Nullable Player player, @NotNull Map<String, ?> data) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        Component parsed = TextUtil.parse(sanitize(raw), player, data);

        return LEGACY.serialize(parsed);
    }

    /**
     * Converts a list of configured lines into one block of text, one line per entry.
     *
     * @param raw The MiniMessage strings from a form config.
     * @return Bedrock-ready text, or an empty string when the list is {@code null} or empty.
     */
    @NotNull
    @Contract(pure = true)
    public static String block(@Nullable List<String> raw) {
        return block(raw, null, Map.of());
    }

    /**
     * Converts a list of configured lines into one block of text, one line per entry.
     *
     * @param raw    The MiniMessage strings from a form config.
     * @param player The viewer, used for PlaceholderAPI substitution.
     * @param data   Placeholders made available as MiniMessage tags.
     * @return Bedrock-ready text, or an empty string when the list is {@code null} or empty.
     */
    @NotNull
    @Contract(pure = true)
    public static String block(@Nullable List<String> raw, @Nullable Player player, @NotNull Map<String, ?> data) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String value : raw) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }

            builder.append(line(value, player, data));
        }

        return builder.toString();
    }

    /**
     * Truncates a name to the configured maximum, appending an ellipsis when it had to be
     * cut.
     *
     * @param name      The name to shorten.
     * @param maxLength The maximum length, or a negative number to disable shortening.
     * @return The possibly shortened name.
     */
    @NotNull
    @Contract(pure = true)
    public static String shorten(@NotNull String name, int maxLength) {
        if (maxLength < 0 || name.length() <= maxLength) {
            return name;
        }

        return name.substring(0, maxLength).trim() + "...";
    }

    /**
     * Removes the MiniMessage tags Bedrock cannot render.
     *
     * @param raw The MiniMessage string to clean.
     * @return The string without unsupported tags.
     */
    @NotNull
    @Contract(pure = true)
    private static String sanitize(@NotNull String raw) {
        String cleaned = UNSUPPORTED_TAGS.matcher(raw).replaceAll("");

        return INTERACTION_TAGS.matcher(cleaned).replaceAll("");
    }
}