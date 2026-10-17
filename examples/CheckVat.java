/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package examples;

import vies.client.ViesClient;
import vies.client.ViesRequester;
import vies.client.ViesResponse;

/**
 * Complete command-line example. No argument performs an offline invalid-input check.
 * Magyarul: teljes parancssori példa; argumentum nélkül csak offline bemeneti ellenőrzést végez.
 *
 * <p>Run / Futtatás: {@code java -cp vies-client-1.2.1.jar examples/CheckVat.java not-a-vat}.
 * Pass a legitimate full EU VAT number to query VIES. Set MY_EU_VAT_NUMBER only to
 * your own authorized requester VAT. Magyarul: valódi teljes EU-adószámmal VIES-t kérdez;
 * a MY_EU_VAT_NUMBER csak a saját, jogosan használt lekérdezői adószám legyen.</p>
 */
public final class CheckVat {
    private static final System.Logger LOGGER = System.getLogger(CheckVat.class.getName());

    private CheckVat() {
    }

    public static void main(String[] args) {
        var builder = ViesClient.builder();
        var requester = System.getenv("MY_EU_VAT_NUMBER");
        if (requester != null && !requester.isBlank()) {
            builder.defaultRequester(ViesRequester.of(requester));
        }
        // EN: Reuse one client in a server; this small CLI closes it before exiting.
        // HU: Szerverben egy kliens legyen megosztva; ez a kis CLI kilépés előtt lezárja.
        try (var client = builder.build()) {
            var result = client.check(args.length == 0 ? "" : args[0]);
            switch (result) {
                case ViesResponse.Valid valid -> LOGGER.log(System.Logger.Level.INFO,
                        "VALID / ÉRVÉNYES: {0}; requestDate={1}; fromCache={2}",
                        valid.vatNumber(), valid.requestDate(), valid.fromCache());
                case ViesResponse.Invalid invalid -> LOGGER.log(System.Logger.Level.INFO,
                        "INVALID / NEM ÉRVÉNYES: {0}; requestDate={1}",
                        invalid.vatNumber(), invalid.requestDate());
                case ViesResponse.Unavailable unavailable -> logError(unavailable);
                case ViesResponse.MalformedInput malformed -> logError(malformed);
            }
        }
    }

    /** EN: Show both languages and a stable code. HU: Mindkét nyelvet és stabil hibakódot adunk. */
    private static void logError(ViesResponse result) {
        var error = result.error().orElseThrow();
        LOGGER.log(System.Logger.Level.WARNING, "{0}; retryable={1}; HU: {2}; EN: {3}",
                error.code(), error.retryable(), error.messageHu(), error.messageEn());
    }
}
