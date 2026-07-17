/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import vies.client.internal.MiniJson;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViesResponseMappingTest {

    private static final VatFormat.Normalized VAT = new VatFormat.Normalized("HU", "00000000");

    private static ViesResponse map(String json) {
        return ViesClient.mapBody(VAT, MiniJson.parse(json));
    }

    @Test
    void mapsValidGetStyleResponse() {
        var response = map("""
                {
                  "isValid" : true,
                  "requestDate" : "2026-07-17T13:11:12.131Z",
                  "userError" : "VALID",
                  "name" : "ACME Kft.",
                  "address" : "1051 BUDAPEST",
                  "requestIdentifier" : "WAPIAAAAY123",
                  "vatNumber" : "00000000"
                }
                """);
        var valid = assertInstanceOf(ViesResponse.Valid.class, response);
        assertTrue(valid.isValid());
        assertEquals("HU00000000", valid.vatNumber());
        assertEquals("ACME Kft.", valid.traderName().orElseThrow());
        assertEquals("1051 BUDAPEST", valid.traderAddress().orElseThrow());
        assertEquals("WAPIAAAAY123", valid.consultationNumber().orElseThrow());
        assertEquals(Instant.parse("2026-07-17T13:11:12.131Z"), valid.requestDate());
        assertFalse(valid.fromCache());
    }

    @Test
    void mapsValidPostStyleResponseAndPlaceholders() {
        var response = map("""
                {"valid": true, "requestDate": "2026-07-17T13:11:12.131Z",
                 "name": "---", "address": "---", "requestIdentifier": ""}
                """);
        var valid = assertInstanceOf(ViesResponse.Valid.class, response);
        assertTrue(valid.traderName().isEmpty());
        assertTrue(valid.traderAddress().isEmpty());
        assertTrue(valid.consultationNumber().isEmpty());
    }

    @Test
    void mapsInvalidResponse() {
        var response = map("""
                {"isValid": false, "userError": "INVALID", "requestDate": "2026-07-17T13:11:12.131Z"}
                """);
        var invalid = assertInstanceOf(ViesResponse.Invalid.class, response);
        assertEquals("HU00000000", invalid.vatNumber());
    }

    @Test
    // Named, separate security contracts are intentionally clearer than one branch-heavy data-driven test.
    @SuppressWarnings("java:S5976")
    void mapsTransientErrorToUnavailable() {
        var response = map("""
                {"isValid": false, "userError": "MS_UNAVAILABLE"}
                """);
        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, response);
        assertEquals("MS_UNAVAILABLE", unavailable.errorCode());
    }

    @Test
    void mapsInputErrorToMalformedInput() {
        var response = map("""
                {"isValid": false, "userError": "INVALID_INPUT"}
                """);
        var malformed = assertInstanceOf(ViesResponse.MalformedInput.class, response);
        assertTrue(malformed.reason().contains("INVALID_INPUT"));
    }

    @Test
    void mapsUnknownBodyShapeToUnavailable() {
        var response = ViesClient.mapBody(VAT, MiniJson.parse("[1,2,3]"));
        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, response);
        assertEquals("MALFORMED_RESPONSE", unavailable.errorCode());
    }

    @Test
    void neverTreatsMissingValidityAsInvalid() {
        var response = map("""
                {"userError": "INVALID", "requestDate": "2026-07-17T13:11:12.131Z"}
                """);
        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, response);
        assertEquals("MALFORMED_RESPONSE", unavailable.errorCode());
    }

    @Test
    void neverTreatsNonBooleanValidityAsInvalid() {
        var response = map("""
                {"isValid": "false", "userError": "INVALID"}
                """);
        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, response);
        assertEquals("MALFORMED_RESPONSE", unavailable.errorCode());

        var contradictoryError = assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid": true, "userError": "INVALID", "requestDate": "2026-07-17T13:11:12.131Z"}
                """));
        assertEquals("MALFORMED_RESPONSE", contradictoryError.errorCode());

        var contradictoryFields = assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid": true, "valid": false, "requestDate": "2026-07-17T13:11:12.131Z"}
                """));
        assertEquals("MALFORMED_RESPONSE", contradictoryFields.errorCode());

        var injectedError = assertInstanceOf(ViesResponse.Unavailable.class,
                map("{\"userError\":\"MS_UNAVAILABLE\\nforged-log\"}"));
        assertEquals("MALFORMED_RESPONSE", injectedError.errorCode());

        for (var validity : new boolean[] {false, true}) {
            var wrongErrorType = assertInstanceOf(ViesResponse.Unavailable.class, map("""
                    {"isValid":%s,"userError":{"code":"MS_UNAVAILABLE"},
                     "requestDate":"2026-07-17T13:11:12.131Z"}
                    """.formatted(validity)));
            assertEquals("MALFORMED_RESPONSE", wrongErrorType.errorCode());
        }

        var oversizedField = assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid":true,"userError":"VALID","requestDate":"2026-07-17T13:11:12.131Z",
                 "name":"%s"}
                """.formatted("x".repeat(513))));
        assertEquals("MALFORMED_RESPONSE", oversizedField.errorCode());
        assertEquals("MALFORMED_RESPONSE", assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid":true,"userError":"VALID","requestDate":"2026-07-17T13:11:12.131Z",
                 "address":"%s"}
                """.formatted("x".repeat(4_097)))).errorCode());
        assertEquals("MALFORMED_RESPONSE", assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid":true,"userError":"VALID","requestDate":"2026-07-17T13:11:12.131Z",
                 "requestIdentifier":"%s"}
                """.formatted("x".repeat(257)))).errorCode());
        assertEquals("MALFORMED_RESPONSE", assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid":true,"userError":"VALID","requestDate":"%s"}
                """.formatted("2".repeat(65)))).errorCode());

        var mixedVat = assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid":true,"userError":"VALID","requestDate":"2026-07-17T13:11:12.131Z",
                 "countryCode":"HU","vatNumber":"87654321"}
                """));
        assertEquals("MALFORMED_RESPONSE", mixedVat.errorCode());

        var mixedCountry = assertInstanceOf(ViesResponse.Unavailable.class, map("""
                {"isValid":true,"userError":"VALID","requestDate":"2026-07-17T13:11:12.131Z",
                 "countryCode":"DE","vatNumber":"000000000"}
                """));
        assertEquals("MALFORMED_RESPONSE", mixedCountry.errorCode());
    }

    @Test
    void neverInventsAnAuditTimestampWhenRequestDateIsMissing() {
        var response = map("""
                {"isValid": true, "userError": "VALID"}
                """);
        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, response);
        assertEquals("MALFORMED_RESPONSE", unavailable.errorCode());
    }

    @Test
    void rejectsAnInvalidAuditTimestamp() {
        var response = map("""
                {"isValid": false, "userError": "INVALID", "requestDate": "not-a-date"}
                """);
        var unavailable = assertInstanceOf(ViesResponse.Unavailable.class, response);
        assertEquals("MALFORMED_RESPONSE", unavailable.errorCode());
    }

    @Test
    void acceptsAnOffsetAuditTimestamp() {
        var response = map("""
                {"isValid": false, "userError": "INVALID", "requestDate": "2026-07-17T19:00:00+02:00"}
                """);
        var invalid = assertInstanceOf(ViesResponse.Invalid.class, response);
        assertEquals(Instant.parse("2026-07-17T17:00:00Z"), invalid.requestDate());
    }
}
