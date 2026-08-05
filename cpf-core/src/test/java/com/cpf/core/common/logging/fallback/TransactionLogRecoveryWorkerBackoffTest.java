package com.cpf.core.common.logging.fallback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TransactionLogRecoveryWorkerBackoffTest {

    @Test
    void appliesExponentialBackoffAndSaturatesOnOverflow() {
        assertThat(TransactionLogRecoveryWorker.retryDelayMs(1_000L, 300_000L, 1)).isEqualTo(1_000L);
        assertThat(TransactionLogRecoveryWorker.retryDelayMs(1_000L, 300_000L, 4)).isEqualTo(8_000L);
        assertThat(TransactionLogRecoveryWorker.retryDelayMs(Long.MAX_VALUE / 4L, Long.MAX_VALUE, 5))
                .isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void rejectsInvalidBackoffBounds() {
        assertThatThrownBy(() -> TransactionLogRecoveryWorker.retryDelayMs(2L, 1L, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TransactionLogRecoveryWorker.retryDelayMs(1L, 2L, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
