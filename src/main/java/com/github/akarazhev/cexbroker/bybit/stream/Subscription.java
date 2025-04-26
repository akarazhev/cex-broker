package com.github.akarazhev.cexbroker.bybit.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

record Subscription(@JsonProperty String op, @JsonProperty String[] args) {
}
