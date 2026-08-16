package com.cpf.batch.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.control.RemoteMessageReconciliationController.RetryUnknownRequest;
import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.batch.control.security.BatVerifiedActorResolver.ApprovedActors;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

class RemoteMessageReconciliationControllerTest {
    private static final String HASH = "a".repeat(64);
    private static final String REASON = "confirmed absent secret=value";
    private static final ApprovedActors ACTORS =
            new ApprovedActors("requester", "approver", "approval-1");

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final CpfVendorSqlCatalog catalog = mock(CpfVendorSqlCatalog.class);
    private final CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
    private final BatVerifiedActorResolver actors = mock(BatVerifiedActorResolver.class);
    private final HttpServletRequest http = mock(HttpServletRequest.class);
    private RemoteMessageReconciliationController controller;

    @BeforeEach
    void setUp() {
        when(provider.forModule("bat")).thenReturn(catalog);
        when(catalog.required("execution-remote-message-reconcile-load")).thenReturn("LOAD");
        when(catalog.required("execution-remote-message-reconcile-unknown-retry")).thenReturn("RETRY");
        when(catalog.required("execution-remote-message-reconcile-audit")).thenReturn("AUDIT");
        when(actors.approved(http, "requester", "approver", "approval-1")).thenReturn(ACTORS);
        controller = new RemoteMessageReconciliationController(jdbc, provider, actors);
    }

    @Test
    void unknownRetryUsesCasAndPersistsApprovedAudit() {
        RetryUnknownRequest request = request(REASON);
        String code = reconciliationCode(request);
        when(jdbc.queryForMap("LOAD", "REQUEST", "MSG-1")).thenReturn(unknown(7L));
        when(jdbc.update("RETRY", code, "REQUEST", "MSG-1", HASH, 2L, 7L)).thenReturn(1);
        when(jdbc.update(eq("AUDIT"), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        var response = controller.retryUnknown("request", "MSG-1", request, http);

        assertThat(response.getBody()).containsEntry("status", "FAILED").containsEntry("replayed", false);
        verify(jdbc).update("RETRY", code, "REQUEST", "MSG-1", HASH, 2L, 7L);
        verify(jdbc).update(eq("AUDIT"), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void exactReplayIsReadOnlyButSameKeyWithChangedReasonConflicts() {
        RetryUnknownRequest original = request(REASON);
        when(jdbc.queryForMap("LOAD", "REQUEST", "MSG-1"))
                .thenReturn(replayed(8L, reconciliationCode(original)))
                .thenReturn(replayed(8L, reconciliationCode(original)));

        var replay = controller.retryUnknown("REQUEST", "MSG-1", original, http);
        assertThat(replay.getBody()).containsEntry("replayed", true);

        RetryUnknownRequest changed = request("different approved reason");
        assertThatThrownBy(() -> controller.retryUnknown("REQUEST", "MSG-1", changed, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("BATCH_REMOTE_MESSAGE_NOT_UNKNOWN");
        verify(jdbc, never()).update(eq("RETRY"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void casRaceOnlySucceedsForTheSameCanonicalCommand() {
        RetryUnknownRequest request = request(REASON);
        String code = reconciliationCode(request);
        when(jdbc.queryForMap("LOAD", "REQUEST", "MSG-1"))
                .thenReturn(unknown(7L))
                .thenReturn(replayed(8L, code));
        when(jdbc.update("RETRY", code, "REQUEST", "MSG-1", HASH, 2L, 7L)).thenReturn(0);

        var response = controller.retryUnknown("REQUEST", "MSG-1", request, http);

        assertThat(response.getBody()).containsEntry("replayed", true);
        verify(jdbc, never()).update(eq("AUDIT"), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void auditFailureAndDatabaseFailureRemainFailClosed() {
        RetryUnknownRequest request = request(REASON);
        String code = reconciliationCode(request);
        when(jdbc.queryForMap("LOAD", "REQUEST", "MSG-1")).thenReturn(unknown(7L));
        when(jdbc.update("RETRY", code, "REQUEST", "MSG-1", HASH, 2L, 7L)).thenReturn(1);
        when(jdbc.update(eq("AUDIT"), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        assertThatThrownBy(() -> controller.retryUnknown("REQUEST", "MSG-1", request, http))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AUDIT_REJECTED");

        when(jdbc.queryForMap("LOAD", "REQUEST", "MSG-2"))
                .thenThrow(new IllegalStateException("db-down"));
        assertThatThrownBy(() -> controller.retryUnknown("REQUEST", "MSG-2", request, http))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("db-down");
    }

    @Test
    void missingMessageMapsOnlyEmptyResultToNotFound() {
        RetryUnknownRequest request = request(REASON);
        when(jdbc.queryForMap("LOAD", "REQUEST", "MSG-1"))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThatThrownBy(() -> controller.retryUnknown("REQUEST", "MSG-1", request, http))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("BATCH_REMOTE_MESSAGE_NOT_FOUND");
    }

    private static RetryUnknownRequest request(String reason) {
        return new RetryUnknownRequest(
                HASH, 2L, 7L, "reconcile-command-1", "requester", "approver", "approval-1", reason);
    }

    private static Map<String, Object> unknown(long version) {
        Map<String, Object> row = new HashMap<>();
        row.put("direction_cd", "REQUEST");
        row.put("message_id", "MSG-1");
        row.put("payload_sha256", HASH);
        row.put("status_cd", "UNKNOWN");
        row.put("attempt_no", 2L);
        row.put("last_error_cd", "SocketTimeout");
        row.put("version_no", version);
        return row;
    }

    private static Map<String, Object> replayed(long version, String code) {
        Map<String, Object> row = unknown(version);
        row.put("status_cd", "FAILED");
        row.put("last_error_cd", code);
        return row;
    }

    private static String reconciliationCode(RetryUnknownRequest request) {
        String canonical = String.join("\n",
                "REQUEST",
                "MSG-1",
                HASH,
                Long.toString(request.expectedAttemptNo()),
                Long.toString(request.expectedVersion()),
                request.idempotencyKey(),
                ACTORS.requestedBy(),
                ACTORS.approvedBy(),
                ACTORS.approvalRequestId(),
                request.reason());
        try {
            return "RECONCILED_RETRY_" + HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception failure) {
            throw new IllegalStateException(failure);
        }
    }
}
