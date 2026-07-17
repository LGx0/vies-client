/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Zero-dependency client for the European Commission's VIES VAT-number
 * validation REST API.
 * Magyarul: nulla futásidejű függőségű kliens az Európai Bizottság VIES
 * közösségiadószám-ellenőrző REST API-jához.
 */
module vies.client {
    requires java.net.http;

    exports vies.client;
}
