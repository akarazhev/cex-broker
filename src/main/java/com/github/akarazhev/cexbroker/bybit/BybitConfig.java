package com.github.akarazhev.cexbroker.bybit;

import com.github.akarazhev.cexbroker.config.AppConfig;

import java.util.Arrays;

public final class BybitConfig {
    private static final String PUBLIC_TESTNET_SPOT = AppConfig.getAsString("bybit.public.testnet.spot");
    private static final String[] PUBLIC_SUBSCRIBE_TOPICS = AppConfig.getAsString("bybit.public.subscribe.topics").split(",");
    private static final long PING_INTERVAL = AppConfig.getAsLong("bybit.ping.interval");
    private static final long RECONNECT_INTERVAL = AppConfig.getAsLong("bybit.reconnect.interval");

    private BybitConfig() {
        throw new UnsupportedOperationException();
    }

    public static String getPublicTestnetSpot() {
        return PUBLIC_TESTNET_SPOT;
    }

    public static String[] getPublicSubscribeTopics() {
        return PUBLIC_SUBSCRIBE_TOPICS;
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
                ", publicSubscribeTopics=" + Arrays.toString(getPublicSubscribeTopics()) +
                ", pingInterval=" + getPingInterval() +
                ", reconnectInterval=" + getReconnectInterval() +
                '}';
    }
}
