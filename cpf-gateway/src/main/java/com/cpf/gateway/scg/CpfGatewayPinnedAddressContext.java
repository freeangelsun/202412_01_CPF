package com.cpf.gateway.scg;

import java.net.InetAddress;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/** 요청 thread 안에서 검증한 hostname→IP 연결 identity를 전용 HTTP client에 전달합니다. */
final class CpfGatewayPinnedAddressContext {
    private static final ThreadLocal<Map<String, InetAddress>> CURRENT = new ThreadLocal<>();

    private CpfGatewayPinnedAddressContext() {}

    static <T> T call(String host, InetAddress address, Callable<T> action) throws Exception {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(address, "address");
        Objects.requireNonNull(action, "action");
        if (CURRENT.get() != null) {
            throw new IllegalStateException("GATEWAY_PIN_CONTEXT_ALREADY_ACTIVE");
        }
        CURRENT.set(Map.of(normalize(host), address));
        try {
            return action.call();
        } finally {
            CURRENT.remove();
        }
    }

    static InetAddress[] resolve(String host) throws java.net.UnknownHostException {
        Map<String, InetAddress> pins = CURRENT.get();
        InetAddress address = pins == null ? null : pins.get(normalize(host));
        if (address == null) {
            throw new java.net.UnknownHostException("Unapproved gateway DNS resolution: " + host);
        }
        return new InetAddress[] {address};
    }

    static boolean active() {
        return CURRENT.get() != null;
    }

    private static String normalize(String host) {
        String value = host.trim().toLowerCase(Locale.ROOT);
        while (value.endsWith(".")) value = value.substring(0, value.length() - 1);
        return value;
    }
}
