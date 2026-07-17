/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

/**
 * The party on whose behalf the validation is performed (typically your own company's
 * EU VAT number). When present, VIES may return a consultation number
 * ({@code requestIdentifier}). The value is optional; retain it together with the
 * request date and input when your audit process requires it. Its legal or
 * evidentiary significance depends on the applicable rules.
 *
 * <p>Both components are normalized and format-validated eagerly; construction fails
 * fast with {@link IllegalArgumentException} on malformed input.</p>
 *
 * <p>Magyarul: annak a félnek a saját közösségi adószáma, akinek a nevében a
 * lekérdezés történik. Megadásával a VIES konzultációs azonosítót adhat.</p>
 *
 * @param countryCode  two-letter member state code ({@code GR} is mapped to {@code EL})
 * @param vatNumber    national part without the country prefix, e.g. {@code 12345678}
 */
public record ViesRequester(String countryCode, String vatNumber) {

    public ViesRequester {
        var rawCountry = countryCode;
        var rawNumber = vatNumber;
        var normalized = VatFormat.normalize(countryCode, vatNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid requester VAT number / Hibás lekérdezői adószám: "
                                + safeInput(rawCountry) + " " + safeInput(rawNumber)));
        countryCode = normalized.countryCode();
        vatNumber = normalized.nationalNumber();
    }

    /** Creates a requester from a full identifier such as {@code "HU00000000"}. */
    public static ViesRequester of(String fullVatNumber) {
        var normalized = VatFormat.normalize(fullVatNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid requester VAT number / Hibás lekérdezői adószám: "
                                + safeInput(fullVatNumber)));
        return new ViesRequester(normalized.countryCode(), normalized.nationalNumber());
    }

    /** Full identifier, e.g. {@code HU00000000}. */
    public String full() {
        return countryCode + vatNumber;
    }

    /** Bounds reflected input in exceptions / Korlátozza a kivételben visszaadott bemenetet. */
    private static String safeInput(String input) {
        var value = String.valueOf(input);
        var safe = new StringBuilder(Math.min(value.length(), 129));
        var offset = 0;
        var count = 0;
        while (offset < value.length() && count < 128) {
            var codePoint = value.codePointAt(offset);
            var unpairedSurrogate = codePoint <= Character.MAX_VALUE
                    && Character.isSurrogate((char) codePoint);
            if (unpairedSurrogate || Character.isISOControl(codePoint)
                    || codePoint == 0x2028 || codePoint == 0x2029) {
                safe.append('?');
            } else {
                safe.appendCodePoint(codePoint);
            }
            offset += Character.charCount(codePoint);
            count++;
        }
        return offset < value.length() ? safe.append('…').toString() : safe.toString();
    }
}
