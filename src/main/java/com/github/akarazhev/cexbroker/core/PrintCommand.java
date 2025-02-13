package com.github.akarazhev.cexbroker.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PrintCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrintCommand.class);
    private final String message;

    public PrintCommand(final String message) {
        this.message = message;
    }

    @Override
    public void execute() {
        LOGGER.info("Printing: {}", message);
    }
}
