package com.cpf.batch.centercut.runner;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.spi.CenterCutTargetProvider;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CenterCutRuntimeStateProviderTest {
    private final CenterCutTargetProvider targetProvider = mock(CenterCutTargetProvider.class);

    @Test
    void validBatStoreAndConcreteTargetProviderAreReady() throws Exception {
        DataSource dataSource = validDataSource();
        CenterCutRuntimeStateProvider provider =
                new CenterCutRuntimeStateProvider(dataSource, List.of(targetProvider));

        assertThat(provider.actualState()).isEqualTo(ActualState.READY);
        assertThat(provider.ready()).isTrue();
        assertThat(provider.availableCapacity()).isEqualTo(1);
        assertThat(provider.dependencyHealth())
                .containsEntry("batDataSource", "UP")
                .containsEntry("centerCutTargetProviders", "UP");
        assertThat(provider.metrics()).containsEntry("targetProviderCount", 1);
        assertThat(provider.lastErrorCode()).isNull();
    }

    @Test
    void missingTargetProviderFailsClosedEvenWhenBatStoreIsAvailable() throws Exception {
        CenterCutRuntimeStateProvider provider =
                new CenterCutRuntimeStateProvider(validDataSource(), List.of());

        assertThat(provider.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(provider.ready()).isFalse();
        assertThat(provider.availableCapacity()).isZero();
        assertThat(provider.dependencyHealth()).containsEntry("centerCutTargetProviders", "DOWN");
        assertThat(provider.lastErrorCode())
                .isEqualTo("BAT_CENTER_CUT_TARGET_PROVIDER_NOT_CONFIGURED");
    }

    @Test
    void unavailableBatStoreFailsClosedWithProviderPresent() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("database unavailable"));
        CenterCutRuntimeStateProvider provider =
                new CenterCutRuntimeStateProvider(dataSource, List.of(targetProvider));

        assertThat(provider.actualState()).isEqualTo(ActualState.DEGRADED);
        assertThat(provider.ready()).isFalse();
        assertThat(provider.dependencyHealth()).containsEntry("batDataSource", "DOWN");
        assertThat(provider.lastErrorCode()).contains("SQLEXCEPTION");
    }

    private DataSource validDataSource() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        return dataSource;
    }
}
