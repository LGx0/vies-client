/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client.internal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal, dependency-free JSON parser — just enough to decode the small, stable
 * response documents of the VIES REST API without dragging a JSON library into the
 * dependency tree. Values map to {@code Map<String,Object>}, {@code List<Object>},
 * {@code String}, {@code BigDecimal}, {@code Boolean} and {@code null}.
 *
 * <p>Internal API: not exported from the module, no compatibility guarantees.</p>
 *
 * <p>Magyarul: kis, belső JSON-olvasó kizárólag a VIES rövid válaszaihoz. A külső
 * JSON minden mezőjét a {@code ViesClient} külön is típus- és tartalomellenőrzi.</p>
 */
public final class MiniJson {

    /**
     * EN: Bounds recursive parsing, container growth and decimal construction.
     * HU: Korlátozza a rekurziót, a konténerek növekedését és a decimális feldolgozást.
     */
    private static final int MAX_NESTING_DEPTH = 64;
    private static final int MAX_NUMBER_LENGTH = 128;
    private static final int MAX_JSON_VALUES = 4_096;

    /** Thrown when the input is not well-formed JSON. */
    public static final class ParseException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        ParseException(String message, int position) {
            super(message + " (at index " + position + ")");
        }
    }

    private final String src;
    private int pos;
    private int values;

    private MiniJson(String src) {
        this.src = src;
    }

    public static Object parse(String input) {
        if (input == null) {
            throw new ParseException("Input is null", 0);
        }
        var parser = new MiniJson(input);
        parser.skipWhitespace();
        var value = parser.parseValue(0);
        parser.skipWhitespace();
        if (parser.pos != input.length()) {
            throw new ParseException("Trailing characters after JSON value", parser.pos);
        }
        return value;
    }

    private Object parseValue(int depth) {
        if (++values > MAX_JSON_VALUES) {
            throw new ParseException("Maximum JSON value count exceeded", pos);
        }
        if (pos >= src.length()) {
            throw new ParseException("Unexpected end of input", pos);
        }
        var c = src.charAt(pos);
        return switch (c) {
            case '{' -> parseObject(nextDepth(depth));
            case '[' -> parseArray(nextDepth(depth));
            case '"' -> parseString();
            case 't' -> parseLiteral("true", Boolean.TRUE);
            case 'f' -> parseLiteral("false", Boolean.FALSE);
            case 'n' -> parseLiteral("null", null);
            default -> {
                if (c == '-' || (c >= '0' && c <= '9')) {
                    yield parseNumber();
                }
                throw new ParseException("Unexpected character '" + c + "'", pos);
            }
        };
    }

    private Map<String, Object> parseObject(int depth) {
        expect('{');
        var result = new LinkedHashMap<String, Object>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw new ParseException("Expected string key", pos);
            }
            var key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            if (result.containsKey(key)) {
                throw new ParseException("Duplicate object key '" + key + "'", pos);
            }
            result.put(key, parseValue(depth));
            skipWhitespace();
            var c = peek();
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == '}') {
                pos++;
                return result;
            }
            throw new ParseException("Expected ',' or '}' in object", pos);
        }
    }

    private List<Object> parseArray(int depth) {
        expect('[');
        var result = new ArrayList<Object>();
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            result.add(parseValue(depth));
            skipWhitespace();
            var c = peek();
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == ']') {
                pos++;
                return result;
            }
            throw new ParseException("Expected ',' or ']' in array", pos);
        }
    }

    private String parseString() {
        expect('"');
        var sb = new StringBuilder();
        while (true) {
            var c = nextStringCharacter();
            if (c == '"') {
                return sb.toString();
            }
            if (c == '\\') {
                appendEscapedCharacter(sb);
            } else {
                appendRawCharacter(sb, c);
            }
        }
    }

    /** Reads one code unit while retaining the exact error position for malformed input. */
    private char nextStringCharacter() {
        if (pos >= src.length()) {
            throw new ParseException("Unterminated string", pos);
        }
        return src.charAt(pos++);
    }

    /** Validates escaped UTF-16, including mandatory high/low-surrogate pairing. */
    private void appendEscapedCharacter(StringBuilder sb) {
        var escaped = parseEscape();
        if (Character.isHighSurrogate(escaped)) {
            appendEscapedSurrogatePair(sb, escaped);
        } else if (Character.isLowSurrogate(escaped)) {
            throw new ParseException("Low surrogate without high surrogate", pos);
        } else {
            sb.append(escaped);
        }
    }

    private void appendEscapedSurrogatePair(StringBuilder sb, char high) {
        if (pos + 2 > src.length() || src.charAt(pos) != '\\' || src.charAt(pos + 1) != 'u') {
            throw new ParseException("High surrogate without low surrogate", pos);
        }
        pos += 2;
        var low = parseUnicodeEscape();
        if (!Character.isLowSurrogate(low)) {
            throw new ParseException("Invalid low surrogate", pos - 4);
        }
        sb.append(high).append(low);
    }

    /** Rejects raw control characters and unpaired UTF-16 surrogates. */
    private void appendRawCharacter(StringBuilder sb, char current) {
        if (current < 0x20) {
            throw new ParseException("Unescaped control character in string", pos - 1);
        }
        if (Character.isHighSurrogate(current)) {
            appendRawSurrogatePair(sb, current);
        } else if (Character.isLowSurrogate(current)) {
            throw new ParseException("Low surrogate without high surrogate", pos - 1);
        } else {
            sb.append(current);
        }
    }

    private void appendRawSurrogatePair(StringBuilder sb, char high) {
        if (pos >= src.length() || !Character.isLowSurrogate(src.charAt(pos))) {
            throw new ParseException("High surrogate without low surrogate", pos - 1);
        }
        sb.append(high).append(src.charAt(pos++));
    }

    private char parseEscape() {
        if (pos >= src.length()) {
            throw new ParseException("Unterminated escape sequence", pos);
        }
        var c = src.charAt(pos++);
        return switch (c) {
            case '"' -> '"';
            case '\\' -> '\\';
            case '/' -> '/';
            case 'b' -> '\b';
            case 'f' -> '\f';
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case 'u' -> parseUnicodeEscape();
            default -> throw new ParseException("Invalid escape '\\" + c + "'", pos - 1);
        };
    }

    private char parseUnicodeEscape() {
        if (pos + 4 > src.length()) {
            throw new ParseException("Truncated \\u escape", pos);
        }
        var hex = src.substring(pos, pos + 4);
        try {
            var code = Integer.parseInt(hex, 16);
            pos += 4;
            return (char) code;
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid \\u escape '" + hex + "'", pos);
        }
    }

    private BigDecimal parseNumber() {
        var start = pos;
        consumeOptionalMinus();
        consumeIntegerPart();
        consumeOptionalFraction();
        consumeOptionalExponent();
        return toDecimal(start);
    }

    /** Splits JSON-number validation into grammar-sized steps without relaxing the grammar. */
    private void consumeOptionalMinus() {
        if (peek() == '-') {
            pos++;
        }
    }

    private void consumeIntegerPart() {
        if (pos < src.length() && src.charAt(pos) == '0') {
            pos++;
            if (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                throw new ParseException("Leading zero in number", pos);
            }
        } else {
            consumeDigits("number");
        }
    }

    private void consumeOptionalFraction() {
        if (pos < src.length() && src.charAt(pos) == '.') {
            pos++;
            consumeDigits("fraction");
        }
    }

    private void consumeOptionalExponent() {
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            pos++;
            if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            consumeDigits("exponent");
        }
    }

    private BigDecimal toDecimal(int start) {
        if (pos - start > MAX_NUMBER_LENGTH) {
            throw new ParseException("Number is too long", start);
        }
        try {
            return new BigDecimal(src.substring(start, pos));
        } catch (NumberFormatException e) {
            throw new ParseException("Number is outside the supported JSON range", start);
        }
    }

    /**
     * EN: VIES documents are shallow; a hard depth limit prevents stack exhaustion.
     * HU: A VIES-válasz sekély; a fix mélységi korlát megakadályozza a veremkimerítést.
     */
    private int nextDepth(int depth) {
        if (depth >= MAX_NESTING_DEPTH) {
            throw new ParseException("Maximum nesting depth exceeded", pos);
        }
        return depth + 1;
    }

    private void consumeDigits(String part) {
        var start = pos;
        while (pos < src.length() && src.charAt(pos) >= '0' && src.charAt(pos) <= '9') {
            pos++;
        }
        if (pos == start) {
            throw new ParseException("Expected digit in " + part, pos);
        }
    }

    private Object parseLiteral(String literal, Object value) {
        if (!src.startsWith(literal, pos)) {
            throw new ParseException("Invalid literal, expected '" + literal + "'", pos);
        }
        pos += literal.length();
        return value;
    }

    private char peek() {
        if (pos >= src.length()) {
            throw new ParseException("Unexpected end of input", pos);
        }
        return src.charAt(pos);
    }

    private void expect(char c) {
        if (pos >= src.length() || src.charAt(pos) != c) {
            throw new ParseException("Expected '" + c + "'", pos);
        }
        pos++;
    }

    private void skipWhitespace() {
        while (pos < src.length()) {
            var c = src.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                return;
            }
        }
    }
}
