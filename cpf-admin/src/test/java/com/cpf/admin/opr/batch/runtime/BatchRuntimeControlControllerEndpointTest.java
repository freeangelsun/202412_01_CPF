package com.cpf.admin.opr.batch.runtime;

import com.cpf.core.api.data.CpfDataRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchRuntimeControlControllerEndpointTest {
    private BatchRuntimeControlClient client;
    private BatchRuntimeControlController controller;

    @BeforeEach
    void setUp() {
        client = mock(BatchRuntimeControlClient.class);
        controller = new BatchRuntimeControlController(client);
    }

    @Test
    void everyPrivilegedEndpointUsesAuthenticatedActorAndStripsNestedAliases() {
        when(client.saveJobDefinition(any())).thenReturn(row("state", "DRAFT"));
        when(client.transitionJobDefinition(anyString(), anyLong(), any())).thenReturn(row("state", "PUBLISHED"));
        when(client.command(any())).thenReturn(row("state", "ACCEPTED"));
        when(client.createPlan(any())).thenReturn(row("state", "CREATED"));

        Map<String, Object> nestedActor = Map.of(
                "operatorId", "browser-operator",
                "child", Map.of("requestUser", "browser-child", "safe", "value"));
        controller.saveJobDefinition("session-admin", Map.of("jobId", "JOB-1", "reason", "test", "payload", nestedActor));
        controller.transitionJobDefinition("session-admin", "JOB-1", 1L, Map.of(
                "targetState", "PUBLISHED", "reason", "publish", "expectedVersion", 1L,
                "approvalRequestId", "APR-1", "payload", nestedActor));
        controller.command("session-admin", Map.of(
                "commandId", "CMD-1", "idempotencyKey", "IDEM-1", "commandType", "STOP",
                "reason", "test", "approvalRequestId", "APR-1", "approvedBy", "approver",
                "targetIds", List.of("BAT-1"), "payload", nestedActor));
        controller.plan("session-admin", Map.of(
                "idempotencyKey", "PLAN-1", "reason", "deploy", "approvalRequestId", "APR-1",
                "expectedVersion", 0L, "payload", nestedActor));

        assertCanonicalActor(captureSave());
        assertCanonicalActor(captureTransition());
        assertCanonicalActor(captureCommand());
        assertCanonicalActor(capturePlan());
    }

    @Test
    void validationErrorsAreAlways400AndNeverUnknownResult() {
        assertValidation(controller.saveJobDefinition("session-admin", Map.of("reason", "missing job")), "BAT_JOB_DEFINITION_INVALID");
        assertValidation(controller.transitionJobDefinition("session-admin", "JOB-1", 1L,
                Map.of("targetState", "PUBLISHED", "reason", "publish", "expectedVersion", -1L)),
                "BAT_JOB_TRANSITION_INVALID");
        assertValidation(controller.command("session-admin", Map.of()), "BAT_COMMAND_INVALID");
        assertValidation(controller.plan("session-admin", Map.of(
                "idempotencyKey", "PLAN-1", "reason", "deploy", "approvalRequestId", "APR-1",
                "expectedVersion", -1L)), "BAT_DEPLOYMENT_PLAN_INVALID");
        assertValidation(controller.plan(" ", Map.of(
                "idempotencyKey", "PLAN-1", "reason", "deploy", "approvalRequestId", "APR-1",
                "expectedVersion", 0L)), "BAT_DEPLOYMENT_PLAN_INVALID");
    }

    @Test
    void typedOwnerErrorsUseOneEndpointIndependentStatusMatrix() {
        for (BatchControlClientException.Category category : BatchControlClientException.Category.values()) {
            when(client.createPlan(any())).thenThrow(new BatchControlClientException(category, "E-" + category, "failure", "TRACE", null));
            ResponseEntity<Map<String, Object>> response = controller.plan("session-admin", validPlan());
            int expected = switch (category) {
                case VALIDATION -> 400;
                case PERMISSION -> 403;
                case NOT_FOUND -> 404;
                case CONFLICT -> 409;
                case UNKNOWN_RESULT -> 502;
                case UNAVAILABLE -> 503;
                case OWNER_ERROR -> 500;
            };
            assertEquals(expected, response.getStatusCode().value(), category.name());
            assertEquals(category == BatchControlClientException.Category.UNKNOWN_RESULT ? "UNKNOWN_RESULT" : "FAILED",
                    response.getBody().get("state"));
        }
    }

    @Test
    void unexpectedTransportFailureIsOnlyCaseMappedToUnknownResult() {
        when(client.createPlan(any())).thenThrow(new IllegalStateException("socket closed"));
        ResponseEntity<Map<String, Object>> response = controller.plan("session-admin", validPlan());
        assertEquals(503, response.getStatusCode().value());
        assertEquals("UNKNOWN_RESULT", response.getBody().get("state"));
        assertEquals("BAT_CONTROL_UNREACHABLE", response.getBody().get("errorCode"));
    }

    private Map<String, Object> validPlan() {
        return Map.of("idempotencyKey", "PLAN-1", "reason", "deploy", "approvalRequestId", "APR-1", "expectedVersion", 0L);
    }

    private void assertValidation(ResponseEntity<Map<String, Object>> response, String code) {
        assertEquals(400, response.getStatusCode().value());
        assertEquals("FAILED", response.getBody().get("state"));
        assertEquals(code, response.getBody().get("errorCode"));
        assertFalse("UNKNOWN_RESULT".equals(response.getBody().get("state")));
    }

    private void assertCanonicalActor(Map<String, Object> value) {
        assertEquals("session-admin", value.get("requestedBy"));
        assertNoAlias(value, true);
        assertThrows(UnsupportedOperationException.class, () -> value.put("requestedBy", "tamper"));
    }

    private void assertNoAlias(Object value, boolean top) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                boolean canonical = top && "requestedBy".equals(key);
                assertFalse(!canonical && List.of("requestedBy", "requestUser", "actorId", "operatorId", "operatorIdOverride").contains(key), key);
                assertNoAlias(entry.getValue(), false);
            }
        } else if (value instanceof List<?> list) {
            list.forEach(item -> assertNoAlias(item, false));
        }
    }

    private Map<String, Object> captureSave() {
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor(); verify(client).saveJobDefinition(captor.capture()); return captor.getValue();
    }
    private Map<String, Object> captureTransition() {
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor(); verify(client).transitionJobDefinition(anyString(), anyLong(), captor.capture()); return captor.getValue();
    }
    private Map<String, Object> captureCommand() {
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor(); verify(client).command(captor.capture()); return captor.getValue();
    }
    private Map<String, Object> capturePlan() {
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor(); verify(client).createPlan(captor.capture()); return captor.getValue();
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<Map<String, Object>> mapCaptor() { return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class); }
    private CpfDataRow row(String key, Object value) { return CpfDataRow.copyOf(Map.of(key, value)); }
}
