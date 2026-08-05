package com.hibiscusmc.hmcclaims.util;

import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.Settings;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.user.User;
import com.hibiscusmc.hmcclaims.user.UserManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility for parsing and sending messages using the MiniMessage format.
 */
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class TextUtil {

    private final static MiniMessage MINI_MESSAGE
            = MiniMessage.miniMessage();

    private final static MiniMessage SAFE_MINI_MESSAGE
            = MiniMessage.builder()
            .tags(TagResolver.resolver(
                    StandardTags.color(),
                    StandardTags.gradient(),
                    StandardTags.rainbow(),
                    StandardTags.decorations(),
                    StandardTags.pride()
            ))
            .build();

    @Inject
    private ConfigHolder<Messages> messages;

    @Inject
    private ConfigHolder<Settings> settings;

    @Inject
    private UserManager userManager;

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
    public void send(@NotNull Audience audience, String string, Map data) {
        if (string == null || string.isEmpty()) {
            return;
        }

        audience.sendMessage(parseWithPrefix(string, data));
    }

    /**
     * Sends a MiniMessage-formatted notification string to a user.
     * This method takes into consideration the notification-cooldown set in the config.
     *
     * @param audience The recipient.
     * @param string   The message content.
     */
    public void sendNotification(@NotNull Audience audience, String string) {
        sendNotification(audience, string, Map.of());
    }

    /**
     * Sends a MiniMessage-formatted notification string with placeholders to a user.
     * This method takes into consideration the notification-cooldown set in the config.
     *
     * @param audience The recipient.
     * @param string   The message content.
     * @param data     A map of placeholders and their replacement values.
     */
    public void sendNotification(@NotNull Audience audience, String string, Map data) {
        if (string == null || string.isEmpty()) {
            return;
        }

        long notificationCooldown = settings.get().notificationCooldown();

        if (notificationCooldown > -1) {
            User user = audience.get(Identity.UUID)
                    .flatMap(uuid -> userManager.getUser(uuid))
                    .orElse(null);

            if (user != null) {
                if (System.currentTimeMillis() > (user.lastNotificationSent() + notificationCooldown)) {
                    return;
                }

                user.lastNotificationSent(System.currentTimeMillis());
            }
        }

        audience.sendMessage(parseWithPrefix(string, data));
    }

    /**
     * Parses a string into a {@link Component}, including the configured prefix.
     */
    @NotNull
    public Component parseWithPrefix(String string) {
        return parseWithPrefix(string, false);
    }

    /**
     * Parses a string with placeholders into a {@link Component}, including the configured prefix.
     */
    @NotNull
    public Component parseWithPrefix(String string, Map<String, Object> data) {
        return parseWithPrefix(string, true, data);
    }

    /**
     * Parses a string into a {@link Component}, optionally including the configured prefix.
     */
    @NotNull
    public Component parseWithPrefix(String string, boolean withPrefix) {
        return parseWithPrefix(string, withPrefix, Map.of());
    }

    /**
     * Core logic for parsing strings with prefix support and placeholder resolution.
     */
    @NotNull
    public Component parseWithPrefix(@NotNull String string, boolean withPrefix, Map<String, Object> data) {
        if (string.isEmpty()) {
            return Component.empty();
        }

        return parse(
                string, withPrefix ?
                        MapUtil.add(new HashMap<>(data), "prefix", messages.get().prefix()) :
                        data
        );
    }

    /**
     * Static helper to parse a list of MiniMessage strings.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static List<Component> parseList(List<String> list) {
        return parseList(list, Map.of());
    }

    /**
     * Static helper to parse a list of MiniMessage strings with placeholders.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static List<Component> parseList(List<String> list, Map data) {
        return list.stream().map(string -> parse(string, data)).toList();
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
    public static Component parse(@NotNull String string, Map data) {
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
     * Specialized parser for Item lores.
     * <p>
     * Ensures items are not italicized by default and uses white as a base color.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static List<Component> parseItemLore(List<String> list) {
        return parseItemLore(list, Map.of());
    }

    /**
     * Specialized parser for Item lores with placeholders.
     * <p>
     * Ensures items are not italicized by default and uses white as a base color.
     */
    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static List<Component> parseItemLore(List<String> list, Map data) {
        return list.stream().map(string -> parseItem(string, data)).toList();
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
    public static Component parseItem(@NotNull String string, Map data) {
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
     * Converts a list of {@link Component}s back into a MiniMessage string.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static List<String> unparse(@NotNull List<Component> list) {
        return list.stream().map(TextUtil::unparse).collect(Collectors.toList());
    }

    /**
     * Converts a {@link Component} back into a MiniMessage string.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String unparse(Component component) {
        if (component == null) {
            return "";
        }

        String serialized = MINI_MESSAGE.serialize(component);

        if (serialized.startsWith("<!italic>")) {
            serialized = serialized.substring(9);
        }

        return serialized;
    }

    /**
     * Parses only color + decoration related tags in a message
     *
     * @param unsafeText the input message to parse
     * @return the output, with only the safe tags parsed
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Component safe(String unsafeText) {
        return SAFE_MINI_MESSAGE.deserialize(unsafeText);
    }

    /**
     * Removes all tags from the text.
     *
     * @param unsafeText the input message, with potential tags
     * @return the output, without tags
     * @see MiniMessage#stripTags(String)
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static String strip(String unsafeText) {
        return MINI_MESSAGE.stripTags(unsafeText);
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
    private static TagResolver resolvePlaceholders(@NotNull Map<String, Object> data) {
        return TagResolver.resolver(data.entrySet().stream()
                .map(entry -> {
                    @Subst("id")
                    String key = entry.getKey();

                    Object value = entry.getValue();

                    if (value instanceof Component component) {
                        return Placeholder.component(key, component);
                    } else {
                        return Placeholder.parsed(key, value.toString());
                    }
                })
                .toList());
    }
}