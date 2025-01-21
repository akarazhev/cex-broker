package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.stream.Config;
import com.github.akarazhev.cexbroker.stream.Filter;
import com.github.akarazhev.cexbroker.stream.Filters;
import com.github.akarazhev.cexbroker.stream.Mapper;
import com.github.akarazhev.cexbroker.stream.Mappers;
import com.github.akarazhev.cexbroker.stream.Observables;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import com.github.akarazhev.cexbroker.stream.StreamProducer;
import com.github.akarazhev.cexbroker.stream.Subscriber;
import com.github.akarazhev.cexbroker.stream.Subscribers;
import io.reactivex.rxjava3.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) {
        final long t = System.currentTimeMillis();
        LOGGER.info("Starting CEX Broker...");
        LOGGER.info(com.github.akarazhev.cexbroker.bybit.config.Config.print());
        LOGGER.info(Config.print());
        final Mapper bybitMapper = Mappers.ofBybit();
        final Filter bybitFilter = Filters.ofBybit();
        final StreamHandler bybitHandler = new StreamProducer();
        final Subscriber bybitSubscriber = Subscribers.ofBybit(bybitHandler);
        final Disposable bybitDisposable = Observables.ofBybit()
                .map(bybitMapper.map())
                .filter(bybitFilter.filter())
                .subscribe(bybitSubscriber.onNext(), bybitSubscriber.onError(), bybitSubscriber.onComplete());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down CEX Broker...");
            bybitDisposable.dispose();
            bybitHandler.close();
        }));
        LOGGER.info("CEX Broker has been started in {} ms", System.currentTimeMillis() - t);
    }
}
