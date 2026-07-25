package com.cpf.core.api.centercut;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class CpfCenterCutContractTest {
    @Test void unknownResultIsExplicitAndNotRetryAlias() {
        CpfCenterCutTarget target = new CpfCenterCutTarget(
                "T1", "JOB1", "B1", LocalDate.of(2026,7,25), "{}",
                "TX", "P", "S", 0, CpfCenterCutStatus.RUNNING);
        CpfCenterCutResult result = CpfCenterCutResult.unknown(target, "timeout after send", null);
        assertThat(result.status()).isEqualTo(CpfCenterCutStatus.UNKNOWN_RESULT);
        assertThat(result.status()).isNotEqualTo(CpfCenterCutStatus.RETRY_REQUESTED);
    }
}
