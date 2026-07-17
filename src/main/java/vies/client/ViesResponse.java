/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import java.time.Instant;
import java.util.Optional;

/**
 * Outcome of a VIES VAT-number validation, modelled as a sealed hierarchy so callers
 * can pattern-match exhaustively:
 *
 * {@snippet :
 * switch (client.check("HU00000000")) {
 *     case ViesResponse.Valid v        -> log("Registered: " + v.traderName().orElse("?"));
 *     case ViesResponse.Invalid i      -> log("Not a registered VAT number");
 *     case ViesResponse.Unavailable u  -> log("VIES down, retry later: " + u.errorCode());
 *     case ViesResponse.MalformedInput m -> log("Bad input: " + m.reason());
 * }
 * }
 *
 * <p>Magyarul: a négy eredmény szándékosan különbözik. Az {@link Unavailable}
 * technikai bizonytalanság, ezért soha nem kezelhető {@link Invalid} eredményként.</p>
 */
public sealed interface ViesResponse
        permits ViesResponse.Valid, ViesResponse.Invalid, ViesResponse.Unavailable, ViesResponse.MalformedInput {

    /** Convenience shortcut: {@code true} only for {@link Valid}. */
    default boolean isValid() {
        return this instanceof Valid;
    }

    /**
     * Structured bilingual error information, empty for successful Valid/Invalid decisions.
     * Strukturált magyar–angol hiba, ha a VIES nem tudott döntést adni.
     */
    default Optional<ViesError> error() {
        if (this instanceof Unavailable unavailable) {
            return Optional.of(ViesError.of(unavailable.errorCode()));
        }
        if (this instanceof MalformedInput malformed) {
            var code = "INVALID_VAT_FORMAT";
            if (malformed.reason().contains("INVALID_REQUESTER_INFO")) {
                code = "INVALID_REQUESTER_INFO";
            } else if (malformed.reason().contains("VIES rejected")) {
                code = "INVALID_INPUT";
            }
            return Optional.of(ViesError.of(code));
        }
        return Optional.empty();
    }

    /**
     * The queried VAT number exists and is registered for intra-EU trade.
     * Magyarul: a VIES érvényesként igazolta a közösségi adószámot.
     *
     * @param countryCode        two-letter member state code ({@code EL} for Greece)
     * @param vatNumber          full VAT identifier, e.g. {@code HU00000000}
     * @param traderName         registered name, if the member state discloses it
     * @param traderAddress      registered address, if the member state discloses it
     * @param requestDate        timestamp reported by VIES for this consultation
     * @param consultationNumber optional consultation identifier
     *                           ({@code requestIdentifier}); VIES may return it when
     *                           the request was made with a {@link ViesRequester}
     * @param fromCache          {@code true} when served from the configured cache
     *                           (built-in or custom/external)
     */
    record Valid(
            String countryCode,
            String vatNumber,
            Optional<String> traderName,
            Optional<String> traderAddress,
            Instant requestDate,
            Optional<String> consultationNumber,
            boolean fromCache
    ) implements ViesResponse {
    }

    /**
     * VIES answered authoritatively: the number is not registered for intra-EU trade.
     * Magyarul: ez üzleti eredmény, nem technikai hiba; automatikus retry nem szükséges.
     */
    record Invalid(String countryCode, String vatNumber, Instant requestDate) implements ViesResponse {
    }

    /**
     * VIES (or the queried member state's backend) could not answer right now.
     * The number is neither confirmed nor denied — treat as "retry later",
     * never as "invalid".
     * Magyarul: a VIES nem tudott dönteni; tartós sorból később újrapróbálható.
     *
     * @param errorCode VIES error code (e.g. {@code MS_UNAVAILABLE},
     *                  {@code GLOBAL_MAX_CONCURRENT_REQ}) or a transport-level
     *                  code such as {@code NETWORK_ERROR}, {@code TIMEOUT},
     *                  {@code HTTP_500}, {@code MALFORMED_RESPONSE}, or
     *                  {@code CLIENT_OVERLOADED} when local async backpressure rejects work
     */
    record Unavailable(String countryCode, String vatNumber, String errorCode) implements ViesResponse {
    }

    /**
     * The input never reached VIES (unrecognized country prefix, format violation)
     * or VIES rejected the request as malformed ({@code INVALID_INPUT},
     * {@code INVALID_REQUESTER_INFO}). Retrying with the same input is pointless.
     * Magyarul: a bemenetet javítani kell, azonos adattal a retry értelmetlen.
     */
    record MalformedInput(String input, String reason) implements ViesResponse {
    }
}
