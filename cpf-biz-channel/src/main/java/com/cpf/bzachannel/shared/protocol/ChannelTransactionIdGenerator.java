package com.cpf.bzachannel.shared.protocol;

import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/** Standalone implementation of the public CPF 34-character transaction-id wire format. */
public final class ChannelTransactionIdGenerator {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final Clock clock;
    private final String issuer;
    private final String instanceToken;
    private final AtomicLong sequence = new AtomicLong();
    private volatile LocalDate sequenceDate;

    public ChannelTransactionIdGenerator(String issuer, String instanceId, Clock clock) {
        this.issuer = requireIssuer(issuer);
        this.instanceToken = instanceToken(instanceId);
        this.clock = clock;
    }

    public static ChannelTransactionIdGenerator runtime(String issuer) {
        return new ChannelTransactionIdGenerator(issuer, runtimeHostName(), Clock.systemDefaultZone());
    }

    public synchronized String next() {
        LocalDateTime now = LocalDateTime.now(clock);
        if (!now.toLocalDate().equals(sequenceDate)) {
            sequenceDate = now.toLocalDate();
            sequence.set(0L);
        }
        long n = sequence.incrementAndGet();
        if (n > 9_999_999L) throw new IllegalStateException("daily transaction-id sequence exhausted");
        return now.format(TS) + issuer + instanceToken + String.format("%07d", n);
    }

    private static String runtimeHostName() {
        String explicit = System.getenv("BZA_CHANNEL_INSTANCE_ID");
        if (explicit != null && !explicit.isBlank()) return explicit.trim();
        try {
            String host = InetAddress.getLocalHost().getHostName();
            if (host != null && !host.isBlank()) return host;
        } catch (Exception ignored) { }
        throw new IllegalStateException("BZA Channel runtime hostname is unavailable; set BZA_CHANNEL_INSTANCE_ID");
    }

    private static String requireIssuer(String value) {
        String v = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!v.matches("[A-Z0-9]{3}")) throw new IllegalArgumentException("issuer must be 3 characters");
        return v;
    }

    private static String instanceToken(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("instanceId required");
        String normalized = value.trim().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!normalized.isBlank() && normalized.length() <= 7) return normalized + "0".repeat(7 - normalized.length());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.trim().getBytes(StandardCharsets.UTF_8));
            String token = new BigInteger(1, digest).mod(BigInteger.valueOf(36L).pow(7)).toString(36).toUpperCase(Locale.ROOT);
            return "0".repeat(7 - token.length()) + token;
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
