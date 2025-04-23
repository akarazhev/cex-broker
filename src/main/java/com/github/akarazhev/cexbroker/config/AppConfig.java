package com.github.akarazhev.cexbroker.config;

import java.io.IOException;
import java.util.Properties;

public final class AppConfig {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (final var input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found in classpath");
            }

            PROPERTIES.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load application.properties", ex);
        }
    }

    private AppConfig() {
        throw new UnsupportedOperationException();
    }

    public static String getAsString(final String key) {
        return get(key);
    }

    public static boolean getAsBoolean(final String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static long getAsLong(final String key) {
        return Long.parseLong(get(key));
    }

    private static String get(final String key) {
        var value = System.getenv(key);
        if (value != null) {
            return value;
        }

        value = PROPERTIES.getProperty(key);
        if (value != null) {
            return value;
        }

        throw new RuntimeException("Property " + key + " not found");
    }
}
