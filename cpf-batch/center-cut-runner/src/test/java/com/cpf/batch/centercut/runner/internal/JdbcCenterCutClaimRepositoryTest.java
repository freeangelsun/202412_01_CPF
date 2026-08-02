package com.cpf.batch.centercut.runner.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcCenterCutClaimRepositoryTest {
    @Test
    void claimsOnlyItemsBoundToTheSpringBatchCenterCutExecution() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("centercut-claim-find-candidates")).thenReturn("find-candidates");
        when(jdbc.queryForList("find-candidates")).thenReturn(List.of(
                Map.of("center_cut_item_id", 11L, "center_cut_execution_id", "other-exec",
                        "tps_limit", 0, "concurrency_limit", 1),
                Map.of("center_cut_item_id", 12L, "center_cut_execution_id", "target-exec",
                        "tps_limit", 10, "concurrency_limit", 2)));
        when(admission.acquire("target-exec", 10, 2)).thenReturn(true);

        JdbcCenterCutClaimRepository repository = spy(
                new JdbcCenterCutClaimRepository(jdbc, admission, provider));
        JdbcCenterCutClaimRepository.Claim expected = new JdbcCenterCutClaimRepository.Claim(
                12L, "runner-a", "claim-a", 3L, Instant.now().plusSeconds(30), "target-exec");
        doReturn(Optional.of(expected)).when(repository).tryClaim(
                eq(12L), eq("runner-a"), eq("pool-a"), any(Duration.class), eq("target-exec"));

        Optional<JdbcCenterCutClaimRepository.Claim> actual = repository.claimForExecution(
                "target-exec", "runner-a", "pool-a", Duration.ofSeconds(30));

        assertThat(actual).contains(expected);
        verify(admission, never()).acquire(eq("other-exec"), any(Integer.class), any(Integer.class));
        verify(admission).acquire("target-exec", 10, 2);
        verify(repository, never()).tryClaim(
                eq(11L), any(), any(), any(Duration.class), eq("other-exec"));
    }
}
