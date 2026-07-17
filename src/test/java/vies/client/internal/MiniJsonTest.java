/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client.internal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MiniJsonTest {

    @Test
    void parsesTypicalViesResponse() {
        var body = """
                {
                  "isValid" : true,
                  "requestDate" : "2026-07-17T13:11:12.131Z",
                  "userError" : "VALID",
                  "name" : "ACME Kft.",
                  "address" : "1051 BUDAPEST F\\u0150 UTCA 1.",
                  "requestIdentifier" : "",
                  "vatNumber" : "12345678"
                }
                """;
        var parsed = assertInstanceOf(Map.class, MiniJson.parse(body));
        assertEquals(Boolean.TRUE, parsed.get("isValid"));
        assertEquals("ACME Kft.", parsed.get("name"));
        assertEquals("1051 BUDAPEST FŐ UTCA 1.", parsed.get("address"));
        assertEquals("", parsed.get("requestIdentifier"));
    }

    @Test
    void parsesNestedStructuresAndScalars() {
        var parsed = MiniJson.parse("""
                {"vow":{"available":true},"countries":[{"countryCode":"AT"},{"countryCode":"BE"}],"n":-1.5e2,"x":null}
                """.strip());
        var root = assertInstanceOf(Map.class, parsed);
        var vow = assertInstanceOf(Map.class, root.get("vow"));
        assertEquals(Boolean.TRUE, vow.get("available"));
        var countries = assertInstanceOf(List.class, root.get("countries"));
        assertEquals(2, countries.size());
        assertEquals(0, new BigDecimal("-150").compareTo((BigDecimal) root.get("n")));
        assertTrue(root.containsKey("x"));
        assertNull(root.get("x"));
    }

    @Test
    void handlesEscapes() {
        var parsed = (Map<?, ?>) MiniJson.parse("{\"s\":\"a\\\"b\\\\c\\nd\\t\\/\"}");
        assertEquals("a\"b\\c\nd\t/", parsed.get("s"));
    }

    @Test
    void rejectsMalformedInput() {
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("{"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("{\"a\":}"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("{} trailing"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("{'a':1}"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("\"unterminated"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse(""));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("nul"));
        assertThrows(MiniJson.ParseException.class,
                () -> MiniJson.parse("[".repeat(65) + "0" + "]".repeat(65)));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("{\"a\":1,\"a\":2}"));
        assertThrows(MiniJson.ParseException.class,
                () -> MiniJson.parse("1e" + "9".repeat(200)));
        assertThrows(MiniJson.ParseException.class,
                () -> MiniJson.parse("[" + "0,".repeat(4_096) + "0]"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("01"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("-01"));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("\"\\uD800\""));
        assertThrows(MiniJson.ParseException.class, () -> MiniJson.parse("\"\\uDC00\""));
        assertEquals("💥", MiniJson.parse("\"\\uD83D\\uDCA5\""));
    }
}
