package com.cpf.notification.dispatch;

import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;
import com.cpf.notification.spi.CpfNotificationProvider;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfNotificationWorkerUnknownResultTest {
    @Test
    void unknownResultIsNotCompletedAsSent() {
        JdbcCpfNotificationOutbox outbox = mock(JdbcCpfNotificationOutbox.class);
        CpfNotificationRequest request = new CpfNotificationRequest(
                "N-1", "email", "masked@example.com", "T-1", Map.of(), "I-1", null);
        CpfNotificationContextCodec codec = codec();
        String lineage = captureLineage(codec, "TX-1");
        when(outbox.claimWithContext("w", 10, Instant.parse("2026-08-02T00:00:00Z"), Duration.ofSeconds(30)))
                .thenReturn(List.of(new JdbcCpfNotificationOutbox.ClaimedNotification(request, lineage, 0)));
        CpfNotificationProvider provider = mock(CpfNotificationProvider.class);
        when(provider.channel()).thenReturn("email");
        Instant now = Instant.parse("2026-08-02T00:00:00Z");
        when(provider.send(request)).thenReturn(CpfNotificationResult.unknown("N-1", "email", "timeout", now));
        var properties = new CpfNotificationProperties(
                true, "w", 10, Duration.ofSeconds(30), Duration.ofSeconds(30), Duration.ofMinutes(1));
        var worker = new CpfNotificationWorker(
                outbox, List.of(provider), new CpfNotificationPreferencePolicy(), properties,
                Clock.fixed(now, ZoneOffset.UTC), codec);

        worker.runOnce("w", 10);

        verify(outbox).markUnknown(
                CpfNotificationResult.unknown("N-1", "email", "timeout", now),
                Instant.parse("2026-08-02T00:01:00Z"));
        verify(outbox, never()).complete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void providerExceptionIsUnknownAndNeverBlindRetried() {
        JdbcCpfNotificationOutbox outbox = mock(JdbcCpfNotificationOutbox.class);
        CpfNotificationRequest request = new CpfNotificationRequest(
                "N-2", "email", "masked@example.com", "T-1", Map.of(), "I-2", null);
        Instant now = Instant.parse("2026-08-02T00:00:00Z");
        CpfNotificationContextCodec codec = codec();
        String lineage = captureLineage(codec, "TX-2");
        when(outbox.claimWithContext("w", 10, now, Duration.ofSeconds(30)))
                .thenReturn(List.of(new JdbcCpfNotificationOutbox.ClaimedNotification(request, lineage, 0)));
        CpfNotificationProvider provider = mock(CpfNotificationProvider.class);
        when(provider.channel()).thenReturn("email");
        when(provider.send(request)).thenThrow(new IllegalStateException("socket closed"));
        var properties = new CpfNotificationProperties(
                true, "w", 10, Duration.ofSeconds(30), Duration.ofSeconds(30), Duration.ofMinutes(1));
        var worker = new CpfNotificationWorker(
                outbox, List.of(provider), new CpfNotificationPreferencePolicy(), properties,
                Clock.fixed(now, ZoneOffset.UTC), codec);

        worker.runOnce("w", 10);

        verify(outbox).markUnknown(
                CpfNotificationResult.unknown("N-2", "email", "socket closed", now),
                Instant.parse("2026-08-02T00:01:00Z"));
        verify(outbox, never()).retry(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        verify(outbox, never()).complete(org.mockito.ArgumentMatchers.any());
    }

    private static CpfNotificationContextCodec codec() {
        return new CpfNotificationContextCodec(factory("TX-TEST"));
    }

    private static CpfContextExecutionFactory factory(String transactionId) {
        return new CpfContextExecutionFactory(() -> transactionId, new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "EX-" + java.util.UUID.randomUUID(); }
            public String newSegmentId() { return "SG-" + java.util.UUID.randomUUID(); }
        }, () -> LocalDate.of(2026, 8, 2),
                Clock.fixed(Instant.parse("2026-08-02T00:00:00Z"), ZoneOffset.UTC));
    }

    private static String captureLineage(CpfNotificationContextCodec codec, String transactionId) {
        CpfContextExecutionFactory factory = factory(transactionId);
        CpfContext root = factory.newRoot(null, "notification.test", null, null,
                Instant.parse("2026-08-02T00:01:00Z"));
        try (AutoCloseable ignored = CpfContexts.bind(CpfContextSnapshot.capture(root))) {
            return codec.capture();
        } catch (RuntimeException runtime) {
            throw runtime;
        } catch (Exception checked) {
            throw new IllegalStateException(checked);
        } finally {
            if (CpfContexts.current() != null) throw new AssertionError("CPF context leak");
        }
    }
}
