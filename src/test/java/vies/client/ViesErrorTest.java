/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViesErrorTest {

    @Test
    void providesBilingualRetryableNetworkError() {
        var error = ViesError.of("NETWORK_ERROR");

        assertEquals("NETWORK_ERROR", error.code());
        assertFalse(error.messageHu().isBlank());
        assertFalse(error.messageEn().isBlank());
        assertTrue(error.retryable());
    }

    @Test
    void distinguishesPermanentInputErrors() {
        var malformed = new ViesResponse.MalformedInput("HU123", "bad format");
        var error = malformed.error().orElseThrow();

        assertEquals("INVALID_VAT_FORMAT", error.code());
        assertFalse(error.retryable());
    }

    @Test
    void classifiesHttpStatusCodesForExternalRetry() {
        assertTrue(ViesError.of("HTTP_429").retryable());
        assertTrue(ViesError.of("HTTP_503").retryable());
        assertFalse(ViesError.of("HTTP_400").retryable());
    }

    @Test
    void validAndInvalidDecisionsHaveNoError() {
        assertTrue(new ViesResponse.Invalid("HU", "HU00000000", java.time.Instant.EPOCH)
                .error().isEmpty());
    }

    @Test
    void everyPublicErrorCodeHasBothLanguagesAndStableRetryability() {
        var expected = Map.ofEntries(
                Map.entry("SERVICE_UNAVAILABLE", true),
                Map.entry("MS_UNAVAILABLE", true),
                Map.entry("TIMEOUT", true),
                Map.entry("NETWORK_ERROR", true),
                Map.entry("MALFORMED_RESPONSE", true),
                Map.entry("CLIENT_OVERLOADED", true),
                Map.entry("CACHE_ERROR", true),
                Map.entry("INTERRUPTED", false),
                Map.entry("CLIENT_CLOSED", false),
                Map.entry("INVALID_VAT_FORMAT", false),
                Map.entry("INVALID_INPUT", false),
                Map.entry("INVALID_REQUESTER_INFO", false),
                Map.entry("IP_BLOCKED", false),
                Map.entry("VAT_BLOCKED", false),
                Map.entry("INTERNAL_ERROR", false));

        expected.forEach((code, retryable) -> {
            var error = ViesError.of(code);
            assertEquals(code, error.code());
            assertEquals(retryable, error.retryable(), code);
            assertFalse(error.messageHu().isBlank(), code);
            assertFalse(error.messageEn().isBlank(), code);
        });
    }

    @Test
    void preservesUnknownMachineCodeWithoutRetryStorm() {
        var error = ViesError.of("NEW_VIES_ERROR");
        assertEquals("NEW_VIES_ERROR", error.code());
        assertFalse(error.retryable());
        assertTrue(error.messageHu().contains("NEW_VIES_ERROR"));
        assertTrue(error.messageEn().contains("NEW_VIES_ERROR"));

        var injected = ViesError.of("X\nFORGED");
        assertEquals("UNKNOWN_ERROR", injected.code());
        assertFalse(injected.messageHu().contains("FORGED"));
        assertFalse(injected.messageEn().contains("FORGED"));
        assertEquals("UNKNOWN_ERROR", ViesError.of("X".repeat(100_000)).code());
    }
}
