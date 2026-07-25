package com.cpf.batch.job.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.batch.runtime.centercut.BatCenterCutService;
import com.cpf.core.api.centercut.CpfCenterCutStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutSampleTargetProviderTest {

    @Test
    void sampleProviderSupportsSingleTransactionAndPerItemSegments() {
        AtomicLong sequence = new AtomicLong();
        BatCenterCutService service = new BatCenterCutService(() ->
                "CC-BAT-20260701120000000-" + String.format("%07d", sequence.incrementAndGet()));
        BatCenterCutSampleTargetProvider provider = new BatCenterCutSampleTargetProvider();
        BatCenterCutSampleHandler handler = new BatCenterCutSampleHandler();

        var summary = service.execute("CPF_BAT_CENTER_CUT_JOB", 10, provider, handler);

        assertThat(summary.requestedCount()).isEqualTo(3);
        assertThat(summary.successCount()).isEqualTo(3);

        @SuppressWarnings("unchecked")
        List<CpfCenterCutResult> results = (List<CpfCenterCutResult>) provider.snapshot().get("results");
        assertThat(results)
                .hasSize(3)
                .allSatisfy(result -> {
                    assertThat(result.status()).isEqualTo(CpfCenterCutStatus.SUCCESS);
                    assertThat(result.transactionSegmentId()).startsWith("CC-BAT-");
                });
    }
}
