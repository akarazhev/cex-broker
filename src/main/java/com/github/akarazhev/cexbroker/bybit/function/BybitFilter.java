package com.github.akarazhev.cexbroker.bybit.function;

import com.github.akarazhev.cexbroker.bybit.Constants;
import io.reactivex.rxjava3.functions.Predicate;

import java.util.Map;

public final class BybitFilter {

    private BybitFilter() {
    }

    public static final Predicate<Map<String, Object>> CONTAINS_TOPIC = o ->
            o.containsKey(Constants.Filters.TOPIC);
}
