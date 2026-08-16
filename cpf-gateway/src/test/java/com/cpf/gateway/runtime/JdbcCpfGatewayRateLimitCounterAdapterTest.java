package com.cpf.gateway.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcCpfGatewayRateLimitCounterAdapterTest {
    @Test
    void reportsDistributedAndDoesNotOpenTransactionForEmptyBatch() {
        JdbcCpfGatewayRateLimitCounterAdapter adapter = new JdbcCpfGatewayRateLimitCounterAdapter(
                mock(JdbcTemplate.class), mock(PlatformTransactionManager.class));

        var result = adapter.consumeAtomically(java.util.List.of());

        assertThat(adapter.distributed()).isTrue();
        assertThat(result.accepted()).isTrue();
        assertThat(result.results()).isEmpty();
    }

    @Test
    void healthFailsClosedWhenJdbcProbeFails() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new IllegalStateException("db unavailable"));
        JdbcCpfGatewayRateLimitCounterAdapter adapter = new JdbcCpfGatewayRateLimitCounterAdapter(
                jdbc, mock(PlatformTransactionManager.class));

        var health = adapter.health();

        assertThat(health.ready()).isFalse();
        assertThat(health.status()).isEqualTo("DOWN");
    }
    @Test
    void rejectsInvalidAtomicBatchesBeforeOpeningATransaction() {
        JdbcCpfGatewayRateLimitCounterAdapter adapter = new JdbcCpfGatewayRateLimitCounterAdapter(
                mock(JdbcTemplate.class), mock(PlatformTransactionManager.class));
        var first = command("scope", "request-a", 1L, 5_000L);

        assertThatThrownBy(() -> adapter.consumeAtomically(List.of(
                first, command("scope", "request-b", 1L, 5_000L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate counter row");
        assertThatThrownBy(() -> adapter.consumeAtomically(List.of(
                first, command("other", "request-b", 2L, 5_000L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("one policy version");
        assertThatThrownBy(() -> adapter.consumeAtomically(List.of(
                first, command("other", "request-b", 1L, 5_001L))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("one observation time");

        List<com.cpf.gateway.api.CpfGatewayRateLimitCounterPort.CounterCommand> tooMany =
                new ArrayList<>();
        for (int index = 0; index < 17; index++) {
            tooMany.add(command("scope-" + index, "request-" + index, 1L, 5_000L));
        }
        assertThatThrownBy(() -> adapter.consumeAtomically(tooMany))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("too many atomic");
    }

    @Test
    void requestJournalHashRejectsSameRequestWithDifferentPayload() {
        var original = command("scope", "request", 1L, 5_000L);
        String hash = JdbcCpfGatewayRateLimitCounterAdapter.requestHash(original);

        JdbcCpfGatewayRateLimitCounterAdapter.validateJournalHash(original, hash);
        var changed = new com.cpf.gateway.api.CpfGatewayRateLimitCounterPort.CounterCommand(
                original.policyVersion(), original.counterKey(), original.requestId(),
                original.windowStartEpochMillis(), original.windowMillis(), original.quota(),
                original.burst(), 2, original.abuseThreshold(), original.blockMillis(),
                original.nowEpochMillis());

        assertThat(JdbcCpfGatewayRateLimitCounterAdapter.requestHash(changed)).isNotEqualTo(hash);
        assertThatThrownBy(() ->
                JdbcCpfGatewayRateLimitCounterAdapter.validateJournalHash(changed, hash))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("payload conflict");
    }

    @Test
    void blockedDecisionUsesTheLongerBlockDeadlineForRetryAfter() {
        var command = command("scope", "request", 1L, 5_000L);
        var row = new JdbcCpfGatewayRateLimitCounterAdapter.CounterRow(
                1L, 3, 120_000L, 7L, 60_000L);

        var result = JdbcCpfGatewayRateLimitCounterAdapter.evaluate(command, row);

        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).isEqualTo("ABUSE_BLOCKED");
        assertThat(result.resetAtEpochMillis()).isEqualTo(120_000L);
    }


    @Test
    void carriesAnActiveAbuseBlockAcrossCounterWindows() {
        var current = new JdbcCpfGatewayRateLimitCounterAdapter.CounterRow(
                0L, 0, 0L, 1L, 120_000L);

        var carried = JdbcCpfGatewayRateLimitCounterAdapter.carryForwardActiveBlock(
                current, List.of(90_000L, 180_000L));

        assertThat(carried.blockedUntil()).isEqualTo(180_000L);
        var command = new com.cpf.gateway.api.CpfGatewayRateLimitCounterPort.CounterCommand(
                1L, "scope", "request", 60_000L, 60_000L,
                10, 0, 1, 2, 120_000L, 65_000L);
        var result = JdbcCpfGatewayRateLimitCounterAdapter.evaluate(command, carried);
        assertThat(result.accepted()).isFalse();
        assertThat(result.reason()).isEqualTo("ABUSE_BLOCKED");
        assertThat(result.resetAtEpochMillis()).isEqualTo(180_000L);
    }

    @Test
    void rejectsCorruptActiveAbuseBlockRows() {
        var current = new JdbcCpfGatewayRateLimitCounterAdapter.CounterRow(
                0L, 0, 0L, 1L, 120_000L);

        assertThatThrownBy(() -> JdbcCpfGatewayRateLimitCounterAdapter.carryForwardActiveBlock(
                current, java.util.Arrays.asList(100_000L, null)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid active");
    }

    private static com.cpf.gateway.api.CpfGatewayRateLimitCounterPort.CounterCommand command(
            String key, String requestId, long policyVersion, long now) {
        return new com.cpf.gateway.api.CpfGatewayRateLimitCounterPort.CounterCommand(
                policyVersion, key, requestId, 0L, 60_000L, 10, 0, 1, 2, 120_000L, now);
    }

}
