package com.github.akarazhev.cexbroker.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class LogCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogCommand.class);
    private final String message;

    public LogCommand(final String message) {
        this.message = message;
    }

    @Override
    public void execute() {
        LOGGER.info("Logging: {}", message);
    }
}
