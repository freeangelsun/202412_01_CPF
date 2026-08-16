package com.cpf.batch.control.deploy;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeploymentCellLockTest {
    @Test
    void differentOwnerIsReportedAsContention() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = catalog();
        when(jdbc.queryForObject("LOCK_OWNER", String.class, "cell-a")).thenReturn("deployment-b");

        DeploymentCellLock lock = new DeploymentCellLock(jdbc, sql);

        assertThat(lock.acquire("cell-a", "deployment-a"))
                .isEqualTo(DeploymentCellLock.Acquisition.CONTENDED);
    }

    @Test
    void storeFailureIsNeverConvertedToContention() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = catalog();
        when(jdbc.update("LOCK_ACQUIRE", "cell-a", "deployment-a"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));

        DeploymentCellLock lock = new DeploymentCellLock(jdbc, sql);

        assertThatThrownBy(() -> lock.acquire("cell-a", "deployment-a"))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("database unavailable");
    }

    private static CpfVendorSqlCatalog catalog() {
        CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
        when(sql.required("deploy-lock-acquire")).thenReturn("LOCK_ACQUIRE");
        when(sql.required("deploy-lock-owner")).thenReturn("LOCK_OWNER");
        when(sql.required("deploy-lock-release")).thenReturn("LOCK_RELEASE");
        return sql;
    }
}
