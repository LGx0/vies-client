/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(30)
class ViesClientHttpTest {

    private HttpServer server;
    private ExecutorService serverExecutor;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        if (serverExecutor != null) {
            serverExecutor.close();
        }
    }

    @Test
    void retriesTransientHttpFailures() throws Exception {
        var requests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            if (requests.incrementAndGet() < 3) {
                send(exchange, 503, "temporarily unavailable");
            } else {
                sendValid(exchange);
            }
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .retries(2)
                .retryDelay(Duration.ofMillis(100))
                .build()) {
            assertInstanceOf(ViesResponse.Valid.class, client.check("DE000000000"));
        }
        assertEquals(3, requests.get());
    }

    @Test
    void neverFollowsRedirectsThatCouldLeakVatOrRequesterData() throws Exception {
        var redirectedRequests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            exchange.getResponseHeaders().set("Location", "/redirect-target");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/redirect-target", exchange -> {
            redirectedRequests.incrementAndGet();
            sendValid(exchange);
        });

        try (var client = ViesClient.builder().baseUrl(baseUrl).disableCache().build()) {
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000000", ViesRequester.of("HU00000000")));
            assertEquals("HTTP_302", unavailable.errorCode());
        }
        assertEquals(0, redirectedRequests.get());
    }

    @Test
    void rejectsOversizedResponsesWithoutUnboundedHeapGrowth() throws Exception {
        var baseUrl = startServer(exchange -> {
            if (exchange.getRequestURI().getPath().endsWith("000000000")) {
                send(exchange, 200, "x".repeat(65_537));
                return;
            }
            var invalidUtf8 = new byte[] {(byte) 0xc3, 0x28};
            exchange.sendResponseHeaders(200, invalidUtf8.length);
            try (var output = exchange.getResponseBody()) {
                output.write(invalidUtf8);
            }
        });

        try (var client = ViesClient.builder().baseUrl(baseUrl).disableCache().build()) {
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000000"));
            assertEquals("MALFORMED_RESPONSE", unavailable.errorCode());
            var invalidUtf8 = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000001"));
            assertEquals("MALFORMED_RESPONSE", invalidUtf8.errorCode());
        }
    }

    @Test
    void coalescesConcurrentRequestsForTheSameVatNumber() throws Exception {
        var requests = new AtomicInteger();
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder().baseUrl(baseUrl).disableCache().build()) {
            var futures = new ArrayList<java.util.concurrent.CompletableFuture<ViesResponse>>();
            for (var i = 0; i < 200; i++) {
                futures.add(client.checkAsync("DE000000000"));
            }
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            releaseResponse.countDown();
            for (var future : futures) {
                assertInstanceOf(ViesResponse.Valid.class, future.get(2, TimeUnit.SECONDS));
            }
        }
        assertEquals(1, requests.get());
    }

    @Test
    void boundsNetworkConcurrencyWithoutBlockingPlatformThreads() throws Exception {
        var active = new AtomicInteger();
        var maximum = new AtomicInteger();
        var fourRequestsStarted = new CountDownLatch(4);
        var releaseResponses = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            var current = active.incrementAndGet();
            maximum.accumulateAndGet(current, Math::max);
            fourRequestsStarted.countDown();
            try {
                releaseResponses.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            } finally {
                active.decrementAndGet();
            }
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxConcurrentRequests(4)
                .build()) {
            var futures = new ArrayList<java.util.concurrent.CompletableFuture<ViesResponse>>();
            for (var i = 0; i < 40; i++) {
                futures.add(client.checkAsync("DE%09d".formatted(i)));
            }
            assertTrue(fourRequestsStarted.await(2, TimeUnit.SECONDS));
            assertEquals(4, maximum.get());
            releaseResponses.countDown();
            for (var future : futures) {
                assertInstanceOf(ViesResponse.Valid.class, future.get(5, TimeUnit.SECONDS));
            }
        } finally {
            releaseResponses.countDown();
        }
        assertEquals(4, maximum.get());
    }

    @Test
    void rejectsExcessAsyncWorkWithBoundedMemoryBackpressure() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponses = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponses.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxConcurrentRequests(1)
                .maxPendingAsyncRequests(4)
                .build()) {
            var accepted = new ArrayList<java.util.concurrent.CompletableFuture<ViesResponse>>();
            for (var i = 0; i < 4; i++) {
                accepted.add(client.checkAsync("DE%09d".formatted(i)));
            }
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));

            var rejected = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.checkAsync("DE000000999").get(1, TimeUnit.SECONDS));
            assertEquals("CLIENT_OVERLOADED", rejected.errorCode());

            releaseResponses.countDown();
            for (var future : accepted) {
                assertInstanceOf(ViesResponse.Valid.class, future.get(5, TimeUnit.SECONDS));
            }
        } finally {
            releaseResponses.countDown();
        }
    }

    @Test
    void cancellationDoesNotLeakAsyncCapacity() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var responseSent = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
                responseSent.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        var executor = new TaskFinishedExecutor();
        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxPendingAsyncRequests(1)
                .executor(executor)
                .build()) {
            var cancelled = client.checkAsync("DE000000001");
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            assertTrue(cancelled.cancel(false));

            var whileRunning = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.checkAsync("DE000000002").get(1, TimeUnit.SECONDS));
            assertEquals("CLIENT_OVERLOADED", whileRunning.errorCode());

            releaseResponse.countDown();
            assertTrue(responseSent.await(2, TimeUnit.SECONDS));
            assertTrue(executor.firstTaskFinished.await(2, TimeUnit.SECONDS));
            assertInstanceOf(ViesResponse.Valid.class,
                    client.checkAsync("DE000000003").get(2, TimeUnit.SECONDS));
        } finally {
            releaseResponse.countDown();
            executor.close();
        }
    }

    @Test
    void closeFromAsyncCallbackDoesNotDeadlock() throws Exception {
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });
        var client = ViesClient.builder().baseUrl(baseUrl).disableCache().build();

        var completed = client.checkAsync("DE000000000")
                .thenApply(response -> {
                    client.close();
                    return response;
                });
        releaseResponse.countDown();

        assertInstanceOf(ViesResponse.Valid.class, completed.get(2, TimeUnit.SECONDS));
    }

    @Test
    void networkAdmissionWaitHasAnEndToEndBound() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxConcurrentRequests(1)
                .admissionTimeout(Duration.ofMillis(50))
                .build()) {
            var first = client.checkAsync("DE000000001");
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));

            var start = System.nanoTime();
            var overloaded = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000002"));
            var elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertEquals("CLIENT_OVERLOADED", overloaded.errorCode());
            assertTrue(elapsedMillis < 500, "admission waited too long: " + elapsedMillis + " ms");
            releaseResponse.countDown();
            assertInstanceOf(ViesResponse.Valid.class, first.get(2, TimeUnit.SECONDS));
        } finally {
            releaseResponse.countDown();
        }
    }

    @Test
    void sharedCacheFailureDoesNotCauseAViesStampede() throws Exception {
        var requests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });
        var failingCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                throw new IllegalStateException("Redis unavailable");
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only poisoned cache for this test.
                throw new IllegalStateException("Redis unavailable");
            }
        };

        try (var client = ViesClient.builder().baseUrl(baseUrl).cache(failingCache).build()) {
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000000"));
            assertEquals("CACHE_ERROR", unavailable.errorCode());
        }

        var observedCacheKey = new AtomicReference<String>();
        var poisonedCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                observedCacheKey.set(key);
                return java.util.Optional.of(new ViesResponse.Valid("DE", "DE000000000",
                        java.util.Optional.empty(), java.util.Optional.of("x".repeat(4_097)),
                        java.time.Instant.parse("2026-07-17T17:00:00Z"),
                        java.util.Optional.empty(), true));
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only blocking cache for this test.
            }
        };
        try (var client = ViesClient.builder().baseUrl(baseUrl).cache(poisonedCache).build()) {
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000000"));
            assertEquals("CACHE_ERROR", unavailable.errorCode());
        }
        assertTrue(observedCacheKey.get().startsWith("vies:v1:"));
        assertFalse(observedCacheKey.get().contains("DE000000000"));
        assertEquals(0, requests.get());
    }

    @Test
    void inlineCustomExecutorCannotHoldTheLifecycleLockDuringIo() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });
        var inlineExecutor = new InlineExecutor();
        var caller = Executors.newSingleThreadExecutor();
        var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .executor(inlineExecutor)
                .requestTimeout(Duration.ofSeconds(10))
                .build();
        try {
            var submitted = caller.submit(() -> client.checkAsync("DE000000000"));
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));

            var start = System.nanoTime();
            client.close();
            var closeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertTrue(closeMillis < 500, "close blocked for " + closeMillis + " ms");
            var result = submitted.get(2, TimeUnit.SECONDS).get(2, TimeUnit.SECONDS);
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, result);
            assertEquals("CLIENT_CLOSED", unavailable.errorCode());
        } finally {
            releaseResponse.countDown();
            client.close();
            caller.close();
            inlineExecutor.close();
        }
    }

    @Test
    void chainedAsyncCallsSeeReleasedCapacityAndFreshSingleFlightBoundary() throws Exception {
        var requests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxPendingAsyncRequests(1)
                .build()) {
            var result = client.checkAsync("DE000000000")
                    .thenCompose(first -> client.checkAsync("DE000000000"))
                    .get(2, TimeUnit.SECONDS);
            assertInstanceOf(ViesResponse.Valid.class, result);
        }
        assertEquals(2, requests.get());
    }

    @Test
    void chainedDistinctAsyncCallsSeeReleasedPendingPermit() throws Exception {
        var requests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxPendingAsyncRequests(1)
                .build()) {
            var second = client.checkAsync("DE000000001")
                    .thenCompose(first -> client.checkAsync("DE000000002"))
                    .get(2, TimeUnit.SECONDS);
            assertInstanceOf(ViesResponse.Valid.class, second);
        }
        assertEquals(2, requests.get());
    }

    @Test
    void closeRacingWithBlockingCacheReturnsClientClosedWithoutHttpCall() throws Exception {
        var requests = new AtomicInteger();
        var cacheEntered = new CountDownLatch(1);
        var releaseCache = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });
        var blockingCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                cacheEntered.countDown();
                var interrupted = false;
                while (true) {
                    try {
                        releaseCache.await();
                        break;
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                return java.util.Optional.empty();
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only empty cache for this test.
            }
        };
        var client = ViesClient.builder().baseUrl(baseUrl).cache(blockingCache).build();
        var result = client.checkAsync("DE000000000");
        assertTrue(cacheEntered.await(2, TimeUnit.SECONDS));

        client.close();
        releaseCache.countDown();

        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                result.get(2, TimeUnit.SECONDS));
        assertEquals("CLIENT_CLOSED", unavailable.errorCode());
        assertEquals(0, requests.get());
    }

    @Test
    void synchronousCloseRaceAlsoReturnsClientClosedWithoutHttpCall() throws Exception {
        var requests = new AtomicInteger();
        var cacheEntered = new CountDownLatch(1);
        var releaseCache = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });
        var blockingCache = blockingCache(cacheEntered, releaseCache);
        var client = ViesClient.builder().baseUrl(baseUrl).cache(blockingCache).build();

        try (var caller = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = caller.submit(() -> client.check("DE000000000"));
            assertTrue(cacheEntered.await(2, TimeUnit.SECONDS));
            client.close();
            releaseCache.countDown();

            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    result.get(2, TimeUnit.SECONDS));
            assertEquals("CLIENT_CLOSED", unavailable.errorCode());
        } finally {
            releaseCache.countDown();
            client.close();
        }
        assertEquals(0, requests.get());
    }

    @Test
    void closeInterruptsCustomExecutorWorkButDoesNotOwnTheExecutor() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });
        var externalExecutor = Executors.newVirtualThreadPerTaskExecutor();
        var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .executor(externalExecutor)
                .build();
        try {
            var result = client.checkAsync("DE000000000");
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));

            var start = System.nanoTime();
            client.close();
            var closeMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            assertTrue(closeMillis < 500, "close blocked for " + closeMillis + " ms");
            assertFalse(externalExecutor.isShutdown(), "caller-owned executor was shut down");
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    result.get(2, TimeUnit.SECONDS));
            assertEquals("CLIENT_CLOSED", unavailable.errorCode());
        } finally {
            releaseResponse.countDown();
            externalExecutor.close();
        }
    }

    @Test
    void closeCancelsAQueuedCustomExecutorTaskBeforeItCanRun() throws Exception {
        var requests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });
        var executor = new QueueingExecutor();
        var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .executor(executor)
                .build();
        try {
            var result = client.checkAsync("DE000000000");
            assertTrue(executor.taskQueued.await(1, TimeUnit.SECONDS));

            client.close();

            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    result.get(2, TimeUnit.SECONDS));
            assertEquals("CLIENT_CLOSED", unavailable.errorCode());
            var queued = assertInstanceOf(Future.class, executor.queuedTask.get());
            assertTrue(queued.isCancelled());
            executor.runQueuedTask();
            assertEquals(0, requests.get());
        } finally {
            client.close();
            executor.close();
        }
    }

    @Test
    void blockingUserCallbackCannotBlockCloseOrTheLifecycleLock() throws Exception {
        var cacheEntered = new CountDownLatch(1);
        var releaseCache = new CountDownLatch(1);
        var callbackEntered = new CountDownLatch(1);
        var releaseCallback = new CountDownLatch(1);
        var callbackUsedVirtualThread = new AtomicBoolean();
        var client = ViesClient.builder()
                .baseUrl("http://127.0.0.1:1/rest")
                .cache(blockingCache(cacheEntered, releaseCache))
                .build();
        try (var closer = Executors.newVirtualThreadPerTaskExecutor()) {
            var callback = client.checkAsync("DE000000000").thenRun(() -> {
                // EN/HU: shutdown callbacks must not allocate one native thread per operation.
                callbackUsedVirtualThread.set(Thread.currentThread().isVirtual());
                callbackEntered.countDown();
                awaitIgnoringInterrupt(releaseCallback);
            });
            assertTrue(cacheEntered.await(2, TimeUnit.SECONDS));

            var closeCall = closer.submit(client::close);
            assertTrue(callbackEntered.await(2, TimeUnit.SECONDS));
            closeCall.get(500, TimeUnit.MILLISECONDS);

            releaseCallback.countDown();
            callback.get(2, TimeUnit.SECONDS);
            assertTrue(callbackUsedVirtualThread.get(),
                    "shutdown completion must use a virtual thread");
        } finally {
            releaseCache.countDown();
            releaseCallback.countDown();
            client.close();
        }
    }

    @Test
    void synchronousLeaderAndFollowerShareClientClosedOutcome() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var cacheReads = new AtomicInteger();
        var followerJoined = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });
        var emptyCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                // Leader reads twice; the third read proves the follower entered checkNormalized.
                if (cacheReads.incrementAndGet() >= 3) {
                    followerJoined.countDown();
                }
                return java.util.Optional.empty();
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only failing cache for this test.
            }
        };
        var client = ViesClient.builder().baseUrl(baseUrl).cache(emptyCache).build();
        try (var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            var leader = callers.submit(() -> client.check("DE000000000"));
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            var follower = callers.submit(() -> client.check("DE000000000"));
            assertTrue(followerJoined.await(2, TimeUnit.SECONDS));

            client.close();
            releaseResponse.countDown();

            for (var result : List.of(leader, follower)) {
                var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                        result.get(2, TimeUnit.SECONDS));
                assertEquals("CLIENT_CLOSED", unavailable.errorCode());
            }
        } finally {
            releaseResponse.countDown();
        }
    }

    @Test
    void synchronousLeaderAndFollowerStayIdenticalWhenCloseRacesWithCacheWrite() throws Exception {
        var cacheReads = new AtomicInteger();
        var cacheWriteEntered = new CountDownLatch(1);
        var followerReachedCache = new CountDownLatch(1);
        var releaseCacheWrite = new CountDownLatch(1);
        var baseUrl = startServer(ViesClientHttpTest::sendValid);
        var blockingWriteCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                // EN: the leader reads twice; the third read belongs to the follower.
                // HU: a vezető kétszer olvas; a harmadik olvasás már a követőé.
                if (cacheReads.incrementAndGet() >= 3) {
                    followerReachedCache.countDown();
                }
                return java.util.Optional.empty();
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only failure-injection cache for this test.
                cacheWriteEntered.countDown();
                awaitIgnoringInterrupt(releaseCacheWrite);
            }
        };
        var client = ViesClient.builder().baseUrl(baseUrl).cache(blockingWriteCache).build();
        try (var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            var leader = callers.submit(() -> client.check("DE000000000"));
            assertTrue(cacheWriteEntered.await(2, TimeUnit.SECONDS));
            var follower = callers.submit(() -> client.check("DE000000000"));
            assertTrue(followerReachedCache.await(2, TimeUnit.SECONDS));

            client.close();
            releaseCacheWrite.countDown();

            for (var result : List.of(leader, follower)) {
                var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                        result.get(2, TimeUnit.SECONDS));
                assertEquals("CLIENT_CLOSED", unavailable.errorCode());
            }
        } finally {
            releaseCacheWrite.countDown();
            client.close();
        }
    }

    @Test
    void sameKeyAsyncFollowersDoNotConsumeAdditionalPendingSlots() throws Exception {
        var requests = new AtomicInteger();
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxPendingAsyncRequests(1)
                .build()) {
            var sameKey = new ArrayList<java.util.concurrent.CompletableFuture<ViesResponse>>();
            for (var i = 0; i < 100; i++) {
                sameKey.add(client.checkAsync("DE000000000"));
            }
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            var distinct = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.checkAsync("DE000000002").get(1, TimeUnit.SECONDS));
            assertEquals("CLIENT_OVERLOADED", distinct.errorCode());

            releaseResponse.countDown();
            for (var future : sameKey) {
                assertInstanceOf(ViesResponse.Valid.class, future.get(2, TimeUnit.SECONDS));
            }
        } finally {
            releaseResponse.countDown();
        }
        assertEquals(1, requests.get());
    }

    @Test
    void rejectedCustomExecutorCleansAsyncAdmissionAndSingleFlight() throws Exception {
        var requests = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });
        var executor = new RejectOnceExecutor();
        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxPendingAsyncRequests(1)
                .executor(executor)
                .build()) {
            assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> client.checkAsync("DE000000000").get(1, TimeUnit.SECONDS));
            assertInstanceOf(ViesResponse.Valid.class,
                    client.checkAsync("DE000000000").get(2, TimeUnit.SECONDS));
        } finally {
            executor.close();
        }
        assertEquals(1, requests.get());
    }

    @Test
    void synchronousLeaderAndAsyncFollowerUseOneNetworkRequest() throws Exception {
        var requests = new AtomicInteger();
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder().baseUrl(baseUrl).disableCache().build();
             var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            var syncLeader = callers.submit(() -> client.check("DE000000000"));
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            var asyncFollower = client.checkAsync("DE000000000");
            releaseResponse.countDown();

            assertInstanceOf(ViesResponse.Valid.class, syncLeader.get(2, TimeUnit.SECONDS));
            assertInstanceOf(ViesResponse.Valid.class, asyncFollower.get(2, TimeUnit.SECONDS));
        } finally {
            releaseResponse.countDown();
        }
        assertEquals(1, requests.get());
    }

    @Test
    void asynchronousLeaderAndSynchronousFollowerUseOneNetworkRequest() throws Exception {
        var requests = new AtomicInteger();
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder().baseUrl(baseUrl).disableCache().build();
             var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            var asyncLeader = client.checkAsync("DE000000000");
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            var syncFollower = callers.submit(() -> client.check("DE000000000"));
            releaseResponse.countDown();

            assertInstanceOf(ViesResponse.Valid.class, asyncLeader.get(2, TimeUnit.SECONDS));
            assertInstanceOf(ViesResponse.Valid.class, syncFollower.get(2, TimeUnit.SECONDS));
        } finally {
            releaseResponse.countDown();
        }
        assertEquals(1, requests.get());
    }

    @Test
    void boundsSynchronousCallersWithImmediateBackpressure() throws Exception {
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });

        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .disableCache()
                .maxPendingSyncRequests(1)
                .build();
             var callers = Executors.newVirtualThreadPerTaskExecutor()) {
            var admitted = callers.submit(() -> client.check("DE000000001"));
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            var rejected = assertInstanceOf(ViesResponse.Unavailable.class,
                    client.check("DE000000002"));
            assertEquals("CLIENT_OVERLOADED", rejected.errorCode());
            releaseResponse.countDown();
            assertInstanceOf(ViesResponse.Valid.class, admitted.get(2, TimeUnit.SECONDS));
        } finally {
            releaseResponse.countDown();
        }
    }

    @Test
    void asyncFatalErrorCompletesExceptionallyAndReachesWorkerHandler() throws Exception {
        var fatal = new AssertionError("fatal cache failure");
        var uncaught = new AtomicReference<Throwable>();
        var uncaughtSeen = new CountDownLatch(1);
        var factory = Thread.ofPlatform().uncaughtExceptionHandler((thread, failure) -> {
            uncaught.set(failure);
            uncaughtSeen.countDown();
        }).factory();
        var executor = Executors.newSingleThreadExecutor(factory);
        var failingCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                throw fatal;
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only failure-injection cache for this test.
            }
        };

        try (var client = ViesClient.builder()
                .baseUrl("http://127.0.0.1:1/rest")
                .cache(failingCache)
                .executor(executor)
                .build()) {
            var failure = assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> client.checkAsync("DE000000000").get(2, TimeUnit.SECONDS));
            assertSame(fatal, failure.getCause());
            assertTrue(uncaughtSeen.await(2, TimeUnit.SECONDS));
            assertSame(fatal, uncaught.get());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void inlineFatalErrorReleasesExactlyOneAsyncPermit() throws Exception {
        var fatal = new AssertionError("fatal inline cache failure");
        var failFirstRead = new AtomicBoolean(true);
        var requestStarted = new CountDownLatch(1);
        var releaseResponse = new CountDownLatch(1);
        var baseUrl = startServer(exchange -> {
            requestStarted.countDown();
            try {
                releaseResponse.await();
                sendValid(exchange);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                exchange.close();
            }
        });
        var cache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                if (failFirstRead.getAndSet(false)) {
                    throw fatal;
                }
                return java.util.Optional.empty();
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only scripted cache for this test.
            }
        };
        var executor = new InlineThenAsyncExecutor();
        try (var client = ViesClient.builder()
                .baseUrl(baseUrl)
                .cache(cache)
                .executor(executor)
                .maxPendingAsyncRequests(1)
                .build()) {
            assertSame(fatal, assertThrows(AssertionError.class,
                    () -> client.checkAsync("DE000000001")));

            var accepted = client.checkAsync("DE000000002");
            assertTrue(requestStarted.await(2, TimeUnit.SECONDS));
            var rejected = client.checkAsync("DE000000003");
            assertTrue(rejected.isDone(), "a leaked permit accepted a second operation");
            var unavailable = assertInstanceOf(ViesResponse.Unavailable.class,
                    rejected.get(1, TimeUnit.SECONDS));
            assertEquals("CLIENT_OVERLOADED", unavailable.errorCode());

            releaseResponse.countDown();
            assertInstanceOf(ViesResponse.Valid.class, accepted.get(2, TimeUnit.SECONDS));
        } finally {
            releaseResponse.countDown();
            executor.close();
        }
    }

    @Test
    void synchronousFatalErrorIsNotSwallowedWhenCloseWinsPublication() throws Exception {
        var fatal = new AssertionError("fatal sync cache failure");
        var reads = new AtomicInteger();
        var secondReadEntered = new CountDownLatch(1);
        var releaseSecondRead = new CountDownLatch(1);
        var cache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                if (reads.incrementAndGet() == 1) {
                    return java.util.Optional.empty();
                }
                secondReadEntered.countDown();
                awaitIgnoringInterrupt(releaseSecondRead);
                throw fatal;
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Deliberately read-only blocking cache for this test.
            }
        };
        var caller = Executors.newSingleThreadExecutor();
        var client = ViesClient.builder()
                .baseUrl("http://127.0.0.1:1/rest")
                .cache(cache)
                .build();
        try {
            var result = caller.submit(() -> client.check("DE000000000"));
            assertTrue(secondReadEntered.await(2, TimeUnit.SECONDS));
            client.close();
            releaseSecondRead.countDown();

            var failure = assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> result.get(2, TimeUnit.SECONDS));
            assertSame(fatal, failure.getCause());
        } finally {
            releaseSecondRead.countDown();
            client.close();
            caller.close();
        }
    }

    @Test
    void secondCacheCheckPreventsAStaleMissFromCallingVies() throws Exception {
        var requests = new AtomicInteger();
        var cacheReads = new AtomicInteger();
        var baseUrl = startServer(exchange -> {
            requests.incrementAndGet();
            sendValid(exchange);
        });
        var cachedValue = new ViesResponse.Valid("DE", "DE000000000",
                java.util.Optional.empty(), java.util.Optional.empty(),
                java.time.Instant.parse("2026-07-17T17:00:00Z"),
                java.util.Optional.empty(), true);
        var scriptedCache = new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                return cacheReads.incrementAndGet() == 1
                        ? java.util.Optional.empty()
                        : java.util.Optional.of(cachedValue);
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // This scripted cache exposes the value on its second read; writes are irrelevant.
            }
        };

        try (var client = ViesClient.builder().baseUrl(baseUrl).cache(scriptedCache).build()) {
            var valid = assertInstanceOf(ViesResponse.Valid.class, client.check("DE000000000"));
            assertTrue(valid.fromCache());
        }
        assertEquals(2, cacheReads.get());
        assertEquals(0, requests.get());
    }

    private String startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/rest/ms/DE/vat", handler);
        serverExecutor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(serverExecutor);
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/rest";
    }

    private static void sendValid(HttpExchange exchange) throws IOException {
        send(exchange, 200, """
                {"isValid":true,"userError":"VALID","requestDate":"2026-07-17T17:00:00Z"}
                """);
    }

    private static void send(HttpExchange exchange, int status, String value) throws IOException {
        var body = value.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (var output = exchange.getResponseBody()) {
            output.write(body);
        }
    }

    private static ViesCache blockingCache(CountDownLatch entered, CountDownLatch release) {
        return new ViesCache() {
            @Override
            public java.util.Optional<ViesResponse.Valid> get(String key) {
                entered.countDown();
                var interrupted = false;
                while (true) {
                    try {
                        release.await();
                        break;
                    } catch (InterruptedException e) {
                        interrupted = true;
                    }
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                return java.util.Optional.empty();
            }

            @Override
            public void put(String key, ViesResponse.Valid value, Duration ttl) {
                // Read-only test double: the fixture exercises interruption during cache lookup.
            }
        };
    }

    private static void awaitIgnoringInterrupt(CountDownLatch latch) {
        var interrupted = false;
        while (true) {
            try {
                latch.await();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class RejectOnceExecutor extends AbstractExecutorService {
        private final ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();
        private final AtomicBoolean reject = new AtomicBoolean(true);

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            if (reject.getAndSet(false)) {
                throw new RejectedExecutionException("intentional first rejection");
            }
            delegate.execute(command);
        }
    }

    /** Inline executor used to expose lifecycle-lock reentrancy / Helyben futó lock-race tesztexecutor. */
    private static final class InlineExecutor extends AbstractExecutorService {
        private final AtomicBoolean shutdown = new AtomicBoolean();

        @Override
        public void shutdown() {
            shutdown.set(true);
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown.set(true);
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return shutdown.get();
        }

        @Override
        public boolean isTerminated() {
            return shutdown.get();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown.get();
        }

        @Override
        public void execute(Runnable command) {
            if (shutdown.get()) {
                throw new RejectedExecutionException("executor is shut down");
            }
            command.run();
        }
    }

    /** First task inline, later tasks async / Első task helyben, a továbbiak aszinkron futnak. */
    private static final class InlineThenAsyncExecutor extends AbstractExecutorService {
        private final AtomicBoolean first = new AtomicBoolean(true);
        private final ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            if (first.getAndSet(false)) {
                command.run();
            } else {
                delegate.execute(command);
            }
        }
    }

    /**
     * Executor exposing a deterministic completion signal for cancellation cleanup.
     * Magyarul: latch jelzi, hogy az első worker ténylegesen befejezte a takarítást.
     */
    private static final class TaskFinishedExecutor extends AbstractExecutorService {
        private final ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();
        private final CountDownLatch firstTaskFinished = new CountDownLatch(1);

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(() -> {
                try {
                    command.run();
                } finally {
                    firstTaskFinished.countDown();
                }
            });
        }
    }

    /**
     * Executor that retains a task without running it, used to verify close-time cancellation.
     * Magyarul: futtatás nélkül sorban tartja a feladatot a close-cancel teszthez.
     */
    private static final class QueueingExecutor extends AbstractExecutorService {
        private final AtomicBoolean shutdown = new AtomicBoolean();
        private final AtomicReference<Runnable> queuedTask = new AtomicReference<>();
        private final CountDownLatch taskQueued = new CountDownLatch(1);

        @Override
        public void shutdown() {
            shutdown.set(true);
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown.set(true);
            var task = queuedTask.getAndSet(null);
            return task == null ? List.of() : List.of(task);
        }

        @Override
        public boolean isShutdown() {
            return shutdown.get();
        }

        @Override
        public boolean isTerminated() {
            return shutdown.get();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown.get();
        }

        @Override
        public void execute(Runnable command) {
            if (shutdown.get()) {
                throw new RejectedExecutionException("executor is shut down");
            }
            queuedTask.set(command);
            taskQueued.countDown();
        }

        void runQueuedTask() {
            var task = queuedTask.getAndSet(null);
            if (task != null) {
                task.run();
            }
        }
    }
}
