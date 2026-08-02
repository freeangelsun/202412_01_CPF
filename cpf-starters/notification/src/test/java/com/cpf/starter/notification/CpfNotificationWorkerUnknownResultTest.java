package com.cpf.starter.notification;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfNotificationWorkerUnknownResultTest {
    @Test
    void unknownResultIsNotCompletedAsSent() {
        JdbcCpfNotificationOutbox outbox = mock(JdbcCpfNotificationOutbox.class);
        CpfNotificationRequest request = new CpfNotificationRequest(
                "N-1", "email", "masked@example.com", "T-1", Map.of(), "I-1", "TX-1", null);
        when(outbox.claim("w", 10, Instant.parse("2026-08-02T00:00:00Z"), Duration.ofSeconds(30)))
                .thenReturn(List.of(request));
        CpfNotificationProvider provider = mock(CpfNotificationProvider.class);
        when(provider.channel()).thenReturn("email");
        when(provider.send(request)).thenReturn(CpfNotificationResult.unknown("N-1", "email", "timeout"));
        var properties = new CpfNotificationProperties(
                true, "w", 10, Duration.ofSeconds(30), Duration.ofSeconds(30), Duration.ofMinutes(1));
        var worker = new CpfNotificationWorker(
                outbox, List.of(provider), new CpfNotificationPreferencePolicy(), properties,
                Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"), ZoneOffset.UTC));

        worker.runOnce("w", 10);

        verify(outbox).markUnknown(
                CpfNotificationResult.unknown("N-1", "email", "timeout"),
                Instant.parse("2026-08-02T00:01:00Z"));
        verify(outbox, never()).complete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void providerExceptionIsUnknownAndNeverBlindRetried() {
        JdbcCpfNotificationOutbox outbox = mock(JdbcCpfNotificationOutbox.class);
        CpfNotificationRequest request = new CpfNotificationRequest(
                "N-2", "email", "masked@example.com", "T-1", Map.of(), "I-2", "TX-2", null);
        Instant now = Instant.parse("2026-08-02T00:00:00Z");
        when(outbox.claim("w", 10, now, Duration.ofSeconds(30)))
                .thenReturn(List.of(request));
        CpfNotificationProvider provider = mock(CpfNotificationProvider.class);
        when(provider.channel()).thenReturn("email");
        when(provider.send(request)).thenThrow(new IllegalStateException("socket closed"));
        var properties = new CpfNotificationProperties(
                true, "w", 10, Duration.ofSeconds(30), Duration.ofSeconds(30), Duration.ofMinutes(1));
        var worker = new CpfNotificationWorker(
                outbox, List.of(provider), new CpfNotificationPreferencePolicy(), properties,
                Clock.fixed(now, ZoneOffset.UTC));

        worker.runOnce("w", 10);

        verify(outbox).markUnknown(
                CpfNotificationResult.unknown("N-2", "email", "socket closed"),
                Instant.parse("2026-08-02T00:01:00Z"));
        verify(outbox, never()).retry(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        verify(outbox, never()).complete(org.mockito.ArgumentMatchers.any());
    }
}
