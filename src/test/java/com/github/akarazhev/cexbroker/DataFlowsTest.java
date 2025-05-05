package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.stream.BybitFilter;
import com.github.akarazhev.cexbroker.bybit.stream.BybitMapper;
import com.github.akarazhev.cexbroker.bybit.stream.BybitSubscriber;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import io.reactivex.rxjava3.disposables.Disposable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPublicSubscribeTopics;
import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPublicTestnetSpot;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class DataFlowsTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFlowsTest.class);
    private Disposable subscription;

    @AfterEach
    void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            LOGGER.info("Test subscription disposed");
        }
    }

    @Test
    void ofBybit_shouldReceiveDataFromWebSocket() {
        final var latch = new CountDownLatch(1);
        final List<Map<String, Object>> receivedData = new ArrayList<>();
        final var hasError = new AtomicBoolean(false);
        final var bybitSubscriber = getBybitSubscriber(latch, receivedData);
        try {
            // Act
            subscription = DataFlows.ofBybit(getPublicTestnetSpot(), getPublicSubscribeTopics())
                    .map(BybitMapper.ofMap())
                    .filter(BybitFilter.ofFilter())
                    .subscribe(
                            bybitSubscriber.onNext(),
                            t -> {
                                LOGGER.error("Error in test subscription", t);
                                hasError.set(true);
                                latch.countDown();
                            },
                            bybitSubscriber.onComplete()
                    );
            // Assert
            assertTrue(latch.await(30, TimeUnit.SECONDS), "Should receive data within timeout period");
            assertFalse(hasError.get(), "Should not encounter errors during subscription");
            assertFalse(receivedData.isEmpty(), "Should receive at least one data item");
            // Verify data structure
            Map<String, Object> firstData = receivedData.getFirst();
            assertTrue(firstData.containsKey("topic"), "Data should contain 'topic' field");
            LOGGER.info("Integration test received valid data: {}", firstData);
        } catch (Exception e) {
            LOGGER.error("Exception during test execution", e);
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void ofBybit_shouldHandleMultipleMessages() {
        final int expectedMessageCount = 3;
        final var latch = new CountDownLatch(expectedMessageCount);
        final List<Map<String, Object>> receivedData = new ArrayList<>();
        final var bybitSubscriber = getBybitSubscriber(latch, receivedData);
        try {
            // Act
            subscription = DataFlows.ofBybit(getPublicTestnetSpot(), getPublicSubscribeTopics())
                    .map(BybitMapper.ofMap())
                    .filter(BybitFilter.ofFilter())
                    .subscribe(
                            bybitSubscriber.onNext(),
                            throwable -> {
                                LOGGER.error("Error in test subscription", throwable);
                                fail("Should not encounter errors: " + throwable.getMessage());
                            },
                            bybitSubscriber.onComplete()
                    );

            // Assert
            assertTrue(latch.await(60, TimeUnit.SECONDS),
                    "Should receive " + expectedMessageCount + " messages within timeout period");
            assertTrue(receivedData.size() >= expectedMessageCount,
                    "Should receive at least " + expectedMessageCount + " messages");
            LOGGER.info("Successfully received {} messages", receivedData.size());
        } catch (Exception e) {
            LOGGER.error("Exception during test execution", e);
            fail("Test failed with exception: " + e.getMessage());
        }
    }

    private BybitSubscriber getBybitSubscriber(final CountDownLatch latch, final List<Map<String, Object>> receivedData) {
        return BybitSubscriber.create(new StreamHandler() {
            @Override
            public void handle(final String topic, final Map<String, Object> data) {
                LOGGER.info("Received topic: {}, message count: {}", topic, receivedData.size() + 1);
                receivedData.add(data);
                latch.countDown();
            }

            @Override
            public void close() {
                LOGGER.info("Test handler closed");
            }
        });
    }
}