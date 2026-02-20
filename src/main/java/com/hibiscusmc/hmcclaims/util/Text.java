package com.hibiscusmc.hmcclaims.util;

import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class Text {

    @Inject
    private ConfigHolder<Messages> messages;

    private final static MiniMessage MINI_MESSAGE
            = MiniMessage.miniMessage();

    public void send(Audience audience, String string) {
        send(audience, string, Map.of());
    }

    public void send(@NotNull Audience audience, String string, Map<String, String> data) {
        audience.sendMessage(parse(string, data));
    }

    public Component parse(String string) {
        return parse(string, false);
    }

    public Component parse(String string, Map<String, String> data) {
        return parse(string, true, data);
    }

    public Component parse(String string, boolean withPrefix) {
        return parse(string, withPrefix, Map.of());
    }

    public Component parse(@NotNull String string, boolean withPrefix, Map<String, String> data) {
        if (string.isEmpty()) {
            return Component.empty();
        }

        if (withPrefix) {
            string = prefix(string);
        }

        if (data.isEmpty()) {
            return MINI_MESSAGE.deserialize(string);
        }

        TagResolver resolver = resolvePlaceholders(data);
        return MINI_MESSAGE.deserialize(string, TagResolver.resolver(resolver));
    }

    public static Component parseItem(String string) {
        return parseItem(string, Map.of());
    }

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

    @NotNull
    public static String unparse(Component component) {
        String serialized = MINI_MESSAGE.serialize(component);

        if (serialized.startsWith("<!italic>")) {
            serialized = serialized.substring(9);
        }

        return serialized;
    }

    @NotNull
    private String prefix(String string) {
        return messages.get().prefix() + string;
    }

    @NotNull
    private static TagResolver resolvePlaceholders(@NotNull Map<String, String> data) {
        List<TagResolver.Single> resolvers = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            @Subst("placeholder")
            String key = entry.getKey();

            resolvers.add(Placeholder.parsed(key, entry.getValue()));
        }

        return TagResolver.resolver(resolvers);
    }
}