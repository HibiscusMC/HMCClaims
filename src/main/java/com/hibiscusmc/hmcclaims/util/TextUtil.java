package com.hibiscusmc.hmcclaims.util;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.Map;

/**
 * Utility for parsing and sending messages using the MiniMessage format.
 */
@Singleton
public class TextUtil {

    private final static MiniMessage MINI_MESSAGE
            = MiniMessage.miniMessage();

    @Inject
    private ConfigHolder<Messages> messages;

    /**
     * Sends a MiniMessage-formatted string to an audience.
     *
     * @param audience The recipient (Player, Console, etc.).
     * @param string   The message content.
     */
    public void send(Audience audience, String string) {
        send(audience, string, Map.of());
    }

    /**
     * Sends a MiniMessage-formatted string with placeholders to an audience.
     *
     * @param audience The recipient.
     * @param string   The message content.
     * @param data     A map of placeholders and their replacement values.
     */
    public void send(@NotNull Audience audience, String string, Map<String, String> data) {
        audience.sendMessage(parseWithPrefix(string, data));
    }

    /**
     * Parses a string into a Component, including the configured prefix.
     */
    @NotNull
    public Component parseWithPrefix(String string) {
        return parseWithPrefix(string, false);
    }

    /**
     * Parses a string with placeholders into a Component, including the configured prefix.
     */
    @NotNull
    public Component parseWithPrefix(String string, Map<String, String> data) {
        return parseWithPrefix(string, true, data);
    }

    /**
     * Parses a string into a Component, optionally including the configured prefix.
     */
    @NotNull
    public Component parseWithPrefix(String string, boolean withPrefix) {
        return parseWithPrefix(string, withPrefix, Map.of());
    }

    /**
     * Core logic for parsing strings with prefix support and placeholder resolution.
     */
    @NotNull
    public Component parseWithPrefix(@NotNull String string, boolean withPrefix, Map<String, String> data) {
        if (string.isEmpty()) {
            return Component.empty();
        }

        if (withPrefix) {
            string = prefix(string);
        }

        return parse(string, data);
    }

    /**
     * Static helper to parse a simple MiniMessage string.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Component parse(String string) {
        return parse(string, Map.of());
    }

    /**
     * Static helper to parse a MiniMessage string with placeholders.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static Component parse(@NotNull String string, Map<String, String> data) {
        if (string.isEmpty()) {
            return Component.empty();
        }

        if (data.isEmpty()) {
            return MINI_MESSAGE.deserialize(string);
        }

        TagResolver resolver = resolvePlaceholders(data);
        return MINI_MESSAGE.deserialize(string, TagResolver.resolver(resolver));
    }

    /**
     * Specialized parser for Item display names and lore.
     * <p>
     * Ensures items are not italicized by default and uses white as a base color.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Component parseItem(String string) {
        return parseItem(string, Map.of());
    }

    /**
     * Specialized parser for Item display names and lore with placeholders.
     * <p>
     * Ensures items are not italicized by default and uses white as a base color.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static Component parseItem(@NotNull String string, Map<String, String> data) {
        if (string.isEmpty()) {
            return Component.empty();
        }

        if (data.isEmpty()) {
            return MINI_MESSAGE.deserialize(string)
                    .colorIfAbsent(NamedTextColor.WHITE)
                    .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }

        TagResolver resolver = resolvePlaceholders(data);

        return MINI_MESSAGE
                .deserialize(string, resolver)
                .colorIfAbsent(NamedTextColor.WHITE)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /**
     * Converts a Component back into a MiniMessage string.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String unparse(Component component) {
        String serialized = MINI_MESSAGE.serialize(component);

        if (serialized.startsWith("<!italic>")) {
            serialized = serialized.substring(9);
        }

        return serialized;
    }

    /**
     * Prepends the plugin-wide message prefix to a raw string.
     * <p>
     * This method retrieves the current prefix from the {@link #messages} config holder.
     *
     * @param string The message content to be prefixed.
     * @return A concatenated string of [prefix] + [string].
     */
    @NotNull
    private String prefix(String string) {
        return messages.get().prefix() + string;
    }

    /**
     * Converts a map of raw string data into a MiniMessage {@link TagResolver}.
     * <p>
     * This method iterates through the map and treats each key as a placeholder
     * and each value as a parsed MiniMessage string.
     *
     * @param data The map of placeholder keys and values.
     * @return A combined {@link TagResolver} containing all provided placeholders.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static TagResolver resolvePlaceholders(@NotNull Map<String, String> data) {
        return TagResolver.resolver(data.entrySet().stream()
                .map(entry -> {
                    @Subst("id")
                    String key = entry.getKey();

                    return Placeholder.parsed(key, entry.getValue());
                })
                .toList());
    }
}