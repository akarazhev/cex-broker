package com.github.akarazhev.cexbroker.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class Commands {
    private static final Logger LOGGER = LoggerFactory.getLogger(Commands.class);
    private final Map<String, Class<? extends Command>> commands = new HashMap<>();

    private Commands() {
        commands.put("PRINT", PrintCommand.class);
        commands.put("LOG", LogCommand.class);
    }

    public static Commands init() {
        return new Commands();
    }

    public Command create(final String commandType, final String message) {
        try {
            final Class<? extends Command> commandClass = commands.get(commandType.toUpperCase());
            if (commandClass != null) {
                return commandClass.getDeclaredConstructor(String.class).newInstance(message);
            }
        } catch (final Exception e) {
            LOGGER.error("Error creating command", e);
        }

        return null;
    }
}
