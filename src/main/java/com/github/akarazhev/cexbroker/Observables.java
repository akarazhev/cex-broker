package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.BybitObservable;
import com.github.akarazhev.cexbroker.bybit.config.Config;
import io.reactivex.rxjava3.core.Observable;

public final class Observables {

    private Observables() {
    }

    public static Observable<String> ofBybit(final Config config) {
        return Observable.create(emitter -> BybitObservable.create(config).subscribe(emitter));
    }
}
