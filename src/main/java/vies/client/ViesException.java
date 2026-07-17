/*
 * Copyright 2026 VIES Client contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package vies.client;

/**
 * Thrown by auxiliary operations ({@link ViesClient#availability()}) when VIES cannot
 * be reached, returns an unsuccessful HTTP status, or the response is not valid
 * bounded JSON.
 *
 * <p>Note that {@link ViesClient#check(String)} never throws for transport problems —
 * it reports them as {@link ViesResponse.Unavailable} so validation call sites stay
 * exception-free.</p>
 *
 * <p>Magyarul: a diagnosztikai {@code availability()} művelet hibája. A normál
 * adószám-ellenőrzés hálózati problémánál nem dobja ezt, hanem strukturált
 * {@link ViesResponse.Unavailable} eredményt ad.</p>
 */
public class ViesException extends RuntimeException {
    private static final long serialVersionUID = 1L;


    public ViesException(String message) {
        super(message);
    }

    public ViesException(String message, Throwable cause) {
        super(message, cause);
    }
}
