package com.cpf.integration.resilience.internal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.data.lock.spi.CpfLockStore;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

class JdbcCpfLockStoreContractTest {

    @Test
    void nextFenceCannotBeCalledOutsideTheStoreTransaction() {
        JdbcCpfLockStore store = new JdbcCpfLockStore(mock(JdbcTemplate.class), mock(TransactionTemplate.class));

        assertThrows(IllegalStateException.class, () -> store.nextFence("outside"));
    }

    @Test
    void missingShardSeedFailsClosedBeforeAWriterCanPersist() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        TransactionTemplate transactions = mock(TransactionTemplate.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactions.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.doInTransaction(transactionStatus);
        });
        when(jdbc.query(startsWith("SELECT shard_id"), org.mockito.ArgumentMatchers.<ResultSetExtractor<Object>>any(), anyInt()))
                .thenReturn(null);
        JdbcCpfLockStore store = new JdbcCpfLockStore(jdbc, transactions);

        assertThrows(IllegalStateException.class, () -> store.update("payment:42", ignored -> active("payment:42")));
    }

    @Test
    void invalidLockKeysAreRejectedBeforeDatabaseAccess() {
        JdbcCpfLockStore store = new JdbcCpfLockStore(mock(JdbcTemplate.class), mock(TransactionTemplate.class));

        assertThrows(IllegalArgumentException.class, () -> store.find(" "));
        assertThrows(IllegalArgumentException.class, () -> store.find("x".repeat(201)));
    }

    private static CpfLockStore.StoredLock active(String key) {
        return new CpfLockStore.StoredLock(
                key,
                "node-a",
                "request-a",
                1L,
                Instant.parse("2026-08-05T00:00:00Z"),
                Instant.parse("2026-08-05T00:00:30Z"),
                CpfLockManager.State.ACTIVE,
                "ACQUIRED",
                null);
    }
}
