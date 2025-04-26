package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.stream.BybitDataFlow;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

public final class DataFlows {

    private DataFlows() {
        throw new UnsupportedOperationException();
    }

    public static Flowable<String> ofBybit() {
//        return Flowable.create(e -> OldBybitDataFlow.create().subscribe(e), BackpressureStrategy.BUFFER);
        return Flowable.create(e -> BybitDataFlow.create().subscribe(e), BackpressureStrategy.BUFFER);
    }
}
