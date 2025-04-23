package com.github.akarazhev.cexbroker.bybit.request;

import com.fasterxml.jackson.annotation.JsonProperty;

record Ping(@JsonProperty String op) {
}
