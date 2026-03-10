package com.hibiscusmc.hmcclaims.service;

import com.hibiscusmc.hmcclaims.command.argument.CommandArgumentImpl;
import com.hibiscusmc.hmcclaims.config.Messages;
import com.hibiscusmc.hmcclaims.config.internal.ConfigHolder;
import com.hibiscusmc.hmcclaims.util.TextUtil;
import org.bukkit.command.CommandSender;
import team.unnamed.commandflow.CommandManager;
import team.unnamed.commandflow.annotated.AnnotatedCommandTreeBuilder;
import team.unnamed.commandflow.annotated.CommandClass;
import team.unnamed.commandflow.annotated.part.PartInjector;
import team.unnamed.commandflow.annotated.part.defaults.DefaultsModule;
import team.unnamed.commandflow.bukkit.BukkitMapCommandManager;
import team.unnamed.commandflow.bukkit.factory.BukkitModule;
import team.unnamed.commandflow.command.Command;
import team.unnamed.commandflow.exception.ArgumentParseException;
import team.unnamed.commandflow.exception.CommandUsage;
import team.unnamed.commandflow.exception.NoPermissionsException;
import team.unnamed.inject.Inject;

import java.util.Map;
import java.util.Set;

public class CommandService implements Service {

    @Inject
    private Set<CommandClass> commands;

    @Inject
    private ConfigHolder<Messages> messagesHolder;

    @Inject
    private TextUtil text;

    @Override
    public void start() {
        PartInjector injector = PartInjector.create();
        injector.install(new DefaultsModule());
        injector.install(new BukkitModule());
        injector.install(new CommandArgumentImpl());

        CommandManager manager = new BukkitMapCommandManager("hmcclaims");
        AnnotatedCommandTreeBuilder treeBuilder = AnnotatedCommandTreeBuilder.create(injector);

        manager.getErrorHandler().addExceptionHandler(CommandUsage.class, (namespace, part) -> {
            Messages messages = messagesHolder.get();

            CommandSender sender = namespace.getObject(CommandSender.class, "sender");
            String label = namespace.getObject(String.class, "label");
            Command command = part.getCommand();

            text.send(sender, messages.commands().usage(), Map.of(
                    "command", label,
                    "usage", TextUtil.unparse(command.getUsage())
            ));

            return true;
        });

        manager.getErrorHandler().addExceptionHandler(NoPermissionsException.class, (namespace, error) -> {
            Messages messages = messagesHolder.get();

            CommandSender sender = namespace.getObject(CommandSender.class, "sender");

            text.send(sender, messages.commands().noPermission());

            return true;
        });

        manager.getErrorHandler().addExceptionHandler(ArgumentParseException.class, (namespace, error) -> {
            Messages messages = messagesHolder.get();

            CommandSender sender = namespace.getObject(CommandSender.class, "sender");
            String message = error.getMessage();

            if (message.contains("translatable")) {
                message = message.split("translatable:")[1]
                        .trim();

                if (message.endsWith("%")) {
                    message = message.substring(0, message.length() - 1);
                }
            }

            switch (message.toLowerCase()) {
                case "player.offline": {
                    text.send(sender, messages.commands().playerNotFound());
                    break;
                }

                default: {
                    text.send(sender, messages.commands().invalidArgument());
                    break;
                }
            }

            return true;
        });

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