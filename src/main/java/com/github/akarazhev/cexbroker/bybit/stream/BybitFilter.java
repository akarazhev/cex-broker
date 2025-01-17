package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.bybit.Constants;
import com.github.akarazhev.cexbroker.stream.Filter;
import io.reactivex.rxjava3.functions.Predicate;

import java.util.Map;

public final class BybitFilter implements Filter {

    private BybitFilter() {
    }

    public static BybitFilter create() {
        return new BybitFilter();
    }

    @Override
    public Predicate<Map<String, Object>> filter() {
        return o -> o.containsKey(Constants.Filters.TOPIC);
    }
}
