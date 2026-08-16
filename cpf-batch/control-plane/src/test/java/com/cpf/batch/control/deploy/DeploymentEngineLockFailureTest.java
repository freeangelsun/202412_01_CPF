package com.cpf.batch.control.deploy;

import com.cpf.batch.api.ArtifactManifest;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.api.DeploymentRequest;
import com.cpf.batch.spi.RuntimeHealthProbe;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeploymentEngineLockFailureTest {
    @Test
    void lockStoreFailureBecomesUnknownResultNotLockContention() {
        DeploymentCellManifest manifest = mock(DeploymentCellManifest.class);
        ArtifactManifest artifact = mock(ArtifactManifest.class);
        when(manifest.cellId()).thenReturn("cell-a");
        when(manifest.artifact()).thenReturn(artifact);
        when(manifest.environment()).thenReturn("local");
        when(manifest.deployment()).thenReturn(mock(DeploymentCellManifest.DeploymentPolicy.class));
        when(artifact.version()).thenReturn("1.0.0");
        DeploymentRequest request = new DeploymentRequest(
                "deployment-a",
                "idempotency-a",
                manifest,
                0,
                "requester-a",
                "approved deployment",
                "approval-a",
                "approver-a",
                Instant.now().plusSeconds(60));

        CompatibilityService compatibility = mock(CompatibilityService.class);
        when(compatibility.evaluate(artifact, "local"))
                .thenReturn(new CompatibilityService.Result(true, "COMPATIBLE"));
        DeploymentExecutionRepository executions = mock(DeploymentExecutionRepository.class);
        when(executions.begin(request)).thenReturn(Optional.empty());
        DeploymentCellLock lock = mock(DeploymentCellLock.class);
        when(lock.acquire("cell-a", "deployment-a"))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        DeploymentEngine engine = new DeploymentEngine(
                List.of(),
                mock(RuntimeHealthProbe.class),
                compatibility,
                mock(JdbcTemplate.class),
                executions,
                lock,
                mock(CpfVendorSqlCatalog.class));

        var result = engine.deploy(request);

        assertThat(result.state()).isEqualTo(CommandState.UNKNOWN_RESULT);
        assertThat(result.failureStage()).isEqualTo("DEPLOYMENT_LOCK_STORE");
        assertThat(result.message()).contains("DataAccessResourceFailureException");
        verify(executions).finish(
                "deployment-a",
                CommandState.UNKNOWN_RESULT,
                "DEPLOYMENT_LOCK_STORE",
                result.message());
    }
}
