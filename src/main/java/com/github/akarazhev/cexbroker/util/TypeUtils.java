package com.github.akarazhev.cexbroker.util;

public final class TypeUtils {

    private TypeUtils() {
        throw new UnsupportedOperationException();
    }

    public static Boolean asBoolean(final Object value) {
        return value != null && !value.toString().isEmpty() ? Boolean.valueOf(value.toString()) : null;
    }

    public static Double asDouble(final Object value) {
        return value != null && !value.toString().isEmpty() ? Double.valueOf(value.toString()) : null;
    }

    public static Integer asInteger(final Object value) {
        return value != null && !value.toString().isEmpty() ? Integer.valueOf(value.toString()) : null;
    }

    public static Long asLong(final Object value) {
        return value != null && !value.toString().isEmpty() ? Long.valueOf(value.toString()) : null;
    }
}
