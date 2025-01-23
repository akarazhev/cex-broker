package com.github.akarazhev.cexbroker.bybit.stream.data;

import java.util.List;
import java.util.Map;

import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.SPOT_TYPE;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.TS;
import static com.github.akarazhev.cexbroker.bybit.stream.data.Constants.Field.DATA;
import static com.github.akarazhev.cexbroker.util.TypeUtils.asLong;

public final class KlineResponse implements Response {
    @Override
    public Map<String, Object> process(final Map<String, Object> response) {
        return new KlineBuilder()
                .setType(SPOT_TYPE)
                .setTs(asLong(response.get(TS)))
                .setData((List<Map<String, Object>>) response.get(DATA))
                .build();
    }
}
