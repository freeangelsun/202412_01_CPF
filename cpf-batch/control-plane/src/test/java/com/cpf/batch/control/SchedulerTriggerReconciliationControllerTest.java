package com.cpf.batch.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.control.SchedulerTriggerReconciliationController.RetryUnknownRequest;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.batch.control.security.BatVerifiedActorResolver.ApprovedActors;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class SchedulerTriggerReconciliationControllerTest {
    private static final Instant FIRE_AT = Instant.parse("2026-08-15T01:00:00Z");
    private static final ApprovedActors ACTORS = new ApprovedActors("requester", "approver", "41");

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private final BatVerifiedActorResolver actors = mock(BatVerifiedActorResolver.class);
    private final HttpServletRequest http = mock(HttpServletRequest.class);
    private SchedulerTriggerReconciliationController controller;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(actors.approved(http, "requester", "approver", "41")).thenReturn(ACTORS);
        controller = new SchedulerTriggerReconciliationController(jdbc, provider, actors);
    }

    @Test
    void unknownRetryUsesCasAndPersistsAudit() {
        RetryUnknownRequest request = request("reconcile-1", "operator confirmed remote result");
        when(jdbc.queryForMap("scheduler-trigger-reconcile-audit-find", "reconcile-1"))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbc.queryForMap("scheduler-trigger-reconcile-load", "SCH-1", Timestamp.from(FIRE_AT)))
                .thenReturn(unknown());
        when(jdbc.update(eq("scheduler-trigger-reconcile-unknown-retry"), any(), eq("SCH-1"),
                eq(Timestamp.from(FIRE_AT)), eq("trigger-1"), eq(2))).thenReturn(1);
        when(jdbc.update("scheduler-trigger-reconcile-audit", "41", "SCH-1@" + FIRE_AT,
                "requester", "approver", "operator confirmed remote result", "reconcile-1", 2))
                .thenReturn(1);

        var response = controller.retryUnknown("SCH-1", request, http);

        assertThat(response.getBody()).containsEntry("status", "FAILED").containsEntry("replayed", false);
    }

    @Test
    void exactAuditReplayIsReadOnlyAndChangedCommandConflicts() {
        RetryUnknownRequest request = request("reconcile-1", "operator confirmed remote result");
        when(jdbc.queryForMap("scheduler-trigger-reconcile-audit-find", "reconcile-1"))
                .thenReturn(audit("operator confirmed remote result"))
                .thenReturn(audit("operator confirmed remote result"));

        var replay = controller.retryUnknown("SCH-1", request, http);
        assertThat(replay.getBody()).containsEntry("replayed", true);

        assertThatThrownBy(() -> controller.retryUnknown(
                "SCH-1", request("reconcile-1", "different confirmed outcome"), http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("IDEMPOTENCY_KEY_REUSED");
        verify(jdbc, never()).update(eq("scheduler-trigger-reconcile-unknown-retry"), any(), any(), any(), any(), any());
    }

    @Test
    void rejectsNonUnknownAndAttemptOrTriggerKeyMismatch() {
        RetryUnknownRequest request = request("reconcile-1", "operator confirmed remote result");
        when(jdbc.queryForMap("scheduler-trigger-reconcile-audit-find", "reconcile-1"))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbc.queryForMap("scheduler-trigger-reconcile-load", "SCH-1", Timestamp.from(FIRE_AT)))
                .thenReturn(Map.of("trigger_status", "FAILED", "idempotency_key", "trigger-1", "attempt_count", 2));
        assertThatThrownBy(() -> controller.retryUnknown("SCH-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("NOT_UNKNOWN");
    }

    @Test
    void casAndAuditFailureRemainFailClosed() {
        RetryUnknownRequest request = request("reconcile-1", "operator confirmed remote result");
        when(jdbc.queryForMap("scheduler-trigger-reconcile-audit-find", "reconcile-1"))
                .thenThrow(new EmptyResultDataAccessException(1));
        when(jdbc.queryForMap("scheduler-trigger-reconcile-load", "SCH-1", Timestamp.from(FIRE_AT)))
                .thenReturn(unknown());
        when(jdbc.update(eq("scheduler-trigger-reconcile-unknown-retry"), any(), eq("SCH-1"),
                eq(Timestamp.from(FIRE_AT)), eq("trigger-1"), eq(2)))
                .thenReturn(0)
                .thenReturn(1);

        assertThatThrownBy(() -> controller.retryUnknown("SCH-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("RECONCILE_CONFLICT");

        when(jdbc.update("scheduler-trigger-reconcile-audit", "41", "SCH-1@" + FIRE_AT,
                "requester", "approver", "operator confirmed remote result", "reconcile-1", 2))
                .thenReturn(0);
        assertThatThrownBy(() -> controller.retryUnknown("SCH-1", request, http))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AUDIT_REJECTED");
    }

    private static RetryUnknownRequest request(String idempotencyKey, String reason) {
        return new RetryUnknownRequest(
                FIRE_AT, "trigger-1", 2, idempotencyKey,
                "requester", "approver", "41", reason);
    }

    private static Map<String, Object> unknown() {
        return Map.of(
                "schedule_id", "SCH-1",
                "scheduled_fire_at", Timestamp.from(FIRE_AT),
                "trigger_status", "UNKNOWN",
                "idempotency_key", "trigger-1",
                "attempt_count", 2,
                "last_error_code", "TIMEOUT");
    }

    private static Map<String, Object> audit(String reason) {
        return Map.of(
                "request_id", "41",
                "entity_key", "SCH-1@" + FIRE_AT,
                "from_status", "UNKNOWN",
                "to_status", "FAILED",
                "requester_id", "requester",
                "approver_id", "approver",
                "reason_text", reason,
                "idempotency_key", "reconcile-1",
                "expected_attempt", 2);
    }
}
