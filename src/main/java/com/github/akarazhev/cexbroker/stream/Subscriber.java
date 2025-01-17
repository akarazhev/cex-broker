package com.github.akarazhev.cexbroker.stream;

import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

import java.util.Map;

public interface Subscriber {

    Consumer<Map<String, Object>> onNext();

    Consumer<Throwable> onError();

    Action onComplete();
}
