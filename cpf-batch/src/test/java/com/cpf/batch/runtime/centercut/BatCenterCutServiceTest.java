package com.cpf.batch.runtime.centercut;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutServiceTest {

    @Test
    void executeKeepsOneTransactionIdAndCreatesPerItemSegments() {
        AtomicLong sequence = new AtomicLong();
        BatCenterCutService service = new BatCenterCutService(() ->
                "CC-20260701120000000-" + String.format("%07d", sequence.incrementAndGet()));
        MemoryProvider provider = new MemoryProvider(List.of(
                target("T001", "ORDER-001"),
                target("T002", "ORDER-002")));

        com.cpf.core.api.centercut.CpfCenterCutSummary summary = service.execute(
                "CPF_CENTER_CUT_SAMPLE_JOB",
                10,
                provider,
                target -> com.cpf.core.api.centercut.CpfCenterCutResult.success(target, "처리 완료", "{\"ok\":true}"));

        assertThat(summary.requestedCount()).isEqualTo(2);
        assertThat(summary.successCount()).isEqualTo(2);
        assertThat(provider.runningTargetIds).containsExactly("T001", "T002");
        assertThat(provider.runningTargets)
                .extracting(com.cpf.core.api.centercut.CpfCenterCutTarget::transactionId)
                .containsOnly("20260701115959000BATbatWK010000001");
        assertThat(provider.runningTargets)
                .extracting(com.cpf.core.api.centercut.CpfCenterCutTarget::parentSegmentId)
                .containsOnly("SEG-BAT-PARENT-0001");
        assertThat(provider.results)
                .extracting(com.cpf.core.api.centercut.CpfCenterCutResult::transactionSegmentId)
                .containsExactly("CC-20260701120000000-0000001", "CC-20260701120000000-0000002");
    }

    @Test
    void executeConvertsHandlerExceptionToFailedResultWithoutChangingTransactionId() {
        BatCenterCutService service = new BatCenterCutService(() -> "CC-20260701120000000-0000001");
        MemoryProvider provider = new MemoryProvider(List.of(target("T001", "ORDER-001")));

        com.cpf.core.api.centercut.CpfCenterCutSummary summary = service.execute(
                "CPF_CENTER_CUT_SAMPLE_JOB",
                10,
                provider,
                target -> {
                    throw new IllegalStateException("업무 처리 실패");
                });

        assertThat(summary.failedCount()).isEqualTo(1);
        assertThat(provider.results).hasSize(1);
        assertThat(provider.results.get(0).status()).isEqualTo(com.cpf.core.api.centercut.CpfCenterCutStatus.FAILED);
        assertThat(provider.results.get(0).message()).isEqualTo("업무 처리 실패");
        assertThat(provider.runningTargets.get(0).transactionId())
                .isEqualTo("20260701115959000BATbatWK010000001");
    }

    private static com.cpf.core.api.centercut.CpfCenterCutTarget target(String targetId, String businessKey) {
        return new com.cpf.core.api.centercut.CpfCenterCutTarget(
                targetId,
                "CPF_CENTER_CUT_SAMPLE_JOB",
                businessKey,
                LocalDate.of(2026, 7, 1),
                "{\"businessKey\":\"" + businessKey + "\"}",
                "20260701115959000BATbatWK010000001",
                "SEG-BAT-PARENT-0001",
                null,
                0,
                com.cpf.core.api.centercut.CpfCenterCutStatus.READY);
    }

    private static final class MemoryProvider implements com.cpf.core.spi.centercut.CenterCutTargetProvider {
        private final List<com.cpf.core.api.centercut.CpfCenterCutTarget> targets;
        private final List<String> runningTargetIds = new ArrayList<>();
        private final List<com.cpf.core.api.centercut.CpfCenterCutTarget> runningTargets = new ArrayList<>();
        private final List<com.cpf.core.api.centercut.CpfCenterCutResult> results = new ArrayList<>();

        private MemoryProvider(List<com.cpf.core.api.centercut.CpfCenterCutTarget> targets) {
            this.targets = targets;
        }

        @Override
        public List<com.cpf.core.api.centercut.CpfCenterCutTarget> findReadyTargets(String centerCutJobId, int limit) {
            return targets.stream()
                    .filter(target -> centerCutJobId.equals(target.centerCutJobId()))
                    .limit(limit)
                    .toList();
        }

        @Override
        public void markRunning(com.cpf.core.api.centercut.CpfCenterCutTarget target) {
            runningTargetIds.add(target.targetId());
            runningTargets.add(target);
        }

        @Override
        public void markResult(com.cpf.core.api.centercut.CpfCenterCutTarget target, com.cpf.core.api.centercut.CpfCenterCutResult result) {
            results.add(result);
        }
    }
}
