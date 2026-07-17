/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client.internal;

import vies.client.ViesResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TtlCacheTest {

    private static ViesResponse.Valid sample(String vat) {
        return new ViesResponse.Valid("HU", vat, Optional.empty(), Optional.empty(),
                Instant.EPOCH, Optional.empty(), false);
    }

    @Test
    void returnsStoredValueUntilExpiry() {
        var now = new Instant[]{Instant.parse("2026-01-01T00:00:00Z")};
        InstantSource clock = () -> now[0];
        var cache = new TtlCache(10, clock);

        cache.put("k", sample("HU00000000"), Duration.ofHours(24));
        assertEquals("HU00000000", cache.get("k").orElseThrow().vatNumber());

        now[0] = now[0].plus(Duration.ofHours(23));
        assertTrue(cache.get("k").isPresent());

        now[0] = now[0].plus(Duration.ofHours(2));
        assertTrue(cache.get("k").isEmpty());
        assertEquals(0, cache.size());
    }

    @Test
    void expiresExactlyAtTheTtlBoundary() {
        var now = new Instant[]{Instant.parse("2026-01-01T00:00:00Z")};
        InstantSource clock = () -> now[0];
        var cache = new TtlCache(10, clock);

        cache.put("k", sample("HU00000000"), Duration.ofHours(1));
        now[0] = now[0].plus(Duration.ofHours(1));

        assertTrue(cache.get("k").isEmpty());
        assertEquals(0, cache.size());
    }

    @Test
    void ignoresNonPositiveTtl() {
        var cache = new TtlCache(10, InstantSource.system());
        cache.put("k", sample("HU00000000"), Duration.ZERO);
        assertTrue(cache.get("k").isEmpty());
    }

    @Test
    void staysWithinConfiguredBound() {
        var cache = new TtlCache(5, InstantSource.system());
        for (var i = 0; i < 50; i++) {
            cache.put("k" + i, sample("HU1234567" + (i % 10)), Duration.ofHours(1));
            assertTrue(cache.size() <= 5, "cache grew past its bound: " + cache.size());
        }
    }

    @Test
    void sweepsExpiredEntriesBeforeEvictingLiveOnes() {
        var now = new Instant[]{Instant.parse("2026-01-01T00:00:00Z")};
        InstantSource clock = () -> now[0];
        var cache = new TtlCache(3, clock);

        cache.put("old1", sample("HU11111111"), Duration.ofMinutes(1));
        cache.put("old2", sample("HU22222222"), Duration.ofMinutes(1));
        cache.put("live", sample("HU33333333"), Duration.ofHours(5));

        now[0] = now[0].plus(Duration.ofMinutes(10));
        cache.put("fresh", sample("HU44444444"), Duration.ofHours(5));

        assertTrue(cache.get("live").isPresent(), "live entry should survive the sweep");
        assertTrue(cache.get("fresh").isPresent());
    }

    @Test
    void remainsBoundedUnderConcurrentWritePressure() throws Exception {
        var cache = new TtlCache(128, InstantSource.system());
        try (var workers = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new java.util.concurrent.Future<?>[32];
            for (var worker = 0; worker < futures.length; worker++) {
                var partition = worker;
                futures[worker] = workers.submit(() -> {
                    for (var i = 0; i < 1_000; i++) {
                        var key = partition + ":" + i;
                        cache.put(key, sample("HU%08d".formatted(i)), Duration.ofHours(1));
                        cache.get(key);
                    }
                });
            }
            for (var future : futures) {
                future.get();
            }
        }
        assertTrue(cache.size() >= 0);
        assertTrue(cache.size() <= 128, "cache grew past its concurrent bound: " + cache.size());
    }
}
