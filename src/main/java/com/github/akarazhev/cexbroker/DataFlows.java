package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.BybitConfig;
import com.github.akarazhev.cexbroker.bybit.stream.BybitDataFlow;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataFlows {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFlows.class);

    private DataFlows() {
        throw new UnsupportedOperationException();
    }

    public static Flowable<String> ofBybit(final String url, final String[] topics) {
        LOGGER.info(BybitConfig.print());
        return Flowable.create(e -> BybitDataFlow.create(url, topics).subscribe(e), BackpressureStrategy.BUFFER);
    }
}
