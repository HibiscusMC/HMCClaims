package com.hibiscusmc.hmcclaims.command.argument;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.CommandContext;
import team.unnamed.commandflow.annotated.part.PartFactory;
import team.unnamed.commandflow.exception.ArgumentParseException;
import team.unnamed.commandflow.part.ArgumentPart;
import team.unnamed.commandflow.part.CommandPart;
import team.unnamed.commandflow.stack.ArgumentStack;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerOrOfflineArgument implements PartFactory {

    @Override
    public CommandPart createPart(String name, List<? extends Annotation> modifiers) {
        return new PlayerOrOfflinePart(name);
    }

    public static class PlayerOrOfflinePart implements ArgumentPart {

        private final String name;

        public PlayerOrOfflinePart(String name) {
            this.name = name;
        }

        @Override
        public List<OfflinePlayer> parseValue(CommandContext context, ArgumentStack stack, CommandPart caller) throws ArgumentParseException {
            String playerName = stack.next();
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                return Collections.singletonList(player);
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(playerName);
            if (offline != null) {
                return Collections.singletonList(offline);
            }

            return Collections.emptyList();
        }

        @Override
        public List<String> getSuggestions(CommandContext commandContext, ArgumentStack stack) {
            String prefix = stack.hasNext() ? stack.next().toLowerCase() : "";
            List<String> suggestions = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) {
                String playerName = player.getName();

                if (prefix.isEmpty() || playerName.toLowerCase().startsWith(prefix)) {
                    suggestions.add(playerName);
                }
            }

            return suggestions;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}