/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies immutable availability snapshots and constructor validation.
 * Magyarul: az elérhetőségi pillanatkép megváltoztathatatlanságát és
 * konstruktor-validációját ellenőrzi.
 */
class ViesAvailabilityTest {

    @Test
    void defensivelyCopiesAndFreezesMemberStateMap() {
        var source = new HashMap<String, ViesAvailability.MemberStateStatus>();
        source.put("HU", ViesAvailability.MemberStateStatus.AVAILABLE);

        var snapshot = new ViesAvailability(true, source);
        source.put("DE", ViesAvailability.MemberStateStatus.UNAVAILABLE);

        assertEquals(Map.of("HU", ViesAvailability.MemberStateStatus.AVAILABLE),
                snapshot.memberStates());
        var memberStates = snapshot.memberStates();
        assertThrows(UnsupportedOperationException.class,
                () -> memberStates.put("PL", ViesAvailability.MemberStateStatus.UNKNOWN));
    }

    @Test
    void rejectsNullMemberStateMap() {
        assertThrows(NullPointerException.class, () -> new ViesAvailability(true, null));
    }
}
