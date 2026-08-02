package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.hook.Hook;
import com.hibiscusmc.hmcclaims.hook.papi.PlaceholderAPIHook;
import team.unnamed.inject.AbstractModule;

public class HookModule extends AbstractModule {

    @Override
    public void configure() {
        multibind(Hook.class)
                .asSet()
                .to(PlaceholderAPIHook.class);
    }
}