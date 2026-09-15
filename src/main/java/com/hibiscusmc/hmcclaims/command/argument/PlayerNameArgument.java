package com.hibiscusmc.hmcclaims.command.argument;

import org.bukkit.Bukkit;
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

public class PlayerNameArgument implements PartFactory {

    @Override
    public CommandPart createPart(String name, List<? extends Annotation> modifiers) {
        return new PlayerNamePart(name);
    }

    public static class PlayerNamePart implements ArgumentPart {

        private final String name;

        public PlayerNamePart(String name) {
            this.name = name;
        }

        @Override
        public List<String> parseValue(CommandContext context, ArgumentStack stack, CommandPart caller) throws ArgumentParseException {
            return Collections.singletonList(stack.next());
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
