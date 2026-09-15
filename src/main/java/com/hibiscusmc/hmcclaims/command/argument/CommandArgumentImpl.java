package com.hibiscusmc.hmcclaims.command.argument;

import com.hibiscusmc.hmcclaims.claim.ClaimMember;
import team.unnamed.commandflow.annotated.part.AbstractModule;
import team.unnamed.commandflow.annotated.part.Key;
import team.unnamed.inject.Inject;
import team.unnamed.inject.Injector;

public class CommandArgumentImpl extends AbstractModule {

    @Inject
    private Injector injector;

    @Override
    public void configure() {
        bindFactory(new Key(String.class, PlayerName.class), new PlayerNameArgument());
        bindFactory(new Key(ClaimMember.class, Member.class), injector.getInstance(ClaimMemberArgument.class));
    }
}
