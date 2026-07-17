/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client.internal;

import vies.client.ViesCache;
import vies.client.ViesResponse;

import java.time.Duration;
import java.time.InstantSource;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bounded in-memory TTL cache — the default {@link ViesCache}. Expired entries are
 * dropped lazily on read. When full, a bounded sample is checked for expired
 * entries before arbitrary eviction (no global scan or LRU lock), keeping hot-path
 * latency predictable under high write concurrency.
 *
 * <p>Magyarul: ez egy gyors, közelítő cache, nem pontos LRU. Lejáratkor lustán
 * töröl, telítettségnél pedig rövid mintából választ, ezért nincs globális lock
 * vagy minden beszúrásnál teljes cache-bejárás.</p>
 *
 * <p>Internal API: not exported from the module.</p>
 */
public final class TtlCache implements ViesCache {

    private record Entry(ViesResponse.Valid value, long expiresAtMillis) {
    }

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final AtomicInteger entryCount = new AtomicInteger();
    private final int maxEntries;
    private final InstantSource clock;

    public TtlCache(int maxEntries, InstantSource clock) {
        if (maxEntries < 1) {
            throw new IllegalArgumentException(
                    "maxEntries must be positive / A maxEntries értékének pozitívnak kell lennie");
        }
        this.maxEntries = maxEntries;
        this.clock = clock;
    }

    @Override
    public Optional<ViesResponse.Valid> get(String key) {
        var entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (clock.millis() >= entry.expiresAtMillis()) {
            if (entries.remove(key, entry)) {
                entryCount.decrementAndGet();
            }
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public void put(String key, ViesResponse.Valid value, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        long ttlMillis;
        try {
            ttlMillis = Math.max(1L, ttl.toMillis());
        } catch (ArithmeticException tooLarge) {
            ttlMillis = Long.MAX_VALUE;
        }
        var now = clock.millis();
        var expiresAt = now > Long.MAX_VALUE - ttlMillis ? Long.MAX_VALUE : now + ttlMillis;
        var previous = entries.put(key, new Entry(value, expiresAt));
        if (previous == null && entryCount.incrementAndGet() > maxEntries) {
            evictToLimit();
        }
    }

    public int size() {
        return entryCount.get();
    }

    private void evictToLimit() {
        var iterator = entries.entrySet().iterator();
        var now = clock.millis();
        var sampled = 0;
        while (entryCount.get() > maxEntries && iterator.hasNext() && sampled++ < 16) {
            var entry = iterator.next();
            if (now >= entry.getValue().expiresAtMillis()
                    && entries.remove(entry.getKey(), entry.getValue())) {
                entryCount.decrementAndGet();
            }
        }
        while (entryCount.get() > maxEntries && iterator.hasNext()) {
            var entry = iterator.next();
            if (entries.remove(entry.getKey()) != null) {
                entryCount.decrementAndGet();
            }
        }
        // Concurrent iterators are weakly consistent; retry with a fresh iterator if needed.
        while (entryCount.get() > maxEntries) {
            var retry = entries.entrySet().iterator();
            if (!retry.hasNext()) {
                return;
            }
            var entry = retry.next();
            if (entries.remove(entry.getKey()) != null) {
                entryCount.decrementAndGet();
            }
        }
    }
}
