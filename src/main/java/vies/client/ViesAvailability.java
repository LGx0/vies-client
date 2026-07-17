/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Snapshot of the VIES {@code /check-status} endpoint: overall service health plus
 * the member-state availability entries included by VIES. The map may be partial or
 * empty. Useful for diagnostics —
 * member state backends go down routinely, and an {@code MS_UNAVAILABLE} answer is
 * easier to explain when you can show which state is offline.
 *
 * <p>Magyarul: megváltoztathatatlan pillanatkép a teljes VIES és az egyes
 * tagállami háttérrendszerek elérhetőségéről. Diagnosztikára használható.</p>
 *
 * @param serviceAvailable whether VIES-on-the-Web itself is up
 * @param memberStates     per-country availability, keyed by member state code
 */
public record ViesAvailability(boolean serviceAvailable, Map<String, MemberStateStatus> memberStates) {

    /**
     * Defensively copies the state map, preserving immutable snapshot semantics.
     * Magyarul: védelmi másolat készül, ezért a hívó később nem módosíthatja a pillanatképet.
     */
    public ViesAvailability {
        memberStates = Map.copyOf(Objects.requireNonNull(memberStates, "memberStates / tagállamok"));
    }

    /** Availability values reported by VIES / A VIES által jelzett állapotok. */
    public enum MemberStateStatus {
        AVAILABLE,
        UNAVAILABLE,
        UNKNOWN;

        static MemberStateStatus fromLabel(String label) {
            if (label == null) {
                return UNKNOWN;
            }
            return switch (label.strip().toLowerCase(Locale.ROOT)) {
                case "available" -> AVAILABLE;
                case "unavailable" -> UNAVAILABLE;
                default -> UNKNOWN;
            };
        }
    }
}
