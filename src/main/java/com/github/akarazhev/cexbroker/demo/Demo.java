package com.github.akarazhev.cexbroker.demo;

import okhttp3.OkHttpClient;

public class Demo {
    public static void main(String[] args) {
        String wsUrl = "wss://stream.bybit.com/v5/public/spot";
        String subscribeMessage = "{\"op\":\"subscribe\",\"args\":[\"publicTrade.BTCUSDT\"]}";

        OkHttpClient client = new OkHttpClient();
        ReconnectingWebSocketListener listener = new ReconnectingWebSocketListener(client, wsUrl, subscribeMessage);
        listener.connect();
    }
}
