package com.hibiscusmc.hmcclaims.command.argument;

import com.hibiscusmc.hmcclaims.claim.Claim;
import com.hibiscusmc.hmcclaims.claim.ClaimAccess;
import com.hibiscusmc.hmcclaims.claim.ClaimManager;
import com.hibiscusmc.hmcclaims.claim.ClaimMember;
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

    @Inject
    private ClaimAccess access;

    @Override
    public CommandPart createPart(String name, List<? extends Annotation> modifiers) {
        return new ClaimMemberPart(name, manager, access);
    }

    public static class ClaimMemberPart implements ArgumentPart {

        private final String name;
        private final ClaimManager manager;
        private final ClaimAccess access;

        public ClaimMemberPart(String name, ClaimManager manager, ClaimAccess access) {
            this.name = name;
            this.manager = manager;
            this.access = access;
        }

        @Override
        public List<ClaimMember> parseValue(CommandContext context, ArgumentStack stack, CommandPart caller) throws ArgumentParseException {
            Claim claim = claimOf(context);
            if (claim == null) {
                return Collections.emptyList();
            }

            // Members carry their last known name, so no server cache lookup is needed
            String playerName = stack.next();
            for (ClaimMember member : claim.members()) {
                if (member.lastKnownName().equalsIgnoreCase(playerName)) {
                    return Collections.singletonList(member);
                }
            }

            return Collections.emptyList();
        }

        @Override
        public List<String> getSuggestions(CommandContext context, ArgumentStack stack) {
            Claim claim = claimOf(context);
            if (claim == null) {
                return Collections.emptyList();
            }

            CommandSender sender = context.getObject(CommandSender.class, "sender");
            if (!(sender instanceof Player player) || !access.canOpen(player, claim)) {
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

        private Claim claimOf(CommandContext context) {
            CommandSender sender = context.getObject(CommandSender.class, "sender");
            if (!(sender instanceof Player player)) {
                return null;
            }

            return manager.getClaimAt(player.getLocation())
                    .orElse(null);
        }
    }
}
