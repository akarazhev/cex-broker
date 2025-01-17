package com.github.akarazhev.cexbroker.stream;

import com.github.akarazhev.cexbroker.bybit.stream.BybitObservable;
import io.reactivex.rxjava3.core.Observable;

public final class Observables {

    private Observables() {
        throw new UnsupportedOperationException();
    }

    public static Observable<String> ofBybit() {
        return Observable.create(emitter -> BybitObservable.create().subscribe(emitter));
    }
}
