package com.github.akarazhev.cexbroker.bybit.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Subscription(@JsonProperty String op, @JsonProperty String[] args) {
}
