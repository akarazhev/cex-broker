package com.github.akarazhev.cexbroker.function;

import com.github.akarazhev.cexbroker.bybit.function.BybitMapper;
import io.reactivex.rxjava3.functions.Function;

import java.util.Map;

public final class Mappers {

    private Mappers() {
    }

    public static Function<String, Map<String, Object>> ofBybit() {
        return BybitMapper.JSON_TO_MAP;
    }
}
