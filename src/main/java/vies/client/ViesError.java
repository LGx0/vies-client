/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A stable, machine-readable VIES error with Hungarian and English explanations.
 *
 * <p>Magyarul: ez az objektum ugyanahhoz a hibakódhoz mindig magyar és angol
 * felhasználói üzenetet ad. A {@code retryable} mező jelzi, hogy egy külső,
 * tartós feldolgozási sorból érdemes-e később újrapróbálni a műveletet.</p>
 *
 * @param code       stable code for logging, metrics and API clients
 * @param messageHu  human-readable Hungarian explanation
 * @param messageEn  human-readable English explanation
 * @param retryable  whether retrying later can reasonably succeed
 */
public record ViesError(
        String code,
        String messageHu,
        String messageEn,
        boolean retryable
) {

    private static final Pattern SAFE_CODE = Pattern.compile("[A-Z][A-Z0-9_]{0,63}");

    public ViesError {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(messageHu, "messageHu");
        Objects.requireNonNull(messageEn, "messageEn");
    }

    /**
     * Converts every known client/VIES code to a bilingual message.
     * Ismeretlen kódnál is biztonságos, általános üzenetet ad vissza.
     */
    public static ViesError of(String rawCode) {
        var candidate = rawCode == null ? "" : rawCode.strip();
        // EN: Public callers cannot inject controls or unbounded text into error/log fields.
        // HU: Publikus hívó nem injektálhat vezérlőjelet vagy korlátlan szöveget a hibakódba/logba.
        var code = SAFE_CODE.matcher(candidate).matches() ? candidate : "UNKNOWN_ERROR";
        return switch (code) {
            case "SERVICE_UNAVAILABLE" -> retryable(code,
                    "A VIES szolgáltatás átmenetileg nem érhető el.",
                    "The VIES service is temporarily unavailable.");
            case "MS_UNAVAILABLE" -> retryable(code,
                    "A tagállami adóhatóság rendszere átmenetileg nem érhető el.",
                    "The member state's tax system is temporarily unavailable.");
            case "TIMEOUT" -> retryable(code,
                    "A VIES nem válaszolt időben; az adószám érvényessége nem került eldöntésre.",
                    "VIES did not respond in time; the VAT number was not validated.");
            case "SERVER_BUSY" -> retryable(code,
                    "A VIES szerver jelenleg túlterhelt.",
                    "The VIES server is currently busy.");
            case "GLOBAL_MAX_CONCURRENT_REQ", "GLOBAL_MAX_CONCURRENT_REQ_TIME" -> retryable(code,
                    "A VIES globális párhuzamos lekérdezési korlátja betelt.",
                    "The global VIES concurrent-request limit has been reached.");
            case "MS_MAX_CONCURRENT_REQ", "MS_MAX_CONCURRENT_REQ_TIME" -> retryable(code,
                    "A tagállam párhuzamos lekérdezési korlátja betelt.",
                    "The member state's concurrent-request limit has been reached.");
            case "NETWORK_ERROR" -> retryable(code,
                    "A VIES hálózati hiba miatt nem érhető el; az adószám érvényessége nem került eldöntésre.",
                    "VIES could not be reached due to a network error; the VAT number was not validated.");
            case "MALFORMED_RESPONSE" -> retryable(code,
                    "A VIES hiányos vagy értelmezhetetlen választ adott; ezt tilos érvénytelen eredménynek venni.",
                    "VIES returned an incomplete or malformed response; do not treat it as invalid.");
            case "CLIENT_OVERLOADED" -> retryable(code,
                    "A helyi VIES kliens elérte a biztonságos terhelési korlátját; próbáld újra később.",
                    "The local VIES client reached its safe capacity; retry later.");
            case "INTERRUPTED" -> permanent(code,
                    "A lekérdezést az alkalmazás megszakította.",
                    "The application interrupted the request.");
            case "INVALID_INPUT" -> permanent(code,
                    "A VIES hibásnak minősítette a kérést.",
                    "VIES rejected the request as invalid.");
            case "INVALID_VAT_FORMAT" -> permanent(code,
                    "Ismeretlen országkód vagy hibás közösségi adószám-formátum.",
                    "Unknown country code or invalid VAT-number format.");
            case "INVALID_REQUESTER_INFO" -> permanent(code,
                    "A lekérdező saját közösségi adószáma hibás.",
                    "The requester's own VAT number is invalid.");
            case "IP_BLOCKED" -> permanent(code,
                    "A VIES blokkolta a kliens IP-címét; üzemeltetői beavatkozás szükséges.",
                    "VIES blocked the client IP address; operator action is required.");
            case "VAT_BLOCKED" -> permanent(code,
                    "A VIES blokkolta az érintett adószám lekérdezését.",
                    "VIES blocked validation of the VAT number.");
            case "CACHE_ERROR" -> retryable(code,
                    "A megosztott VIES gyorsítótár átmenetileg nem érhető el.",
                    "The shared VIES cache is temporarily unavailable.");
            case "CLIENT_CLOSED" -> permanent(code,
                    "A VIES kliens leállt a lekérdezés befejezése előtt.",
                    "The VIES client closed before the request completed.");
            case "INTERNAL_ERROR" -> permanent(code,
                    "Belső alkalmazáshiba történt; üzemeltetői beavatkozás szükséges.",
                    "An internal application error occurred; operator action is required.");
            default -> code.startsWith("HTTP_") ? http(code) : permanent(code,
                    "A VIES nem tudta elbírálni az adószámot (" + code + ").",
                    "VIES could not determine the VAT number's status (" + code + ").");
        };
    }

    private static ViesError http(String code) {
        var retryable = code.equals("HTTP_408") || code.equals("HTTP_429") || code.startsWith("HTTP_5");
        return new ViesError(code,
                "A VIES HTTP-hibával válaszolt (" + code + ").",
                "VIES returned an HTTP error (" + code + ").",
                retryable);
    }

    private static ViesError retryable(String code, String hu, String en) {
        return new ViesError(code, hu, en, true);
    }

    private static ViesError permanent(String code, String hu, String en) {
        return new ViesError(code, hu, en, false);
    }
}
