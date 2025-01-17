package com.github.akarazhev.cexbroker.bybit.stream;

import com.github.akarazhev.cexbroker.stream.Mapper;
import com.github.akarazhev.cexbroker.util.JsonUtils;
import io.reactivex.rxjava3.functions.Function;

import java.util.Map;

public final class BybitMapper implements Mapper {

    private BybitMapper() {
    }

    public static BybitMapper create() {
        return new BybitMapper();
    }

    @Override
    public Function<String, Map<String, Object>> map() {
        return JsonUtils::jsonToMap;
    }
}
