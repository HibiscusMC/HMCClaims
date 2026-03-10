package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.command.ClaimBlocksCommand;
import com.hibiscusmc.hmcclaims.command.ClaimCommand;
import com.hibiscusmc.hmcclaims.command.ClaimListCommand;
import com.hibiscusmc.hmcclaims.command.HMCClaimsCommand;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.inject.AbstractModule;

public class CommandModule extends AbstractModule {

    @Override
    protected void configure() {
        multibind(CommandClass.class)
                .asSet()
                .to(HMCClaimsCommand.class)
                .to(ClaimBlocksCommand.class)
                .to(ClaimListCommand.class)
                .to(ClaimCommand.class);
    }
}