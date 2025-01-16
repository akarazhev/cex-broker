package com.github.akarazhev.cexbroker.bybit.function;

import com.github.akarazhev.cexbroker.util.JsonUtils;
import io.reactivex.rxjava3.functions.Function;

import java.util.Map;

public final class BybitMapper {

    private BybitMapper() {
    }

    public static final Function<String, Map<String, Object>> JSON_TO_MAP = JsonUtils::jsonToMap;
}
