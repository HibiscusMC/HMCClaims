package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.gui.GuiRegistry;
import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.inject.Inject;

@Command(names = {"claimlist", "claims"}, permission = "hmcclaims.commands.list")
public class ClaimListCommand implements CommandClass {

    @Inject
    private GuiRegistry guis;

    @Command(names = {""})
    public void list(@Sender Player sender) {
        ClaimListGui gui = guis.get(ClaimListGui.class);

        gui.open(sender);
    }
}