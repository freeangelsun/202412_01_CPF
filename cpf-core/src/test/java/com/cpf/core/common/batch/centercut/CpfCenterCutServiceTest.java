package com.cpf.core.common.batch.centercut;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class CpfCenterCutServiceTest {

    @Test
    void executeKeepsOneTransactionIdAndCreatesPerItemSegments() {
        AtomicLong sequence = new AtomicLong();
        CpfCenterCutService service = new CpfCenterCutService(() ->
                "CC-20260701120000000-" + String.format("%07d", sequence.incrementAndGet()));
        MemoryProvider provider = new MemoryProvider(List.of(
                target("T001", "ORDER-001"),
                target("T002", "ORDER-002")));

        CpfCenterCutSummary summary = service.execute(
                "CPF_CENTER_CUT_SAMPLE_JOB",
                10,
                provider,
                target -> CpfCenterCutResult.success(target, "처리 완료", "{\"ok\":true}"));

        assertThat(summary.requestedCount()).isEqualTo(2);
        assertThat(summary.successCount()).isEqualTo(2);
        assertThat(provider.runningTargetIds).containsExactly("T001", "T002");
        assertThat(provider.runningTargets)
                .extracting(CpfCenterCutTarget::transactionId)
                .containsOnly("20260701115959000BATbatWK010000001");
        assertThat(provider.runningTargets)
                .extracting(CpfCenterCutTarget::parentSegmentId)
                .containsOnly("SEG-BAT-PARENT-0001");
        assertThat(provider.results)
                .extracting(CpfCenterCutResult::transactionSegmentId)
                .containsExactly("CC-20260701120000000-0000001", "CC-20260701120000000-0000002");
    }

    @Test
    void executeConvertsHandlerExceptionToFailedResultWithoutChangingTransactionId() {
        CpfCenterCutService service = new CpfCenterCutService(() -> "CC-20260701120000000-0000001");
        MemoryProvider provider = new MemoryProvider(List.of(target("T001", "ORDER-001")));

        CpfCenterCutSummary summary = service.execute(
                "CPF_CENTER_CUT_SAMPLE_JOB",
                10,
                provider,
                target -> {
                    throw new IllegalStateException("업무 처리 실패");
                });

        assertThat(summary.failedCount()).isEqualTo(1);
        assertThat(provider.results).hasSize(1);
        assertThat(provider.results.get(0).status()).isEqualTo(CpfCenterCutStatus.FAILED);
        assertThat(provider.results.get(0).message()).isEqualTo("업무 처리 실패");
        assertThat(provider.runningTargets.get(0).transactionId())
                .isEqualTo("20260701115959000BATbatWK010000001");
    }

    private static CpfCenterCutTarget target(String targetId, String businessKey) {
        return new CpfCenterCutTarget(
                targetId,
                "CPF_CENTER_CUT_SAMPLE_JOB",
                businessKey,
                LocalDate.of(2026, 7, 1),
                "{\"businessKey\":\"" + businessKey + "\"}",
                "20260701115959000BATbatWK010000001",
                "SEG-BAT-PARENT-0001",
                null,
                0,
                CpfCenterCutStatus.READY);
    }

    private static final class MemoryProvider implements CenterCutTargetProvider {
        private final List<CpfCenterCutTarget> targets;
        private final List<String> runningTargetIds = new ArrayList<>();
        private final List<CpfCenterCutTarget> runningTargets = new ArrayList<>();
        private final List<CpfCenterCutResult> results = new ArrayList<>();

        private MemoryProvider(List<CpfCenterCutTarget> targets) {
            this.targets = targets;
        }

        @Override
        public List<CpfCenterCutTarget> findReadyTargets(String centerCutJobId, int limit) {
            return targets.stream()
                    .filter(target -> centerCutJobId.equals(target.centerCutJobId()))
                    .limit(limit)
                    .toList();
        }

        @Override
        public void markRunning(CpfCenterCutTarget target) {
            runningTargetIds.add(target.targetId());
            runningTargets.add(target);
        }

        @Override
        public void markResult(CpfCenterCutTarget target, CpfCenterCutResult result) {
            results.add(result);
        }
    }
}
