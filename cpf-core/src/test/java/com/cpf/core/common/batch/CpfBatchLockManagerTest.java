package com.cpf.core.common.batch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfBatchLockManagerTest {

    @Test
    void removesExpiredLeaseBeforeNewOwnerAcquiresLock() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        CpfBatchLockManager manager = manager(jdbcTemplate);

        boolean acquired = manager.acquire("batch:file:JOB:1", "JOB", "logical-file", "owner-b", 30);

        assertThat(acquired).isTrue();
        verify(jdbcTemplate).update("DELETE FROM bat_lock WHERE expire_at <= CURRENT_TIMESTAMP(3)");
        verify(jdbcTemplate).update(anyString(), any(Object[].class));
    }

    @Test
    void rejectsConcurrentOwnerWhenUniqueLeaseAlreadyExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        CpfBatchLockManager manager = manager(jdbcTemplate);
        doThrow(new DuplicateKeyException("이미 다른 writer가 소유한 lease입니다."))
                .when(jdbcTemplate).update(anyString(), any(Object[].class));

        boolean acquired = manager.acquire("batch:file:JOB:1", "JOB", "logical-file", "owner-b", 30);

        assertThat(acquired).isFalse();
    }

    @SuppressWarnings("unchecked")
    private CpfBatchLockManager manager(JdbcTemplate jdbcTemplate) {
        ObjectProvider<JdbcTemplate> jdbcProvider = mock(ObjectProvider.class);
        ObjectProvider<DataSource> dataSourceProvider = mock(ObjectProvider.class);
        when(jdbcProvider.getIfAvailable()).thenReturn(jdbcTemplate);
        return new CpfBatchLockManager(jdbcProvider, dataSourceProvider);
    }
}
