package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.config.AppConfig;

import java.util.Arrays;

public final class BybitConfig {
    // Public endpoints are defined in application.properties
    private static final String PUBLIC_TESTNET_SPOT = AppConfig.getAsString("bybit.public.testnet.spot");
    private static final String PUBLIC_TESTNET_LINEAR = AppConfig.getAsString("bybit.public.testnet.linear");
    private static final String PUBLIC_TESTNET_INVERSE = AppConfig.getAsString("bybit.public.testnet.inverse");
    private static final String PUBLIC_TESTNET_OPTION = AppConfig.getAsString("bybit.public.testnet.option");
    private static final String PUBLIC_TESTNET_SPREAD = AppConfig.getAsString("bybit.public.testnet.spread");
    // Public topics are defined in application.properties
    private static final String[] PUBLIC_SUBSCRIBE_TOPICS = AppConfig.getAsString("bybit.public.subscribe.topics").split(",");
    // Private topics are defined in application.properties
    private static final String[] PRIVATE_SUBSCRIBE_TOPICS = AppConfig.getAsString("bybit.private.subscribe.topics").split(",");
    // Ping interval is defined in application.properties
    private static final long PING_INTERVAL = AppConfig.getAsLong("bybit.ping.interval");
    // Reconnect interval are defined in application.properties
    private static final long RECONNECT_INTERVAL = AppConfig.getAsLong("bybit.reconnect.interval");

    private BybitConfig() {
        throw new UnsupportedOperationException();
    }

    public static String getPublicTestnetSpot() {
        return PUBLIC_TESTNET_SPOT;
    }

    public static String getPublicTestnetLinear() {
        return PUBLIC_TESTNET_LINEAR;
    }

    public static String getPublicTestnetInverse() {
        return PUBLIC_TESTNET_INVERSE;
    }

    public static String getPublicTestnetOption() {
        return PUBLIC_TESTNET_OPTION;
    }

    public static String getPublicTestnetSpread() {
        return PUBLIC_TESTNET_SPREAD;
    }

    public static String[] getPublicSubscribeTopics() {
        return PUBLIC_SUBSCRIBE_TOPICS;
    }

    public static String[] getPrivateSubscribeTopics() {
        return PRIVATE_SUBSCRIBE_TOPICS;
    }

    public static long getPingInterval() {
        return PING_INTERVAL;
    }

    public static long getReconnectInterval() {
        return RECONNECT_INTERVAL;
    }

    public static String print() {
        return "Bybit Config {" +
                "publicTestnetSpot=" + getPublicTestnetSpot() +
                ", publicTestnetLinear=" + getPublicTestnetLinear() +
                ", publicTestnetInverse=" + getPublicTestnetInverse() +
                ", publicTestnetOption=" + getPublicTestnetOption() +
                ", publicTestnetSpread=" + getPublicTestnetSpread() +
                ", publicSubscribeTopics=" + Arrays.toString(getPublicSubscribeTopics()) +
                ", privateSubscribeTopics=" + Arrays.toString(getPrivateSubscribeTopics()) +
                ", pingInterval=" + getPingInterval() +
                ", reconnectInterval=" + getReconnectInterval() +
                '}';
    }
}
