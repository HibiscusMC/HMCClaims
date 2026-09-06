package com.hibiscusmc.hmcclaims.command;

import com.hibiscusmc.hmcclaims.gui.impl.ClaimListGui;
import com.hibiscusmc.hmcclaims.menu.MenuService;
import org.bukkit.entity.Player;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.annotation.Command;
import team.unnamed.commandflow.annotated.annotation.Sender;
import team.unnamed.inject.Inject;

@Command(names = {"claimlist", "claims"}, permission = "hmcclaims.commands.list")
public class ClaimListCommand implements CommandClass {

    @Inject
    private MenuService menus;

    @Command(names = {""})
    public void list(@Sender Player sender) {
        menus.open(ClaimListGui.class, sender);
    }
}