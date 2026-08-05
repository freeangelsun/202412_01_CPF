package com.cpf.batch.centercut.runner.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doAnswer;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionOperations;

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

        TransactionOperations transactions = immediateTransactions();
        JdbcCenterCutClaimRepository repository = spy(
                new JdbcCenterCutClaimRepository(jdbc, admission, provider, transactions));
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
    @Test
    void rollsBackAdmissionWhenTheItemClaimLosesItsRace() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        TransactionOperations transactions = mock(TransactionOperations.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("centercut-claim-find-candidates")).thenReturn("find-candidates");
        when(jdbc.queryForList("find-candidates")).thenReturn(List.of(
                Map.of("center_cut_item_id", 12L, "center_cut_execution_id", "target-exec",
                        "tps_limit", 10, "concurrency_limit", 2)));
        when(admission.acquire("target-exec", 10, 2)).thenReturn(true);
        doAnswer(invocation -> invocation.<org.springframework.transaction.support.TransactionCallback<Optional<JdbcCenterCutClaimRepository.Claim>>>getArgument(0)
                .doInTransaction(status)).when(transactions).execute(any());

        JdbcCenterCutClaimRepository repository = spy(
                new JdbcCenterCutClaimRepository(jdbc, admission, provider, transactions));
        doReturn(Optional.empty()).when(repository).tryClaim(
                eq(12L), eq("runner-a"), eq("pool-a"), any(Duration.class), eq("target-exec"));

        Optional<JdbcCenterCutClaimRepository.Claim> actual = repository.claimForExecution(
                "target-exec", "runner-a", "pool-a", Duration.ofSeconds(30));

        assertThat(actual).isEmpty();
        verify(status).setRollbackOnly();
    }


    @Test
    void failsClosedWhenExpiredItemCannotAdvanceItsExecutionToUnknown() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.queryForList("centercut-claim-find-expired-running")).thenReturn(List.of(
                Map.of("center_cut_item_id", 12L, "center_cut_execution_id", "exec-12")));
        when(jdbc.update("centercut-claim-expire", 12L)).thenReturn(1);
        when(jdbc.update("centercut-item-mark-unknown", 12L)).thenReturn(1);
        when(jdbc.update("centercut-execution-increment-unknown", "exec-12"))
                .thenReturn(0);

        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());

        assertThatThrownBy(repository::recoverExpiredToUnknown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_UNKNOWN_EXECUTION_STATE_CONFLICT");
    }

    @Test
    void rejectsExpiredItemWithoutAnExecutionOwner() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("center_cut_item_id", 13L);
        row.put("center_cut_execution_id", null);
        when(jdbc.queryForList("centercut-claim-find-expired-running"))
                .thenReturn(List.of(row));
        when(jdbc.update("centercut-claim-expire", 13L)).thenReturn(1);
        when(jdbc.update("centercut-item-mark-unknown", 13L)).thenReturn(1);

        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());

        assertThatThrownBy(repository::recoverExpiredToUnknown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_UNKNOWN_EXECUTION_ID_MISSING");
    }



    @Test
    void rejectsExpiredClaimWhenItsItemIsNoLongerRunning() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.queryForList("centercut-claim-find-expired-running")).thenReturn(List.of(
                Map.of("center_cut_item_id", 14L, "center_cut_execution_id", "exec-14")));
        when(jdbc.update("centercut-claim-expire", 14L)).thenReturn(1);
        when(jdbc.update("centercut-item-mark-unknown", 14L)).thenReturn(0);

        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());

        assertThatThrownBy(repository::recoverExpiredToUnknown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_ITEM_UNKNOWN_CONFLICT");
        verify(jdbc, never()).update(
                "centercut-execution-increment-unknown", "exec-14");
    }

    @Test
    void malformedExpiredClaimDoesNotBlockFollowingHealthyClaim() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Map<String, Object> malformed = new java.util.HashMap<>();
        malformed.put("center_cut_item_id", "bad-id");
        malformed.put("center_cut_execution_id", "bad-exec");
        when(jdbc.queryForList("centercut-claim-find-expired-running")).thenReturn(List.of(
                malformed,
                Map.of("center_cut_item_id", 18L, "center_cut_execution_id", "exec-18")));
        when(jdbc.update("centercut-claim-expire", 18L)).thenReturn(1);
        when(jdbc.update("centercut-item-mark-unknown", 18L)).thenReturn(1);
        when(jdbc.update("centercut-execution-increment-unknown", "exec-18"))
                .thenReturn(1);

        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());

        assertThatThrownBy(repository::recoverExpiredToUnknown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_EXPIRED_CLAIM_ITEM_ID_INVALID")
                .hasMessageContaining("recovered=1");
        verify(jdbc).update("centercut-execution-increment-unknown", "exec-18");
    }

    @Test
    void recoversHealthyRowsEvenWhenAnotherExpiredClaimIsPoisoned() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.queryForList("centercut-claim-find-expired-running")).thenReturn(List.of(
                Map.of("center_cut_item_id", 21L, "center_cut_execution_id", "exec-21"),
                Map.of("center_cut_item_id", 22L, "center_cut_execution_id", "exec-22")));
        when(jdbc.update("centercut-claim-expire", 21L)).thenReturn(1);
        when(jdbc.update("centercut-claim-expire", 22L)).thenReturn(1);
        when(jdbc.update("centercut-item-mark-unknown", 21L)).thenReturn(1);
        when(jdbc.update("centercut-item-mark-unknown", 22L)).thenReturn(1);
        when(jdbc.update("centercut-execution-increment-unknown", "exec-21"))
                .thenReturn(1);
        when(jdbc.update("centercut-execution-increment-unknown", "exec-22"))
                .thenReturn(0);

        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());

        assertThatThrownBy(repository::recoverExpiredToUnknown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_EXPIRED_CLAIM_RECOVERY_PARTIAL")
                .hasMessageContaining("recovered=1")
                .hasMessageContaining("22:CENTER_CUT_UNKNOWN_EXECUTION_STATE_CONFLICT");
        verify(jdbc).update("centercut-execution-increment-unknown", "exec-21");
        verify(jdbc).update("centercut-execution-increment-unknown", "exec-22");
    }

    @Test
    void completionFailsClosedWhenItemStateCannotBePersisted() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.update("centercut-claim-release-complete", 31L, "runner", "token", 4L))
                .thenReturn(1);
        when(jdbc.update("centercut-item-complete", "SUCCESS", "ok", 31L))
                .thenReturn(0);
        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());
        JdbcCenterCutClaimRepository.Claim claim = new JdbcCenterCutClaimRepository.Claim(
                31L, "runner", "token", 4L, Instant.now().plusSeconds(30), "exec-31");

        assertThatThrownBy(() -> repository.complete(claim, "SUCCESS", "result", "ok"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_ITEM_COMPLETION_CONFLICT");
        verify(jdbc, never()).update(eq("centercut-result-insert"), any(), any(), any(), any());
    }

    @Test
    void completionFailsClosedWhenExecutionCounterCannotAdvance() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        JdbcCenterCutAdmissionControl admission = mock(JdbcCenterCutAdmissionControl.class);
        CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.update("centercut-claim-release-complete", 32L, "runner", "token", 5L))
                .thenReturn(1);
        when(jdbc.update("centercut-item-complete", "SUCCESS", "ok", 32L)).thenReturn(1);
        when(jdbc.update("centercut-result-insert", "SUCCESS", "result", "ok", 32L))
                .thenReturn(1);
        when(jdbc.update(
                "centercut-execution-update-counters",
                "SUCCESS", "SUCCESS", "SUCCESS", "exec-32"))
                .thenReturn(0);
        JdbcCenterCutClaimRepository repository = new JdbcCenterCutClaimRepository(
                jdbc, admission, provider, immediateTransactions());
        JdbcCenterCutClaimRepository.Claim claim = new JdbcCenterCutClaimRepository.Claim(
                32L, "runner", "token", 5L, Instant.now().plusSeconds(30), "exec-32");

        assertThatThrownBy(() -> repository.complete(claim, "SUCCESS", "result", "ok"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CENTER_CUT_EXECUTION_COUNTER_CONFLICT");
        verify(jdbc, never()).queryForObject(
                eq("centercut-item-count-remaining"), eq(Integer.class), any());
    }

    private static TransactionOperations immediateTransactions() {
        TransactionOperations transactions = mock(TransactionOperations.class);
        doAnswer(invocation -> {
            TransactionStatus status = mock(TransactionStatus.class);
            return invocation.<org.springframework.transaction.support.TransactionCallback<?>>getArgument(0)
                    .doInTransaction(status);
        }).when(transactions).execute(any());
        return transactions;
    }

}
