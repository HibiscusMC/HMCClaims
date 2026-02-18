package com.hibiscusmc.hmcclaims.command.argument;

import org.bukkit.OfflinePlayer;
import team.unnamed.commandflow.annotated.part.AbstractModule;
import team.unnamed.commandflow.annotated.part.Key;

public class CommandArgumentImpl extends AbstractModule {

    @Override
    public void configure() {
        bindFactory(new Key(OfflinePlayer.class, PlayerOrOffline.class), new PlayerOrOfflineArgument());
    }
}