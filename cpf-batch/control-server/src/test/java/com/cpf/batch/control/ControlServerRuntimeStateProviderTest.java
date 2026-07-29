package com.cpf.batch.control;

import com.cpf.batch.api.ActualState;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControlServerRuntimeStateProviderTest {
    @Test
    void unavailableDataSourceIsReportedAsDegradedNotReady() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("database unavailable"));
        ControlServerRuntimeStateProvider provider =
                new ControlServerRuntimeStateProvider(dataSource);

        assertThat(provider.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(provider.ready()).isFalse();
        assertThat(provider.availableCapacity()).isZero();
        assertThat(provider.dependencyHealth()).containsEntry("batDataSource", "DOWN");
        assertThat(provider.lastErrorCode()).contains("SQLEXCEPTION");
    }

    @Test
    void validDataSourceConnectionIsReportedAsReady() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        ControlServerRuntimeStateProvider provider =
                new ControlServerRuntimeStateProvider(dataSource);

        assertThat(provider.actualState()).isEqualTo(ActualState.READY);
        assertThat(provider.ready()).isTrue();
        assertThat(provider.availableCapacity()).isEqualTo(1);
        assertThat(provider.lastErrorCode()).isNull();
    }
}
