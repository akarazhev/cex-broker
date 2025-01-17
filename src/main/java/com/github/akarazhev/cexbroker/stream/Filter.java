package com.github.akarazhev.cexbroker.stream;

import io.reactivex.rxjava3.functions.Predicate;

import java.util.Map;

public interface Filter {

    Predicate<Map<String, Object>> filter();
}
