/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import java.time.Duration;
import java.util.Optional;

/**
 * Pluggable cache for confirmed ({@link ViesResponse.Valid}) results.
 *
 * <p>The client ships with a bounded in-memory TTL cache (the default). Provide your
 * own implementation via {@link ViesClient.Builder#cache(ViesCache)} to back it with
 * Redis, Caffeine, or your framework's cache abstraction. Only {@code Valid} results
 * are ever cached — {@code Invalid} and transient failures always go back to VIES.</p>
 *
 * <p>Implementations must be thread-safe.</p>
 *
 * <p>Magyarul: Redis-adapter esetén kötelező a rövid timeout, a szálbiztonság és
 * a saját metrika/riasztás. A kliens cache-olvasási hibánál {@code CACHE_ERROR}
 * eredményt ad, hogy ne okozzon VIES request stampede-et.</p>
 */
public interface ViesCache {

    /** Returns the cached result for {@code key}, or empty when absent or expired. */
    Optional<ViesResponse.Valid> get(String key);

    /** Stores {@code value} under {@code key} for at most {@code ttl}. */
    void put(String key, ViesResponse.Valid value, Duration ttl);
}
