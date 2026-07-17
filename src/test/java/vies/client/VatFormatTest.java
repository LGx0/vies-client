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

class VatFormatTest {

    @Test
    void normalizesFullIdentifier() {
        var result = VatFormat.normalize("HU00000000").orElseThrow();
        assertEquals("HU", result.countryCode());
        assertEquals("00000000", result.nationalNumber());
        assertEquals("HU00000000", result.full());
    }

    @Test
    void stripsSeparatorsAndUppercases() {
        var result = VatFormat.normalize("hu-00 000.000").orElseThrow();
        assertEquals("HU00000000", result.full());
    }

    @Test
    void mapsGreeceToEl() {
        var result = VatFormat.normalize("GR123456789").orElseThrow();
        assertEquals("EL", result.countryCode());
        assertEquals("EL123456789", result.full());
    }

    @Test
    void rejectsUnknownCountryAndBadFormat() {
        assertTrue(VatFormat.normalize("XX12345678").isEmpty());
        assertTrue(VatFormat.normalize("HU1234567").isEmpty());   // 7 digits
        assertTrue(VatFormat.normalize("AT12345678").isEmpty());  // Austria requires the U prefix
        assertTrue(VatFormat.normalize((String) null).isEmpty());
        assertTrue(VatFormat.normalize("").isEmpty());
        assertTrue(VatFormat.normalize("H".repeat(10_000)).isEmpty());
        assertTrue(VatFormat.normalize("HU", "1".repeat(10_000)).isEmpty());
    }

    @Test
    void acceptsCountrySpecificShapes() {
        assertTrue(VatFormat.normalize("ATU12345678").isPresent());
        assertTrue(VatFormat.normalize("NL123456789B01").isPresent());
        assertTrue(VatFormat.normalize("IE1234567FA").isPresent());
        assertTrue(VatFormat.normalize("SE123456789001").isPresent());
        assertTrue(VatFormat.normalize("XI123456789").isPresent());
    }

    @Test
    void pairNormalizationToleratesAttachedPrefix() {
        var result = VatFormat.normalize("HU", "HU00000000").orElseThrow();
        assertEquals("00000000", result.nationalNumber());

        var greek = VatFormat.normalize("gr", "123456789").orElseThrow();
        assertEquals("EL", greek.countryCode());
    }

    @Test
    void supportedCountriesUseViesCodes() {
        assertTrue(VatFormat.supportedCountries().contains("EL"));
        assertTrue(VatFormat.supportedCountries().contains("XI"));
        assertFalse(VatFormat.supportedCountries().contains("GR"));
        assertTrue(VatFormat.isSupportedCountry("gr")); // canonicalized to EL
        assertFalse(VatFormat.isSupportedCountry("GB"));
    }

    @Test
    void acceptsAtLeastOnePublishedShapeForEverySupportedCountry() {
        var examples = Map.ofEntries(
                Map.entry("AT", "ATU12345678"), Map.entry("BE", "BE0123456789"),
                Map.entry("BG", "BG123456789"), Map.entry("CY", "CY12345678A"),
                Map.entry("CZ", "CZ12345678"), Map.entry("DE", "DE123456789"),
                Map.entry("DK", "DK12345678"), Map.entry("EE", "EE123456789"),
                Map.entry("EL", "EL123456789"), Map.entry("ES", "ESX1234567X"),
                Map.entry("FI", "FI12345678"), Map.entry("FR", "FRXX123456789"),
                Map.entry("HR", "HR12345678901"), Map.entry("HU", "HU00000000"),
                Map.entry("IE", "IE1234567FA"), Map.entry("IT", "IT12345678901"),
                Map.entry("LT", "LT123456789"), Map.entry("LU", "LU12345678"),
                Map.entry("LV", "LV12345678901"), Map.entry("MT", "MT12345678"),
                Map.entry("NL", "NL123456789B01"), Map.entry("PL", "PL1234567890"),
                Map.entry("PT", "PT123456789"), Map.entry("RO", "RO12"),
                Map.entry("SE", "SE123456789001"), Map.entry("SI", "SI12345678"),
                Map.entry("SK", "SK1234567890"), Map.entry("XI", "XI123456789"));

        assertEquals(VatFormat.supportedCountries(), examples.keySet());
        examples.forEach((country, vat) ->
                assertTrue(VatFormat.normalize(vat).isPresent(), country + ": " + vat));
    }
}
