/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViesClientBuilderTest {

    @Test
    void acceptsACompleteHighLoadConfiguration() {
        assertDoesNotThrow(() -> {
            try (var ignored = ViesClient.builder()
                    .baseUrl("https://example.test/vies/")
                    .connectTimeout(Duration.ofSeconds(2))
                    .requestTimeout(Duration.ofSeconds(5))
                    .admissionTimeout(Duration.ofMillis(250))
                    .maxConcurrentRequests(16)
                    .maxPendingSyncRequests(256)
                    .maxPendingAsyncRequests(256)
                    .cacheMaxEntries(50_000)
                    .cacheTtl(Duration.ofHours(1))
                    .retries(2)
                    .retryDelay(Duration.ofMillis(100))
                    .build()) {
                // Construction is the behavior under test / Itt maga a létrehozás a teszt.
            }
        });
    }

    @Test
    void rejectsInvalidUrlsAndLimits() {
        var builder = ViesClient.builder();
        var oversizedUserAgent = "x".repeat(257);
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("relative/path"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("ftp://example.test/vies"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("http://example.test/vies"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("https://user@example.test/vies"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("https://example.test/vies?target=other"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("https://example.test/vies#fragment"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("http://127.attacker.example/vies"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.baseUrl("https://example.test:99999/vies"));
        assertDoesNotThrow(() -> {
            try (var ignored = ViesClient.builder().baseUrl("http://[::1]:8080/vies").build()) {
                // IPv6 loopback is a valid local test endpoint / Az IPv6 loopback helyes tesztcím.
            }
        });
        assertThrows(IllegalArgumentException.class,
                () -> builder.userAgent("unsafe\r\nInjected: true"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.userAgent(oversizedUserAgent));
        assertThrows(IllegalArgumentException.class,
                () -> builder.userAgent("x💥y"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.userAgent("x\u0085y"));
        assertThrows(IllegalArgumentException.class,
                () -> builder.maxConcurrentRequests(0));
        assertThrows(IllegalArgumentException.class,
                () -> builder.maxPendingSyncRequests(0));
        assertThrows(IllegalArgumentException.class,
                () -> builder.maxPendingAsyncRequests(0));
        assertThrows(IllegalArgumentException.class,
                () -> builder.maxConcurrentRequests(513));
        assertThrows(IllegalArgumentException.class,
                () -> builder.maxPendingSyncRequests(10_001));
        assertThrows(IllegalArgumentException.class,
                () -> builder.maxPendingAsyncRequests(10_001));
        assertThrows(IllegalArgumentException.class,
                () -> builder.retries(6));
    }

    @Test
    void rejectsNonPositiveDurations() {
        var builder = ViesClient.builder();
        var negativeSecond = Duration.ofSeconds(-1);
        var shortRetryDelay = Duration.ofMillis(99);
        var longRetryDelay = Duration.ofSeconds(31);
        var overflowingSeconds = Duration.ofSeconds(Long.MAX_VALUE);
        var overflowingBackoff = Duration.ofMillis(Long.MAX_VALUE / 24L + 1L);
        assertThrows(IllegalArgumentException.class,
                () -> builder.connectTimeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> builder.requestTimeout(negativeSecond));
        assertThrows(IllegalArgumentException.class,
                () -> builder.admissionTimeout(Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> builder.retryDelay(Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> builder.retryDelay(shortRetryDelay));
        assertThrows(IllegalArgumentException.class,
                () -> builder.retryDelay(longRetryDelay));
        assertThrows(IllegalArgumentException.class,
                () -> builder.cacheTtl(Duration.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> builder.admissionTimeout(overflowingSeconds));
        assertThrows(IllegalArgumentException.class,
                () -> builder.retryDelay(overflowingSeconds));
        assertThrows(IllegalArgumentException.class,
                () -> builder.retryDelay(overflowingBackoff));
        assertThrows(IllegalArgumentException.class,
                () -> builder.cacheMaxEntries(100_001));
    }
}
