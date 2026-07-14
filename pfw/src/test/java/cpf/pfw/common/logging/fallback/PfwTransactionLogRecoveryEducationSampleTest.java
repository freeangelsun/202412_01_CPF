package cpf.pfw.common.logging.fallback;

import cpf.pfw.common.logging.TransactionLogRecord;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PfwTransactionLogRecoveryEducationSampleTest {

    @Test
    void delegatesPreserveStatusAndRecoveryToPfwCapability() {
        TransactionLogFallbackStore store = mock(TransactionLogFallbackStore.class);
        TransactionLogRecoveryWorker worker = mock(TransactionLogRecoveryWorker.class);
        TransactionLogFallbackStore.FallbackSnapshot snapshot = new TransactionLogFallbackStore.FallbackSnapshot(
                1, 0, 0, 100, 1_000, 0, "local/acc/acc-01/recovery/transaction-db");
        TransactionLogRecoveryWorker.RecoveryResult result = new TransactionLogRecoveryWorker.RecoveryResult(
                1, 1, 0, false, snapshot);
        when(store.enqueue(any(), any(), any(), any())).thenReturn(true);
        when(store.snapshot()).thenReturn(snapshot);
        when(worker.recoverPending()).thenReturn(result);
        PfwTransactionLogRecoveryEducationSample sample = new PfwTransactionLogRecoveryEducationSample(store, worker);

        assertThat(sample.preserveDatabaseFailure(
                TransactionLogRecord.builder().transactionId("GLOBAL-1").build(),
                Map.of("request", "masked"),
                null,
                new IllegalStateException("DB down"))).isTrue();
        assertThat(sample.status()).isSameAs(snapshot);
        assertThat(sample.recoverNow("DB 연결 복구 후 재적재")).isSameAs(result);
        verify(worker).recoverPending();
    }

    @Test
    void rejectsManualRecoveryWithoutAuditReason() {
        PfwTransactionLogRecoveryEducationSample sample = new PfwTransactionLogRecoveryEducationSample(
                mock(TransactionLogFallbackStore.class),
                mock(TransactionLogRecoveryWorker.class));

        assertThatThrownBy(() -> sample.recoverNow(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("감사 사유");
    }
}
