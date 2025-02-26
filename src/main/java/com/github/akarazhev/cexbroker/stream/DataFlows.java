package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.bybit.stream.BybitDataFlow;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;

public final class DataFlows {

    private DataFlows() {
        throw new UnsupportedOperationException();
    }

    public static Flowable<String> ofBybit() {
        // Test it: LATEST vs BUFFER?
        return Flowable.create(e -> BybitDataFlow.create().subscribe(e), BackpressureStrategy.LATEST);
    }
}
