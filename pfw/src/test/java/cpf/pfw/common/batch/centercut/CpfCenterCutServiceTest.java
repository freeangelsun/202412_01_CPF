package cpf.pfw.common.batch.centercut;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class CpfCenterCutServiceTest {

    @Test
    void executeMarksRunningAndStoresResultsWithChildTransactionId() {
        AtomicLong sequence = new AtomicLong();
        CpfCenterCutService service = new CpfCenterCutService(() -> "20260701120000000BATcentcut"
                + String.format("%07d", sequence.incrementAndGet()));
        MemoryProvider provider = new MemoryProvider(List.of(
                target("T001", "ORDER-001"),
                target("T002", "ORDER-002")));

        CpfCenterCutSummary summary = service.execute(
                "CPF_CENTER_CUT_SAMPLE_JOB",
                10,
                provider,
                target -> CpfCenterCutResult.success(target, "처리 완료", "{\"ok\":true}", target.childTransactionGlobalId()));

        assertThat(summary.requestedCount()).isEqualTo(2);
        assertThat(summary.successCount()).isEqualTo(2);
        assertThat(provider.runningTargetIds).containsExactly("T001", "T002");
        assertThat(provider.results)
                .extracting(CpfCenterCutResult::childTransactionGlobalId)
                .containsExactly("20260701120000000BATcentcut0000001", "20260701120000000BATcentcut0000002");
    }

    @Test
    void executeConvertsHandlerExceptionToFailedResult() {
        CpfCenterCutService service = new CpfCenterCutService(() -> "20260701120000000BATcentcut0000001");
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
    }

    private static CpfCenterCutTarget target(String targetId, String businessKey) {
        return new CpfCenterCutTarget(
                targetId,
                "CPF_CENTER_CUT_SAMPLE_JOB",
                businessKey,
                LocalDate.of(2026, 7, 1),
                "{\"businessKey\":\"" + businessKey + "\"}",
                "20260701115959000BATparent0000001",
                null,
                0,
                CpfCenterCutStatus.READY);
    }

    private static final class MemoryProvider implements CenterCutTargetProvider {
        private final List<CpfCenterCutTarget> targets;
        private final List<String> runningTargetIds = new ArrayList<>();
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
        public void markRunning(CpfCenterCutTarget target, String childTransactionGlobalId) {
            runningTargetIds.add(target.targetId());
        }

        @Override
        public void markResult(CpfCenterCutTarget target, CpfCenterCutResult result) {
            results.add(result);
        }
    }
}
