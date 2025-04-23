package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.BybitConstants;
import com.github.akarazhev.cexbroker.stream.Filter;
import io.reactivex.rxjava3.functions.Predicate;

import java.util.Map;

public final class BybitFilter implements Filter {

    private BybitFilter() {
    }

    public static Predicate<Map<String, Object>> ofFilter() {
        return new BybitFilter().filter();
    }

    @Override
    public Predicate<Map<String, Object>> filter() {
        return o -> o.containsKey(BybitConstants.TOPIC_FIELD);
    }
}
