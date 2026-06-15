package com.hibiscusmc.hmcclaims.command.argument;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.CommandContext;
import team.unnamed.commandflow.annotated.part.PartFactory;
import team.unnamed.commandflow.exception.ArgumentParseException;
import team.unnamed.commandflow.part.ArgumentPart;
import team.unnamed.commandflow.part.CommandPart;
import team.unnamed.commandflow.stack.ArgumentStack;
import team.unnamed.inject.Inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClaimMemberArgument implements PartFactory {

    @Inject
    private ClaimManager manager;

    @Override
    public CommandPart createPart(String name, List<? extends Annotation> modifiers) {
        return new ClaimMemberPart(name, manager);
    }

    public static class ClaimMemberPart implements ArgumentPart {

        private final String name;
        private final ClaimManager manager;

        public ClaimMemberPart(String name, ClaimManager manager) {
            this.name = name;
            this.manager = manager;
        }

        @Override
        public List<OfflinePlayer> parseValue(CommandContext context, ArgumentStack stack, CommandPart caller) throws ArgumentParseException {
            CommandSender sender = context.getObject(CommandSender.class, "sender");
            if (!(sender instanceof Player player)) {
                return Collections.emptyList();
            }

            Claim claim = manager.getClaimAt(player.getLocation())
                    .orElse(null);

            if (claim == null) {
                return Collections.emptyList();
            }

            String playerName = stack.next();
            Player target = Bukkit.getPlayer(playerName);
            if (target != null) {
                return claim.getMember(target.getUniqueId()).isPresent() ?
                        Collections.singletonList(target) :
                        Collections.emptyList();
            }

            OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(playerName);
            if (offline != null && offline.getName() != null && offline.hasPlayedBefore()) {
                return claim.getMember(offline.getUniqueId()).isPresent() ?
                        Collections.singletonList(offline) :
                        Collections.emptyList();
            }

            return Collections.emptyList();
        }

        @Override
        public List<String> getSuggestions(CommandContext context, ArgumentStack stack) {
            CommandSender sender = context.getObject(CommandSender.class, "sender");
            if (!(sender instanceof Player player)) {
                return Collections.emptyList();
            }

            Claim claim = manager.getClaimAt(player.getLocation())
                    .orElse(null);

            if (claim == null) {
                return Collections.emptyList();
            }

            String prefix = stack.hasNext() ? stack.next().toLowerCase() : "";
            List<String> suggestions = new ArrayList<>();

            for (ClaimMember member : claim.members()) {
                String playerName = member.lastKnownName();

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