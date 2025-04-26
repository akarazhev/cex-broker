package com.github.akarazhev.cexbroker.bybit.stream;

import com.fasterxml.jackson.annotation.JsonProperty;

record Ping(@JsonProperty String op) {
}
