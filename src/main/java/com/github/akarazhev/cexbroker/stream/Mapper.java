package com.github.akarazhev.cexbroker.stream;

import io.reactivex.rxjava3.functions.Function;

import java.util.Map;

public interface Mapper {

    Function<String, Map<String, Object>> map();
}
