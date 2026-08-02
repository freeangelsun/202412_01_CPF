package com.cpf.batch.control.deploy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.ArtifactManifest;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.api.DeploymentRequest;
import com.cpf.batch.api.DeploymentResult;
import com.cpf.batch.api.DeploymentStrategy;
import com.cpf.batch.api.DesiredState;
import com.cpf.batch.api.RuntimeRole;
import com.cpf.batch.spi.DeploymentTargetAdapter;
import com.cpf.batch.spi.RuntimeHealthProbe;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

/** 배포 Side Effect 원장, 결과불명 Lock 보존, 알려진 변경만 보상하는 상태기계를 검증합니다. */
class DeploymentEngineStateMachineTest {
    private static final DeploymentCellManifest.Instance INSTANCE = new DeploymentCellManifest.Instance(
            "i1", "w", "localhost", 8080, "p", "z", "pool", List.of(), "https://agent", "cfg");

    @Test
    void recordsSuccessfulStagesAndReleasesCellLock() {
        Adapter adapter = new Adapter();
        Fixture fixture = fixture(adapter, new Health(true));

        DeploymentResult result = fixture.engine.deploy(request());

        assertThat(result.state()).isEqualTo(CommandState.SUCCEEDED);
        ArgumentCaptor<DeploymentResult.InstanceResult> resultCaptor =
                ArgumentCaptor.forClass(DeploymentResult.InstanceResult.class);
        verify(fixture.repository, org.mockito.Mockito.times(4))
                .instance(eq("d"), anyInt(), resultCaptor.capture());
        assertThat(resultCaptor.getAllValues().stream().map(DeploymentResult.InstanceResult::stage).toList())
                .containsExactly("DRAIN", "INSTALL", "START", "ADMISSION");
        verify(fixture.lock).release("c", "d");
    }

    @Test
    void retainsLockAndDoesNotCompensateUnknownInstallResult() {
        Adapter adapter = new Adapter();
        adapter.installState = CommandState.UNKNOWN_RESULT;
        Fixture fixture = fixture(adapter, new Health(true));

        DeploymentResult result = fixture.engine.deploy(request());

        assertThat(result.state()).isEqualTo(CommandState.UNKNOWN_RESULT);
        verify(fixture.lock, never()).release(anyString(), anyString());
        assertThat(adapter.rollbackCalls).isZero();
        assertThat(adapter.resumeCalls).isZero();
    }

    @Test
    void compensatesOnlyKnownDrainAndInstallSideEffects() {
        Adapter adapter = new Adapter();
        Fixture fixture = fixture(adapter, new Health(false));

        DeploymentResult result = fixture.engine.deploy(request());

        assertThat(result.state()).isEqualTo(CommandState.ROLLED_BACK);
        assertThat(adapter.rollbackCalls).isEqualTo(1);
        assertThat(adapter.resumeCalls).isEqualTo(1);
        verify(fixture.lock).release("c", "d");
    }

    @Test
    void convertsCellLockReleaseFailureToUnknownResult() {
        Fixture fixture = fixture(new Adapter(), new Health(true));
        doThrow(new IllegalStateException("release failed")).when(fixture.lock).release("c", "d");

        DeploymentResult result = fixture.engine.deploy(request());

        assertThat(result.state()).isEqualTo(CommandState.UNKNOWN_RESULT);
        assertThat(result.failureStage()).isEqualTo("DEPLOYMENT_LOCK_RELEASE");
    }

    private static Fixture fixture(Adapter adapter, Health health) {
        DeploymentExecutionRepository repository = mock(DeploymentExecutionRepository.class);
        DeploymentCellLock lock = mock(DeploymentCellLock.class);
        CompatibilityService compatibility = mock(CompatibilityService.class);
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);

        when(repository.begin(any())).thenReturn(Optional.empty());
        when(sql.required(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(repository).instance(anyString(), anyInt(), any());
        doNothing().when(repository).finish(anyString(), any(), any(), any());
        when(lock.acquire("c", "d")).thenReturn(DeploymentCellLock.Acquisition.ACQUIRED);
        doNothing().when(lock).release("c", "d");
        when(compatibility.evaluate(any(), eq("prod"))).thenReturn(new CompatibilityService.Result(true, "COMPATIBLE"));
        when(jdbc.queryForObject(eq("deploy-runtime-healthy-count"), eq(Integer.class), eq("svc"))).thenReturn(1);

        DeploymentEngine engine = new DeploymentEngine(
                List.of(adapter), health, compatibility, jdbc, repository, lock, sql);
        return new Fixture(engine, repository, lock);
    }

    private static DeploymentRequest request() {
        ArtifactManifest artifact = new ArtifactManifest(
                "g:a", "1", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "sig", "sbom", "provenance", "git-sha", "25", "7.0", "1.0",
                "1.0", "compatible", List.of(), Instant.parse("2026-08-01T00:00:00Z"));
        DeploymentCellManifest.DeploymentPolicy policy = new DeploymentCellManifest.DeploymentPolicy(
                DeploymentStrategy.ROLLING, 0, 1, "/health", 10, 10, "0", false);
        DeploymentCellManifest manifest = new DeploymentCellManifest(
                "c", "prod", RuntimeRole.WORKER, "svc", artifact, "embedded-bootjar",
                List.of(INSTANCE), DesiredState.RUNNING, policy, List.of(), List.of(), Map.of());
        return new DeploymentRequest(
                "d", "k", manifest, 0, "req", "reason", "APR-1", "app", Instant.now().plusSeconds(60));
    }

    private record Fixture(
            DeploymentEngine engine, DeploymentExecutionRepository repository, DeploymentCellLock lock) {}

    private static final class Health implements RuntimeHealthProbe {
        private final boolean ready;

        private Health(boolean ready) {
            this.ready = ready;
        }

        @Override
        public RuntimeHealthProbe.Health probe(
                DeploymentCellManifest.Instance instance, String path, int timeoutSeconds) {
            return new RuntimeHealthProbe.Health(ready, ready, ready ? "ok" : "down");
        }
    }

    private static final class Adapter implements DeploymentTargetAdapter {
        private CommandState installState = CommandState.SUCCEEDED;
        private int rollbackCalls;
        private int resumeCalls;

        @Override
        public boolean supports(DeploymentCellManifest.Instance instance, String runtimeMode) {
            return true;
        }

        @Override
        public DeploymentResult.InstanceResult deploy(
                DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
            return result("INSTALL", installState);
        }

        @Override
        public DeploymentResult.InstanceResult rollback(
                DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
            rollbackCalls++;
            return result("ROLLBACK", CommandState.ROLLED_BACK);
        }

        @Override
        public DeploymentResult.InstanceResult start(
                DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
            return result("START", CommandState.SUCCEEDED);
        }

        @Override
        public DeploymentResult.InstanceResult stop(
                DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
            return result("STOP", CommandState.SUCCEEDED);
        }

        @Override
        public DeploymentResult.InstanceResult drain(
                DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
            return result("DRAIN", CommandState.SUCCEEDED);
        }

        @Override
        public DeploymentResult.InstanceResult resume(
                DeploymentCellManifest manifest, DeploymentCellManifest.Instance instance) {
            resumeCalls++;
            return result("ADMISSION", CommandState.SUCCEEDED);
        }

        private static DeploymentResult.InstanceResult result(String stage, CommandState state) {
            return new DeploymentResult.InstanceResult("i1", state, stage, stage);
        }
    }
}
