package com.cpf.education.batch.support;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationBatchPolicyEducationSampleTest {
    private final EducationBatchPolicyEducationSample sample = new EducationBatchPolicyEducationSample();

    @Test
    void keepsRuntimeOwnershipInBatAndRequiresFencingForStaleWorkers() {
        var policy = sample.lockPolicy();

        assertThat(policy.owner()).isEqualTo("cpf-batch");
        assertThat(policy.fencing()).contains("stale worker", "fencing token");
        assertThat(policy.forcedRelease()).contains("ADM", "승인", "CpfBatchOperationsPort");
    }

    @Test
    void distinguishesRestartRerunAndUnknownResultReconciliation() {
        var policy = sample.restartPolicy();

        assertThat(policy.retryableStates()).containsExactly("FAILED", "STOPPED", "UNKNOWN_RESULT");
        assertThat(policy.restart()).contains("동일 JobInstance", "checkpoint");
        assertThat(policy.rerun()).contains("새 JobInstance");
        assertThat(policy.unknownResult()).contains("대사");
    }

    @Test
    void explainsSchedulerAndChunkLifecycleWithoutRuntimeImplementation() {
        var schedule = sample.schedulePolicy();
        var lifecycle = sample.lifecyclePolicy();

        assertThat(schedule.owner()).isEqualTo("cpf-batch-scheduler");
        assertThat(schedule.leadership()).contains("leader", "fencing token");
        assertThat(schedule.duplicatePrevention()).contains("멱등키");
        assertThat(lifecycle.transactionBoundary()).contains("rollback", "commit");
        assertThat(lifecycle.unknownResult()).contains("UNKNOWN_RESULT");
        assertThat(lifecycle.reconciliation()).contains("대사");
    }
}
