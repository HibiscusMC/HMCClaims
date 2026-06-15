package com.hibiscusmc.hmcclaims.command.argument;

import org.bukkit.OfflinePlayer;
import team.unnamed.commandflow.annotated.part.AbstractModule;
import team.unnamed.commandflow.annotated.part.Key;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

public class CommandArgumentImpl extends AbstractModule {

    @Inject
    private Injector injector;

    @Override
    public void configure() {
        bindFactory(new Key(OfflinePlayer.class, PlayerOrOffline.class), new PlayerOrOfflineArgument());
        bindFactory(new Key(OfflinePlayer.class, ClaimMember.class), injector.getInstance(ClaimMemberArgument.class));
    }
}