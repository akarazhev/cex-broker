package com.github.akarazhev.cexbroker.bybit.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Ping(@JsonProperty String op) {
}
