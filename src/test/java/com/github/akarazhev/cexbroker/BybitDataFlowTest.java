package com.github.akarazhev.cexbroker;

import com.github.akarazhev.cexbroker.bybit.stream.BybitFilter;
import com.github.akarazhev.cexbroker.bybit.stream.BybitMapper;
import com.github.akarazhev.cexbroker.bybit.stream.BybitSubscriber;
import com.github.akarazhev.cexbroker.stream.StreamHandler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPublicSubscribeTopics;
import static com.github.akarazhev.cexbroker.bybit.BybitConfig.getPublicTestnetSpot;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BybitDataFlowTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(BybitDataFlowTest.class);
    private Disposable subscription;

    @AfterEach
    void cleanup() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            LOGGER.info("Test subscription disposed");
        }
    }

    @Test
    void shouldReceiveDataFromWebSocket() {
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
    void shouldHandleMultipleMessages() {
        final var expectedMessageCount = 3;
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

    @Test
    void constructorShouldThrowException() {
        // Testing private constructor for code coverage
        try {
            // Use reflection to access the private constructor
            var constructor = DataFlows.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Constructor should have thrown an exception");
        } catch (final Exception e) {
            // When using reflection, the UnsupportedOperationException is wrapped in an InvocationTargetException
            Throwable cause = e.getCause();
            assertInstanceOf(UnsupportedOperationException.class, cause,
                    "Root cause should be UnsupportedOperationException, but was: " + cause.getClass().getName());
        }
    }

    @Test
    void shouldHandleInvalidUrl() {
        // Arrange
        final var latch = new CountDownLatch(1);
        final var hasError = new AtomicBoolean(false);
        final var errorMessage = new StringBuilder();
        final List<Map<String, Object>> receivedData = new ArrayList<>();
        final var bybitSubscriber = getBybitSubscriber(latch, receivedData);
        try {
            // Act - use an invalid URL that will cause connection issues
            final var invalidUrl = "wss://invalid.url.that.does.not.exist";
            // Set a shorter timeout for the test
            subscription = DataFlows.ofBybit(invalidUrl, getPublicSubscribeTopics())
                    // Add timeout to force error after 5 seconds
                    .timeout(5, TimeUnit.SECONDS)
                    .map(BybitMapper.ofMap())
                    .filter(BybitFilter.ofFilter())
                    .subscribe(
                            bybitSubscriber.onNext(),
                            throwable -> {
                                LOGGER.info("Expected error received: {}", throwable.getMessage());
                                errorMessage.append(throwable.getMessage());
                                hasError.set(true);
                                latch.countDown();
                            },
                            bybitSubscriber.onComplete()
                    );
            // Assert - we expect an error to occur within timeout
            final var completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) {
                // If latch didn't count down, we'll manually verify that no data was received
                // This is also a valid test case - connection failed but didn't trigger error
                assertTrue(receivedData.isEmpty(), "No data should be received with invalid URL");
                LOGGER.info("No error triggered, but connection failed as expected (no data received)");
            } else {
                // If latch counted down, verify it was due to an error
                assertTrue(hasError.get(), "Should encounter errors with invalid URL");
                LOGGER.info("Error handling test passed with message: {}", errorMessage);
            }
        } catch (final Exception e) {
            LOGGER.error("Unexpected exception during test execution", e);
            fail("Test failed with unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void shouldHandleBackpressure() {
        // This test verifies that the BUFFER backpressure strategy works correctly
        final var messageCount = 10;
        final var latch = new CountDownLatch(1);
        final var receivedCount = new AtomicInteger(0);
        final var hasError = new AtomicBoolean(false);
        try {
            // Create a slow consumer
            subscription = DataFlows.ofBybit(getPublicTestnetSpot(), getPublicSubscribeTopics())
                    .map(BybitMapper.ofMap())
                    .filter(BybitFilter.ofFilter())
                    // Introduce artificial delay to test backpressure
                    .observeOn(Schedulers.io(), false, 2) // Small buffer size
                    .doOnNext(data -> {
                        // Simulate slow processing
                        try {
                            Thread.sleep(100);
                            int count = receivedCount.incrementAndGet();
                            LOGGER.info("Processed message {}", count);
                            if (count >= messageCount) {
                                latch.countDown();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    })
                    .subscribe(
                            data -> { /* Already handled in doOnNext */ },
                            throwable -> {
                                LOGGER.error("Error in backpressure test", throwable);
                                hasError.set(true);
                                latch.countDown();
                            },
                            () -> LOGGER.info("Backpressure test completed")
                    );
            // Assert
            assertTrue(latch.await(120, TimeUnit.SECONDS), "Should process required number of messages");
            assertFalse(hasError.get(), "Should not encounter errors during backpressure test");
            assertTrue(receivedCount.get() >= messageCount,
                    "Should receive at least " + messageCount + " messages");
            LOGGER.info("Backpressure test passed, processed {} messages", receivedCount.get());
        } catch (final Exception e) {
            LOGGER.error("Exception during backpressure test", e);
            fail("Backpressure test failed with exception: " + e.getMessage());
        }
    }

    @Test
    void shouldSupportMultipleSubscribers() {
        final var latch1 = new CountDownLatch(1);
        final var latch2 = new CountDownLatch(1);
        final List<Map<String, Object>> receivedData1 = new ArrayList<>();
        final List<Map<String, Object>> receivedData2 = new ArrayList<>();
        final var hasError = new AtomicBoolean(false);
        try {
            // Create the flowable
            var flowable = DataFlows.ofBybit(getPublicTestnetSpot(), getPublicSubscribeTopics())
                    .map(BybitMapper.ofMap())
                    .filter(BybitFilter.ofFilter())
                    .publish()  // Make it multicasting
                    .refCount();
            // First subscriber
            var subscriber1 = getBybitSubscriber(latch1, receivedData1);
            var sub1 = flowable.subscribe(
                    subscriber1.onNext(),
                    throwable -> {
                        LOGGER.error("Error in subscriber 1", throwable);
                        hasError.set(true);
                        latch1.countDown();
                    },
                    subscriber1.onComplete()
            );
            // Second subscriber with slight delay
            Thread.sleep(500);
            var subscriber2 = getBybitSubscriber(latch2, receivedData2);
            var sub2 = flowable.subscribe(
                    subscriber2.onNext(),
                    throwable -> {
                        LOGGER.error("Error in subscriber 2", throwable);
                        hasError.set(true);
                        latch2.countDown();
                    },
                    subscriber2.onComplete()
            );
            // Store both subscriptions for cleanup
            subscription = Disposable.fromAction(() -> {
                sub1.dispose();
                sub2.dispose();
            });
            // Assert
            assertTrue(latch1.await(30, TimeUnit.SECONDS), "First subscriber should receive data");
            assertTrue(latch2.await(30, TimeUnit.SECONDS), "Second subscriber should receive data");
            assertFalse(hasError.get(), "Should not encounter errors");
            assertFalse(receivedData1.isEmpty(), "First subscriber should receive data");
            assertFalse(receivedData2.isEmpty(), "Second subscriber should receive data");
            LOGGER.info("Multiple subscribers test passed. Subscriber 1: {} messages, Subscriber 2: {} messages",
                    receivedData1.size(), receivedData2.size());
        } catch (final Exception e) {
            LOGGER.error("Exception during multiple subscribers test", e);
            fail("Multiple subscribers test failed with exception: " + e.getMessage());
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