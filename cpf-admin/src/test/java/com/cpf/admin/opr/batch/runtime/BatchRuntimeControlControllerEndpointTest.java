package com.cpf.admin.opr.batch.runtime;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.data.api.CpfDataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchRuntimeControlControllerEndpointTest {
    private BatchRuntimeControlClient client;
    private BatchRuntimeControlController controller;

    @BeforeEach
    void setUp() {
        client = mock(BatchRuntimeControlClient.class);
        controller = new BatchRuntimeControlController(client, mock(AdmApprovalService.class), new ObjectMapper());
    }

    @Test
    void everyPrivilegedEndpointUsesAuthenticatedActorAndStripsNestedAliases() {
        // 모든 privileged mutation은 Browser actor 입력이 아니라 인증 Filter가 고정한 adm.operatorId를 받아야 합니다.
        java.util.Arrays.stream(BatchRuntimeControlController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class))
                .filter(method -> !"validateJobDefinition".equals(method.getName()))
                .forEach(method -> {
                    boolean authenticatedActor = java.util.Arrays.stream(method.getParameterAnnotations())
                            .flatMap(java.util.Arrays::stream)
                            .filter(annotation -> annotation instanceof org.springframework.web.bind.annotation.RequestAttribute)
                            .map(annotation -> (org.springframework.web.bind.annotation.RequestAttribute) annotation)
                            .anyMatch(attribute -> "adm.operatorId".equals(attribute.value()));
                    org.junit.jupiter.api.Assertions.assertTrue(authenticatedActor,
                            () -> method.getName() + " must use authenticated adm.operatorId");
                });
        when(client.saveJobDefinition(any())).thenReturn(row("state", "DRAFT"));
        when(client.createPlan(any())).thenReturn(row("state", "CREATED"));

        Map<String,Object> nestedActor = Map.of("operatorId", "browser-operator", "child", Map.of("requestUser", "browser-child", "safe", "value"));
        controller.saveJobDefinition("session-admin", new BatchJobDefinitionRequest(
                "JOB-1", 1L, "Job", "SPRING_BATCH", "DRAFT", "BAT", "test",
                nestedActor, List.of(), List.of(), Map.of(), Map.of(), Map.of(), "", "", "audit reason", null, null, 0L));
        BatchRuntimeDeploymentPlanRequest plan = new BatchRuntimeDeploymentPlanRequest();
        plan.planId = "PLAN-1"; plan.reason = "deploy reason"; plan.manifest = nestedActor;
        controller.plan("session-admin", plan);

        assertCanonicalActor(captureSave());
        assertCanonicalActor(capturePlan());
    }

    @Test
    void validationErrorsAreAlways400AndNeverUnknownResult() {
        assertValidation(controller.saveJobDefinition("session-admin", new BatchJobDefinitionRequest(
                null, 1L, "Job", "SPRING_BATCH", "DRAFT", "BAT", null, Map.of(), List.of(), List.of(), Map.of(), Map.of(), Map.of(), null, null, "missing job", null, null, 0L)),
                "BAT_JOB_DEFINITION_INVALID");
        assertValidation(controller.transitionJobDefinition("session-admin", "JOB-1", 1L,
                new BatchJobDefinitionTransitionRequest(-1L, "PUBLISHED", null, "publish reason")),
                "BAT_JOB_TRANSITION_INVALID");
        BatchRuntimeCommandRequest command = new BatchRuntimeCommandRequest();
        assertValidation(controller.command("session-admin", command), "BAT_COMMAND_INVALID");
        BatchRuntimeDeploymentPlanRequest plan = new BatchRuntimeDeploymentPlanRequest(); plan.reason = "deploy reason";
        assertValidation(controller.plan("session-admin", plan), "BAT_DEPLOYMENT_PLAN_INVALID");
    }

    @Test
    void typedOwnerErrorsUseOneEndpointIndependentStatusMatrix() {
        for (BatchControlClientException.Category category : BatchControlClientException.Category.values()) {
            when(client.createPlan(any())).thenThrow(new BatchControlClientException(category, "E-" + category, "failure", "TRACE", null));
            ResponseEntity<Map<String, Object>> response = controller.plan("session-admin", validPlan());
            int expected = switch (category) {
                case VALIDATION -> 400; case PERMISSION -> 403; case NOT_FOUND -> 404; case CONFLICT -> 409;
                case UNKNOWN_RESULT -> 502; case UNAVAILABLE -> 503; case OWNER_ERROR -> 500;
            };
            assertEquals(expected, response.getStatusCode().value(), category.name());
            assertEquals(category == BatchControlClientException.Category.UNKNOWN_RESULT ? "UNKNOWN_RESULT" : "FAILED", response.getBody().get("state"));
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

    private BatchRuntimeDeploymentPlanRequest validPlan() {
        BatchRuntimeDeploymentPlanRequest plan = new BatchRuntimeDeploymentPlanRequest();
        plan.planId = "PLAN-1"; plan.reason = "deploy reason"; plan.manifest = Map.of("artifact", "cpf-batch");
        return plan;
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
        } else if (value instanceof List<?> list) list.forEach(item -> assertNoAlias(item, false));
    }

    private Map<String, Object> captureSave() {
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor(); verify(client).saveJobDefinition(captor.capture()); return captor.getValue();
    }
    private Map<String, Object> capturePlan() {
        ArgumentCaptor<Map<String, Object>> captor = mapCaptor(); verify(client).createPlan(captor.capture()); return captor.getValue();
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private ArgumentCaptor<Map<String, Object>> mapCaptor() { return (ArgumentCaptor) ArgumentCaptor.forClass(Map.class); }
    private CpfDataRow row(String key, Object value) { return CpfDataRow.copyOf(Map.of(key, value)); }
}
