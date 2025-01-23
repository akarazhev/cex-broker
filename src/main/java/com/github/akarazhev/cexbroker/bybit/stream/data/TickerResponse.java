package com.github.akarazhev.cexbroker.bybit.stream.data;

import java.util.Map;

import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.CS;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.SPOT_TYPE;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.TS;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.DATA;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asInteger;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asLong;

public final class TickerResponse implements Response {
    @Override
    public Map<String, Object> process(final Map<String, Object> response) {
        return new TickerBuilder()
                .setType(SPOT_TYPE)
                .setTs(asLong(response.get(TS)))
                .setCs(asInteger(response.get(CS)))
                .setData((Map<String, Object>) response.get(DATA))
                .build();
    }
}
