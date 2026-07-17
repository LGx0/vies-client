/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Map.entry;

/**
 * Offline normalization and per-member-state format validation of EU VAT numbers,
 * following the formats published on the VIES technical-information page
 * (<a href="https://ec.europa.eu/taxation_customs/vies/#/technical-information">europa.eu</a>).
 *
 * <p>A passing format check is <em>not</em> a VIES confirmation — it merely avoids
 * burning a network round-trip on input that VIES would reject anyway.</p>
 *
 * <p>Magyarul: ez csak formai előszűrés. A sikeres regex-ellenőrzés nem bizonyítja,
 * hogy az adószám él; erről kizárólag a VIES válasza dönt.</p>
 */
public final class VatFormat {

    /**
     * Raw VAT input is intentionally tiny. Rejecting an oversized value before
     * allocating the normalization buffer prevents amplification under hostile load.
     * Magyarul: a túl hosszú bemenetet még a másoló puffer létrehozása előtt eldobjuk.
     */
    private static final int MAX_RAW_VAT_LENGTH = 64;
    private static final int MAX_RAW_COUNTRY_LENGTH = 8;
    private static final String EIGHT_DIGITS = "\\d{8}";
    private static final String NINE_DIGITS = "\\d{9}";
    private static final String ELEVEN_DIGITS = "\\d{11}";

    /** A normalized VAT identifier: two-letter member state code + national part. */
    public record Normalized(String countryCode, String nationalNumber) {
        /** Full identifier, e.g. {@code HU00000000}. */
        public String full() {
            return countryCode + nationalNumber;
        }
    }

    private static final Map<String, Pattern> FORMATS = Map.ofEntries(
            entry("AT", Pattern.compile("U\\d{8}")),
            entry("BE", Pattern.compile("[01]\\d{9}")),
            entry("BG", Pattern.compile("\\d{9,10}")),
            entry("CY", Pattern.compile("\\d{8}[A-Z]")),
            entry("CZ", Pattern.compile("\\d{8,10}")),
            entry("DE", Pattern.compile(NINE_DIGITS)),
            entry("DK", Pattern.compile(EIGHT_DIGITS)),
            entry("EE", Pattern.compile(NINE_DIGITS)),
            entry("EL", Pattern.compile(NINE_DIGITS)),
            entry("ES", Pattern.compile("[A-Z0-9]\\d{7}[A-Z0-9]")),
            entry("FI", Pattern.compile(EIGHT_DIGITS)),
            entry("FR", Pattern.compile("[A-Z0-9]{2}\\d{9}")),
            entry("HR", Pattern.compile(ELEVEN_DIGITS)),
            entry("HU", Pattern.compile(EIGHT_DIGITS)),
            entry("IE", Pattern.compile("\\d{7}[A-W][A-I]?|\\d[A-Z+*]\\d{5}[A-W]")),
            entry("IT", Pattern.compile(ELEVEN_DIGITS)),
            entry("LT", Pattern.compile("\\d{9}|\\d{12}")),
            entry("LU", Pattern.compile(EIGHT_DIGITS)),
            entry("LV", Pattern.compile(ELEVEN_DIGITS)),
            entry("MT", Pattern.compile(EIGHT_DIGITS)),
            entry("NL", Pattern.compile("\\d{9}B\\d{2}")),
            entry("PL", Pattern.compile("\\d{10}")),
            entry("PT", Pattern.compile(NINE_DIGITS)),
            entry("RO", Pattern.compile("\\d{2,10}")),
            entry("SE", Pattern.compile("\\d{10}01")),
            entry("SI", Pattern.compile(EIGHT_DIGITS)),
            entry("SK", Pattern.compile("\\d{10}")),
            // Northern Ireland stays in VIES for goods under the Windsor Framework.
            entry("XI", Pattern.compile("\\d{9}|\\d{12}|(GD|HA)\\d{3}"))
    );

    private VatFormat() {
    }

    /** Member state codes VIES accepts (Greece is {@code EL}, Northern Ireland is {@code XI}). */
    public static Set<String> supportedCountries() {
        return FORMATS.keySet();
    }

    public static boolean isSupportedCountry(String countryCode) {
        return countryCode != null
                && countryCode.length() <= MAX_RAW_COUNTRY_LENGTH
                && FORMATS.containsKey(canonicalCountry(countryCode));
    }

    /**
     * Normalizes a full VAT identifier such as {@code "hu-12 345.678"} or
     * {@code "GR123456789"}: strips separators, upper-cases, maps {@code GR → EL},
     * then validates the national part against the member state's format.
     *
     * @return the normalized identifier, or empty when the prefix is unknown
     *         or the national part violates the member state's format
     */
    public static Optional<Normalized> normalize(String rawVatNumber) {
        if (rawVatNumber == null || rawVatNumber.length() > MAX_RAW_VAT_LENGTH) {
            return Optional.empty();
        }
        var cleaned = clean(rawVatNumber);
        if (cleaned.length() < 4) {
            return Optional.empty();
        }
        var country = canonicalCountry(cleaned.substring(0, 2));
        var pattern = FORMATS.get(country);
        if (pattern == null) {
            return Optional.empty();
        }
        var national = cleaned.substring(2);
        return pattern.matcher(national).matches()
                ? Optional.of(new Normalized(country, national))
                : Optional.empty();
    }

    /**
     * Normalizes a (country code, national number) pair. Tolerates the national
     * number arriving with the country prefix already attached: the value is first
     * validated as-is, then — if that fails — with the leading prefix stripped.
     */
    public static Optional<Normalized> normalize(String countryCode, String nationalNumber) {
        if (countryCode == null || nationalNumber == null
                || countryCode.length() > MAX_RAW_COUNTRY_LENGTH
                || nationalNumber.length() > MAX_RAW_VAT_LENGTH) {
            return Optional.empty();
        }
        var country = canonicalCountry(countryCode);
        var pattern = FORMATS.get(country);
        if (pattern == null) {
            return Optional.empty();
        }
        var cleaned = clean(nationalNumber);
        if (pattern.matcher(cleaned).matches()) {
            return Optional.of(new Normalized(country, cleaned));
        }
        if (cleaned.length() > 2) {
            var prefix = canonicalCountry(cleaned.substring(0, 2));
            var stripped = cleaned.substring(2);
            if (prefix.equals(country) && pattern.matcher(stripped).matches()) {
                return Optional.of(new Normalized(country, stripped));
            }
        }
        return Optional.empty();
    }

    private static String canonicalCountry(String code) {
        var upper = code.strip().toUpperCase(Locale.ROOT);
        // Greece uses the ISO code GR but VIES expects the historical EL.
        return "GR".equals(upper) ? "EL" : upper;
    }

    /**
     * One allocation, no regex engine on the hot path.
     * Magyarul: egyetlen tömbből tisztítjuk és nagybetűsítjük a rövid adószámot.
     */
    private static String clean(String value) {
        var chars = new char[value.length()];
        var size = 0;
        for (var i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            if (c == '.' || c == '-' || Character.isWhitespace(c)) {
                continue;
            }
            chars[size++] = c >= 'a' && c <= 'z' ? (char) (c - ('a' - 'A')) : c;
        }
        return new String(chars, 0, size);
    }
}
