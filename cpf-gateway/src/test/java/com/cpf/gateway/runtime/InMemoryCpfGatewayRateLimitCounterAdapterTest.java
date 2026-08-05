package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRateLimitCounterPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCpfGatewayRateLimitCounterAdapterTest {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:05Z");

    @Test
    void consumesCompositeScopesAtomicallyWithoutPartialMutation() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        var api = command("api", "one-api", 10);
        var client = command("client", "one-client", 1);
        assertTrue(adapter.consumeAtomically(List.of(api, client)).accepted());

        var denied = adapter.consumeAtomically(List.of(
                command("api", "two-api", 10),
                command("client", "two-client", 1)));
        assertFalse(denied.accepted());
        assertEquals(1, denied.limitingIndex());

        // 두 번째 복합 판정이 API counter를 부분 소비하지 않았음을 검증합니다.
        var apiOnly = adapter.consume(command("api", "three-api", 10));
        assertTrue(apiOnly.accepted());
        assertEquals(2L, apiOnly.used());
    }

    @Test
    void duplicateRequestReturnsOriginalDecisionWithoutConsumingAgain() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        var command = command("api", "same", 2);
        assertTrue(adapter.consume(command).accepted());
        var duplicate = adapter.consume(command);
        assertTrue(duplicate.accepted());
        assertTrue(duplicate.duplicate());
        assertEquals(1L, duplicate.used());
    }


    @Test
    void deniedCompositeDuplicateRemainsStableAfterOtherScopeChanges() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        assertTrue(adapter.consumeAtomically(List.of(
                command("api", "first-api", 2),
                command("client", "first-client", 1))).accepted());

        List<CpfGatewayRateLimitCounterPort.CounterCommand> deniedCommands = List.of(
                command("api", "denied-api", 2),
                command("client", "denied-client", 1));
        var denied = adapter.consumeAtomically(deniedCommands);
        assertFalse(denied.accepted());
        assertEquals(1, denied.limitingIndex());

        assertTrue(adapter.consume(command("api", "other-api", 2)).accepted());
        var duplicate = adapter.consumeAtomically(deniedCommands);
        assertFalse(duplicate.accepted());
        assertEquals(1, duplicate.limitingIndex());
        assertTrue(duplicate.results().stream().allMatch(
                CpfGatewayRateLimitCounterPort.CounterResult::duplicate));
    }

    @Test
    void sameAtomicRequestWithDifferentPayloadIsRejected() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        var original = command("api", "same-request", 10);
        assertTrue(adapter.consume(original).accepted());

        var samePayloadLater = new CpfGatewayRateLimitCounterPort.CounterCommand(
                original.policyVersion(), original.counterKey(), original.requestId(),
                original.windowStartEpochMillis(), original.windowMillis(), original.quota(),
                original.burst(), original.units(), original.abuseThreshold(), original.blockMillis(),
                original.nowEpochMillis() + 1_000L);
        assertTrue(adapter.consume(samePayloadLater).duplicate());

        var changed = new CpfGatewayRateLimitCounterPort.CounterCommand(
                original.policyVersion(), original.counterKey(), original.requestId(),
                original.windowStartEpochMillis(), original.windowMillis(), original.quota(),
                original.burst(), 2, original.abuseThreshold(), original.blockMillis(),
                original.nowEpochMillis());
        assertThrows(IllegalStateException.class, () -> adapter.consume(changed));
    }

    @Test
    void abuseThresholdBlocksSubsequentRequests() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        assertTrue(adapter.consume(command("api", "ok", 1, 2, 30_000L)).accepted());
        assertEquals("QUOTA_EXCEEDED", adapter.consume(command("api", "deny-1", 1, 2, 30_000L)).reason());
        assertEquals("ABUSE_BLOCKED", adapter.consume(command("api", "deny-2", 1, 2, 30_000L)).reason());
        assertEquals("ABUSE_BLOCKED", adapter.consume(command("api", "deny-3", 1, 2, 30_000L)).reason());
    }


    @Test
    void abuseBlockSurvivesWindowRolloverUntilItsOwnExpiry() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        long firstStart = NOW.toEpochMilli() - 5_000L;
        var allowed = new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, "client", "allowed", firstStart, 10_000L,
                1, 0, 1, 1, 30_000L, NOW.toEpochMilli());
        assertTrue(adapter.consume(allowed).accepted());
        var denied = new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, "client", "denied", firstStart, 10_000L,
                1, 0, 1, 1, 30_000L, NOW.toEpochMilli());
        assertEquals("ABUSE_BLOCKED", adapter.consume(denied).reason());

        long secondStart = firstStart + 10_000L;
        var rollover = new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, "client", "next-window", secondStart, 10_000L,
                1, 0, 1, 1, 30_000L, secondStart);
        var result = adapter.consume(rollover);

        assertFalse(result.accepted());
        assertEquals("ABUSE_BLOCKED", result.reason());
        assertTrue(result.blockedUntilEpochMillis() > secondStart);
    }

    @Test
    void deniedMultiPermitRequestReportsAvailableUnitsWithoutConsumingThem() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        assertTrue(adapter.consume(command("api", "first", 3, 2)).accepted());
        var denied = adapter.consume(command("api", "second", 3, 2));
        assertFalse(denied.accepted());
        assertEquals(1L, denied.remaining());
        assertEquals(2L, denied.used());
    }

    @Test
    void expiredDedupeEntryAllowsSameRequestIdInNextWindow() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed());
        long firstStart = NOW.toEpochMilli() - 5_000L;
        var first = new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, "api", "same", firstStart, 60_000L, 10, 0, 1, 0, 0L,
                NOW.toEpochMilli());
        assertTrue(adapter.consume(first).accepted());

        long secondStart = first.resetAtEpochMillis();
        var second = new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, "api", "same", secondStart, 60_000L, 10, 0, 1, 0, 0L,
                secondStart);
        var nextWindow = adapter.consume(second);
        assertTrue(nextWindow.accepted());
        assertFalse(nextWindow.duplicate());
        assertEquals(1L, nextWindow.used());
    }

    @Test
    void liveDedupeEntriesAreNeverEvictedToAdmitNewRequests() {
        var adapter = new InMemoryCpfGatewayRateLimitCounterAdapter(100, 2, fixed());
        assertTrue(adapter.consume(command("api", "one", 10)).accepted());
        assertTrue(adapter.consume(command("api", "two", 10)).accepted());
        var failure = assertThrows(IllegalStateException.class,
                () -> adapter.consume(command("api", "three", 10)));
        assertTrue(failure.getMessage().contains("dedupe capacity"));

        var duplicate = adapter.consume(command("api", "one", 10));
        assertTrue(duplicate.accepted());
        assertTrue(duplicate.duplicate());
        assertEquals(1L, duplicate.used());
    }

    private static CpfGatewayRateLimitCounterPort.CounterCommand command(
            String key, String requestId, int quota) {
        return command(key, requestId, quota, 0, 0L);
    }

    private static CpfGatewayRateLimitCounterPort.CounterCommand command(
            String key, String requestId, int quota, int units) {
        return new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, key, requestId, NOW.toEpochMilli() - 5_000L,
                60_000L, quota, 0, units, 0, 0L, NOW.toEpochMilli());
    }

    private static CpfGatewayRateLimitCounterPort.CounterCommand command(
            String key, String requestId, int quota, int abuseThreshold, long blockMillis) {
        return new CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, key, requestId, NOW.toEpochMilli() - 5_000L,
                60_000L, quota, 0, 1, abuseThreshold, blockMillis, NOW.toEpochMilli());
    }

    private static Clock fixed() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }
}
