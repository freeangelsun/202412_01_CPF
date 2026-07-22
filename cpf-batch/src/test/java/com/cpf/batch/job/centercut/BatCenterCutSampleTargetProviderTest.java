package com.cpf.batch.job.centercut;

import com.cpf.core.common.batch.centercut.CpfCenterCutResult;
import com.cpf.core.common.batch.centercut.CpfCenterCutService;
import com.cpf.core.common.batch.centercut.CpfCenterCutStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class BatCenterCutSampleTargetProviderTest {

    @Test
    void sampleProviderSupportsCenterCutExecutionFlow() {
        AtomicLong sequence = new AtomicLong();
        CpfCenterCutService service = new CpfCenterCutService(() -> "20260701120000000BATcentcut"
                + String.format("%07d", sequence.incrementAndGet()));
        BatCenterCutSampleTargetProvider provider = new BatCenterCutSampleTargetProvider();
        BatCenterCutSampleHandler handler = new BatCenterCutSampleHandler();

        var summary = service.execute("CPF_BAT_CENTER_CUT_JOB", 10, provider, handler);

        assertThat(summary.requestedCount()).isEqualTo(3);
        assertThat(summary.successCount()).isEqualTo(3);
        @SuppressWarnings("unchecked")
        List<CpfCenterCutResult> results = (List<CpfCenterCutResult>) provider.snapshot().get("results");
        assertThat(results)
                .hasSize(3)
                .allSatisfy(result -> assertThat(result.status()).isEqualTo(CpfCenterCutStatus.SUCCESS));
    }
}
