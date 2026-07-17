/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package examples;

import vies.client.ViesClient;
import vies.client.ViesError;
import vies.client.ViesResponse;
import com.sun.net.httpserver.HttpServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Minimal demo API server showing how to wire the VIES client into a plain-JDK
 * HTTP server (no framework needed).
 * Magyarul: minimális, keretrendszer nélküli HTTP-bekötési példa kétnyelvű
 * és géppel feldolgozható hibaválaszokkal.
 *
 * <p>Security / Biztonság: the demo binds to loopback only and is not an
 * internet-facing API. Add authentication, TLS, tenant authorization and a
 * distributed rate limiter before adapting it for production. Magyarul: csak
 * helyi címre figyel; éles használathoz hitelesítés, TLS, tenant-jogosultság és
 * elosztott rate limiter kötelező.</p>
 *
 * <pre>
 *   mvn -q package
 *   java -cp target/classes examples/ViesDemoServer.java        # port 8085
 *   curl "http://localhost:8085/vat-check?number=HU00000000"
 * </pre>
 */
public final class ViesDemoServer {

    private static final System.Logger LOGGER = System.getLogger(ViesDemoServer.class.getName());

    public static void main(String[] args) throws Exception {
        var port = args.length > 0 ? Integer.parseInt(args[0]) : 8085;
        try (var vies = ViesClient.builder().retries(1).build();
             var serverExecutor = new ThreadPoolExecutor(
                     8, 32, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(256),
                     Thread.ofPlatform().name("vies-demo-http-", 0).factory(),
                     new ThreadPoolExecutor.CallerRunsPolicy())) {
            // EN: Loopback + bounded backlog/worker pool prevents an accidental public proxy.
            // HU: A loopback és korlátos backlog/worker pool megelőzi a véletlen publikus proxyt.
            var server = HttpServer.create(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 64);
            server.setExecutor(serverExecutor);
            server.createContext("/vat-check", exchange -> {
                int status;
                String json;
                try {
                    if (!"GET".equals(exchange.getRequestMethod())) {
                        status = 405;
                        json = standaloneErrorJson("METHOD_NOT_ALLOWED", ViesError.of("INVALID_INPUT"));
                    } else {
                        var number = extractNumber(exchange.getRequestURI().getRawQuery());
                        var response = vies.check(number);
                        status = httpStatus(response);
                        json = toJson(response);
                    }
                } catch (IllegalArgumentException badEncoding) {
                    status = 400;
                    json = standaloneErrorJson("MALFORMED_INPUT", ViesError.of("INVALID_INPUT"));
                } catch (RuntimeException unexpected) {
                    status = 500;
                    json = standaloneErrorJson("INTERNAL_ERROR", ViesError.of("INTERNAL_ERROR"));
                }
                var body = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                exchange.getResponseHeaders().set("Cache-Control", "no-store");
                exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
                exchange.sendResponseHeaders(status, body.length);
                try (var out = exchange.getResponseBody()) {
                    out.write(body);
                }
            });
            server.start();
            LOGGER.log(System.Logger.Level.INFO,
                    "VIES demo listening on http://localhost:{0}/vat-check?number=HU00000000", port);
            try {
                new CountDownLatch(1).await();
            } finally {
                server.stop(1);
            }
        }
    }

    private static String extractNumber(String rawQuery) {
        if (rawQuery == null) {
            return "";
        }
        if (rawQuery.length() > 256) {
            throw new IllegalArgumentException("Query is too long / A query túl hosszú");
        }
        for (var param : rawQuery.split("&")) {
            if (param.startsWith("number=")) {
                var decoded = URLDecoder.decode(
                        param.substring("number=".length()), StandardCharsets.UTF_8);
                if (decoded.length() > 64) {
                    throw new IllegalArgumentException("VAT number is too long / Az adószám túl hosszú");
                }
                return decoded;
            }
        }
        return "";
    }

    private static String toJson(ViesResponse response) {
        return switch (response) {
            case ViesResponse.Valid v ->
                    "{\"status\":\"VALID\",\"vatNumber\":%s,\"traderName\":%s,\"traderAddress\":%s,"
                    + "\"consultationNumber\":%s,\"fromCache\":%b}"
                    .formatted(quote(v.vatNumber()), quote(v.traderName().orElse(null)),
                            quote(v.traderAddress().orElse(null)),
                            quote(v.consultationNumber().orElse(null)), v.fromCache());
            case ViesResponse.Invalid i ->
                    "{\"status\":\"INVALID\",\"vatNumber\":%s}"
                    .formatted(quote(i.vatNumber()));
            case ViesResponse.Unavailable u ->
                    "{\"status\":\"UNAVAILABLE\",\"vatNumber\":%s,%s}"
                    .formatted(quote(u.vatNumber()), errorJson(u));
            case ViesResponse.MalformedInput m ->
                    "{\"status\":\"MALFORMED_INPUT\",\"input\":%s,\"reason\":%s,%s}"
                    .formatted(quote(m.input()), quote(m.reason()), errorJson(m));
        };
    }

    /** HTTP status mapping / HTTP státuszkód hozzárendelése. */
    private static int httpStatus(ViesResponse response) {
        return switch (response) {
            case ViesResponse.Valid ignored -> 200;
            case ViesResponse.Invalid ignored -> 200;
            case ViesResponse.MalformedInput ignored -> 400;
            case ViesResponse.Unavailable unavailable ->
                    "CLIENT_OVERLOADED".equals(unavailable.errorCode()) ? 429 : 503;
        };
    }

    /** Bilingual error JSON / Kétnyelvű hiba JSON. */
    private static String errorJson(ViesResponse response) {
        var error = response.error().orElseThrow();
        return "\"errorCode\":" + quote(error.code())
                + ",\"messageHu\":" + quote(error.messageHu())
                + ",\"messageEn\":" + quote(error.messageEn())
                + ",\"retryable\":" + error.retryable();
    }

    /** Standalone safe error body / Önálló, biztonságos hibatörzs. */
    private static String standaloneErrorJson(String status, ViesError error) {
        return "{\"status\":" + quote(status)
                + ",\"errorCode\":" + quote(error.code())
                + ",\"messageHu\":" + quote(error.messageHu())
                + ",\"messageEn\":" + quote(error.messageEn())
                + ",\"retryable\":" + error.retryable() + "}";
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        var sb = new StringBuilder("\"");
        for (var i = 0; i < value.length(); i++) {
            var c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
