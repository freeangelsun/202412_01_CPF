package com.cpf.batch.control;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class CenterCutReconciliationControllerTest {
    private JdbcTemplate jdbc;
    private CpfVendorSqlCatalog catalog;
    private BatVerifiedActorResolver actorResolver;
    private HttpServletRequest http;
    private CenterCutReconciliationController controller;
    private CenterCutReconciliationController.ApprovedRequest request;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        catalog = mock(CpfVendorSqlCatalog.class);
        CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
        actorResolver = mock(BatVerifiedActorResolver.class);
        http = mock(HttpServletRequest.class);
        when(provider.forModule("bat")).thenReturn(catalog);
        when(actorResolver.approved(eq(http), eq("requester"), eq("approver"), isNull()))
                .thenReturn(new BatVerifiedActorResolver.ApprovedActors(
                        "requester", "approver", "approval-1"));
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(jdbc.queryForMap("centercut-reconcile-load-execution", "exec-1"))
                .thenReturn(Map.of(
                        "center_cut_job_id", "job-1",
                        "execution_state", "UNKNOWN_RESULT"));
        request = new CenterCutReconciliationController.ApprovedRequest(
                "requester", "approver", "verified business result");
        controller = new CenterCutReconciliationController(jdbc, provider, actorResolver);
    }


    @Test
    void mapsMissingExecutionToNotFoundBeforeMutation() {
        when(jdbc.queryForMap("centercut-reconcile-load-execution", "missing-exec"))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> controller.unknownExecution("missing-exec", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("CENTER_CUT_EXECUTION_NOT_FOUND");
        verify(jdbc, never()).update(
                eq("centercut-reconcile-unknown-items"), any(Object[].class));
    }


    @Test
    void rejectsBlankReasonAsBadRequestBeforeJdbcMutation() {
        var invalid = new CenterCutReconciliationController.ApprovedRequest(
                "requester", "approver", " ");

        assertThatThrownBy(() -> controller.unknownExecution("exec-1", invalid, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("CENTER_CUT_RECONCILE_REASON_REQUIRED");
        verify(jdbc, never()).queryForMap(any(), any());
    }

    @Test
    void failsClosedWhenAuditInsertAffectsNoRow() {
        when(jdbc.update("centercut-reconcile-unknown-items", "exec-1")).thenReturn(1);
        when(jdbc.update("centercut-reconcile-unknown-execution", 1, 1, "exec-1"))
                .thenReturn(1);
        when(jdbc.update(
                eq("centercut-reconcile-audit"),
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        assertThatThrownBy(() -> controller.unknownExecution("exec-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("CENTER_CUT_RECONCILE_AUDIT_PERSISTENCE_FAILED");
    }

    @Test
    void rejectsUnknownReconcileWhenNoUnknownItemWasChanged() {
        when(jdbc.update("centercut-reconcile-unknown-items", "exec-1")).thenReturn(0);

        assertThatThrownBy(() -> controller.unknownExecution("exec-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("CENTER_CUT_UNKNOWN_ITEMS_NOT_FOUND");

        verify(jdbc, never()).update(
                eq("centercut-reconcile-unknown-execution"), any(), any(), any());
        verify(jdbc, never()).update(
                eq("centercut-reconcile-audit"),
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void rejectsAndRollsBackWhenExecutionStateTransitionLosesItsRace() {
        when(jdbc.update("centercut-reconcile-unknown-items", "exec-1")).thenReturn(2);
        when(jdbc.update("centercut-reconcile-unknown-execution", 2, 2, "exec-1"))
                .thenReturn(0);

        assertThatThrownBy(() -> controller.unknownExecution("exec-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("CENTER_CUT_UNKNOWN_EXECUTION_STATE_CONFLICT");

        verify(jdbc, never()).update(
                eq("centercut-reconcile-audit"),
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void disablesJobScopeUnknownReplayBeforeAnyMutation() {
        assertThatThrownBy(() -> controller.unknownJob("job-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(
                        "CENTER_CUT_JOB_SCOPE_RECONCILE_DISABLED_USE_EXECUTION_SCOPE");

        verify(jdbc, never()).update(
                eq("centercut-reconcile-unknown-job"), any(Object[].class));
    }

    @Test
    void auditsOnlyAfterBothUnknownTransitionsSucceed() {
        when(jdbc.update("centercut-reconcile-unknown-items", "exec-1")).thenReturn(2);
        when(jdbc.update("centercut-reconcile-unknown-execution", 2, 2, "exec-1"))
                .thenReturn(1);
        when(jdbc.update(
                eq("centercut-reconcile-audit"),
                eq("job-1"),
                eq("RECONCILE_UNKNOWN"),
                eq("requester"),
                eq("verified business result"),
                any(),
                eq("approver"),
                eq("approver")))
                .thenReturn(1);

        controller.unknownExecution("exec-1", request, http);

        verify(jdbc).update(
                eq("centercut-reconcile-audit"),
                eq("job-1"),
                eq("RECONCILE_UNKNOWN"),
                eq("requester"),
                eq("verified business result"),
                any(),
                eq("approver"),
                eq("approver"));
    }
}
