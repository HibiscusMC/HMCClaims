package com.hibiscusmc.hmcclaims.util;

import com.hibiscusmc.hmcclaims.config.ConfigHolder;
import com.hibiscusmc.hmcclaims.config.Messages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Subst;
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

    public void send(Audience audience, String string, Map<String, String> data) {
        audience.sendMessage(parse(string, true, data));
    }

    public Component parse(String string) {
        return parse(string, true);
    }

    public Component parse(String string, Map<String, String> data) {
        return parse(string, true, data);
    }

    public Component parse(String string, boolean withPrefix) {
        return parse(string, withPrefix, Map.of());
    }

    public Component parse(String string, boolean withPrefix, Map<String, String> data) {
        if (withPrefix) {
            string = prefix(string);
        }

        if (data.isEmpty()) {
            return MINI_MESSAGE.deserialize(string);
        }

        List<TagResolver.Single> resolvers = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            @Subst("placeholder")
            String key = entry.getKey();

            resolvers.add(Placeholder.parsed(key, entry.getValue()));
        }

        return MINI_MESSAGE.deserialize(string, TagResolver.resolver(resolvers));
    }

    public String prefix(String string) {
        return messages.get().prefix() + string;
    }

}