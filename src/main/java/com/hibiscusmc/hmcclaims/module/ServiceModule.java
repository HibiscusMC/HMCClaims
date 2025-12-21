package com.hibiscusmc.hmcclaims.module;

import com.hibiscusmc.hmcclaims.service.CommandService;
import com.hibiscusmc.hmcclaims.service.ListenerService;
import com.hibiscusmc.hmcclaims.service.Service;
import com.hibiscusmc.hmcclaims.service.StorageService;
import team.unnamed.inject.AbstractModule;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        multibind(Service.class)
                .asSet()
                .to(CommandService.class)
                .to(ListenerService.class)
                .to(StorageService.class);
    }

}