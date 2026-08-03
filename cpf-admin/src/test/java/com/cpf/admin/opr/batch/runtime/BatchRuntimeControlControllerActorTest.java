package com.cpf.admin.opr.batch.runtime;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BatchRuntimeControlControllerActorTest {
    private static final Set<String> CLIENT_ACTOR_FIELDS = Set.of(
            "requestedBy", "requestUser", "actorId", "operatorId", "operatorIdOverride");

    @Test
    @SuppressWarnings("unchecked")
    void stripsClientActorAliasesRecursivelyAndInjectsAuthenticatedOperator() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("requestedBy", "browser-user");
        request.put("operatorIdOverride", "browser-override");
        request.put("approvedBy", "security-approver");
        request.put("target", Map.of(
                "actorId", "nested-browser-user",
                "resourceId", "BAT-01",
                "items", List.of(Map.of(
                        "requestUser", "deep-browser-user",
                        "value", 7))));

        Map<String, Object> command = invokeWithServerActor(request, "authenticated-admin");

        assertEquals("authenticated-admin", command.get("requestedBy"));
        assertEquals("security-approver", command.get("approvedBy"));
        assertNoClientActor(command, true);
        assertThrows(UnsupportedOperationException.class, () -> command.put("requestedBy", "tampered"));

        Map<?, ?> target = assertInstanceOf(Map.class, command.get("target"));
        assertThrows(UnsupportedOperationException.class, () -> ((Map<Object, Object>) target).put("actorId", "tampered"));
    }

    @Test
    void rejectsMissingAuthenticatedOperator() throws Exception {
        InvocationTargetException failure = assertThrows(
                InvocationTargetException.class,
                () -> actorMethod().invoke(null, Map.of("reason", "test"), " "));
        assertInstanceOf(IllegalArgumentException.class, failure.getCause());
        assertTrue(failure.getCause().getMessage().contains("authenticated operator"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> invokeWithServerActor(
            Map<String, Object> request, String operatorId) throws Exception {
        return (Map<String, Object>) actorMethod().invoke(null, request, operatorId);
    }

    private static Method actorMethod() throws NoSuchMethodException {
        Method method = BatchRuntimeControlController.class.getDeclaredMethod(
                "withServerActor", Map.class, String.class);
        method.setAccessible(true);
        return method;
    }

    private static void assertNoClientActor(Object value, boolean allowCanonicalTopLevel) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                boolean canonicalTopLevel = allowCanonicalTopLevel && "requestedBy".equals(key);
                assertFalse(CLIENT_ACTOR_FIELDS.contains(key) && !canonicalTopLevel,
                        () -> "client actor field leaked: " + key);
                assertNoClientActor(entry.getValue(), false);
            }
        } else if (value instanceof List<?> list) {
            list.forEach(item -> assertNoClientActor(item, false));
        }
    }
}
