package com.hibiscusmc.hmcclaims.service;

import team.unnamed.commandflow.CommandManager;
import team.unnamed.commandflow.annotated.AnnotatedCommandTreeBuilder;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.part.PartInjector;
import team.unnamed.commandflow.annotated.part.defaults.DefaultsModule;
import team.unnamed.commandflow.bukkit.BukkitMapCommandManager;
import team.unnamed.commandflow.bukkit.factory.BukkitModule;
import team.unnamed.inject.Inject;

import java.util.Set;

public class CommandService implements Service {

    @Inject
    private Set<CommandClass> commands;

    @Override
    public void start() {
        PartInjector injector = PartInjector.create();
        injector.install(new DefaultsModule());
        injector.install(new BukkitModule());

        CommandManager manager = new BukkitMapCommandManager("hmcclaims");
        AnnotatedCommandTreeBuilder treeBuilder = AnnotatedCommandTreeBuilder.create(injector);

        for (CommandClass command : commands) {
            manager.registerCommands(treeBuilder.fromClass(command));
        }
    }

    @Override
    public void reload() {
    }

    @Override
    public void stop() {
    }

}