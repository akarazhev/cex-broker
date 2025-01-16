package com.github.akarazhev.cexbroker.function;

import com.github.akarazhev.cexbroker.bybit.function.BybitFilter;
import io.reactivex.rxjava3.functions.Predicate;

import java.util.Map;

public final class Filters {

    private Filters() {
    }

    public static Predicate<Map<String, Object>> ofBybit() {
        return BybitFilter.CONTAINS_TOPIC;
    }
}
