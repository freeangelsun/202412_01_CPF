package cpf.pfw.common.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfTransactionContextAnomalyMonitorTest {

    @BeforeEach
    void resetCounter() {
        CpfTransactionContextAnomalyMonitor.resetForTest();
    }

    @Test
    void missingTransactionContextIncrementsDiagnosticCounter() {
        assertThat(CpfTransactionContextAnomalyMonitor.recordMissing("BATCH_WORKER")).isEqualTo(1L);
        assertThat(CpfTransactionContextAnomalyMonitor.recordMissing("ONLINE_CONTROLLER")).isEqualTo(2L);
        assertThat(CpfTransactionContextAnomalyMonitor.missingCount()).isEqualTo(2L);
    }
}
