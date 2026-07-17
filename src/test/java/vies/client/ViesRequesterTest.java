/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViesRequesterTest {

    @Test
    void createsRequesterFromFullVatNumber() {
        var requester = ViesRequester.of("hu-00 000.000");
        assertEquals("HU", requester.countryCode());
        assertEquals("00000000", requester.vatNumber());
        assertEquals("HU00000000", requester.full());
    }

    @Test
    void canonicalizesGreekCountryCode() {
        assertEquals("EL123456789", ViesRequester.of("GR123456789").full());
    }

    @Test
    void pairConstructorAcceptsAnAttachedMatchingPrefix() {
        assertEquals("HU00000000", new ViesRequester("HU", "HU00000000").full());
    }

    @Test
    void rejectsInvalidRequesterImmediately() {
        assertThrows(IllegalArgumentException.class, () -> ViesRequester.of("HU123"));
        assertThrows(IllegalArgumentException.class, () -> new ViesRequester("XX", "12345678"));
    }
}
