package com.cpf.batch.control.deploy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.ArtifactManifest;
import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.api.DeploymentRequest;
import com.cpf.batch.api.DeploymentStrategy;
import com.cpf.batch.api.DesiredState;
import com.cpf.batch.api.RuntimeRole;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/** 동일 Idempotency Key에 다른 immutable request를 재사용하는 것을 차단합니다. */
class DeploymentExecutionRepositoryIdempotencyTest {
    @Test
    void canonicalHashIsStableForSameRequestAndChangesForImmutablePayload() {
        DeploymentExecutionRepository repository = repository(mock(JdbcTemplate.class));

        String first = repository.requestHash(request("1.0.0", "APR-1"));
        String same = repository.requestHash(request("1.0.0", "APR-1"));
        String changed = repository.requestHash(request("2.0.0", "APR-1"));

        assertThat(first).isEqualTo(same).hasSize(64);
        assertThat(changed).isNotEqualTo(first);
    }

    @Test
    void duplicateIdempotencyKeyWithDifferentRequestFailsClosed() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        DeploymentExecutionRepository repository = repository(jdbc);
        DeploymentRequest request = request("2.0.0", "APR-1");
        when(jdbc.update(anyString(), any(Object[].class))).thenThrow(new DuplicateKeyException("duplicate"));
        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(List.of(Map.of(
                "request_hash", "0".repeat(64),
                "deployment_id", request.deploymentId(),
                "cell_id", request.manifest().cellId())));

        assertThatThrownBy(() -> repository.begin(request))
                .isInstanceOf(DeploymentIdempotencyConflictException.class)
                .hasMessageContaining("different deployment request");
    }

    private static DeploymentExecutionRepository repository(JdbcTemplate jdbc) {
        CpfVendorSqlCatalog sql = mock(CpfVendorSqlCatalog.class);
        when(sql.required(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        return new DeploymentExecutionRepository(jdbc, sql, new ObjectMapper());
    }

    private static DeploymentRequest request(String version, String approvalId) {
        ArtifactManifest artifact = new ArtifactManifest(
                "g:a", version, "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "sig", "sbom", "provenance", "git-sha", "25", "7.0", "1.0",
                "1.0", "compatible", List.of(), Instant.parse("2026-08-01T00:00:00Z"));
        DeploymentCellManifest.DeploymentPolicy policy = new DeploymentCellManifest.DeploymentPolicy(
                DeploymentStrategy.ROLLING, 0, 1, "/health", 10, 10, "0", false);
        DeploymentCellManifest manifest = new DeploymentCellManifest(
                "c", "prod", RuntimeRole.WORKER, "svc", artifact, "embedded-bootjar",
                List.of(new DeploymentCellManifest.Instance(
                        "i1", "w", "localhost", 8080, "p", "z", "pool", List.of(), "https://agent", "cfg")),
                DesiredState.RUNNING, policy, List.of(), List.of(), Map.of());
        return new DeploymentRequest(
                "d", "idem", manifest, 0, "requester", "approved deployment", approvalId, "approver",
                Instant.parse("2026-08-01T00:00:00Z"));
    }
}
