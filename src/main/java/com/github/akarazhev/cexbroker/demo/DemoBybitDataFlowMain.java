package com.github.akarazhev.cexbroker.demo;

import io.reactivex.rxjava3.core.Flowable;

public class DemoBybitDataFlowMain {
    public static void main(String[] args) {
        String wsUrl = "wss://stream.bybit.com/v5/public/spot";
        String subscribeMessage = "{\"op\":\"subscribe\",\"args\":[\"publicTrade.BTCUSDT\"]}";

        Flowable<String> flowable = DemoBybitDataFlow.create(wsUrl, subscribeMessage);
        flowable.subscribe(
                msg -> System.out.println("Bybit message: " + msg),
                error -> error.printStackTrace(),
                () -> System.out.println("Stream completed")
        );
    }
}
