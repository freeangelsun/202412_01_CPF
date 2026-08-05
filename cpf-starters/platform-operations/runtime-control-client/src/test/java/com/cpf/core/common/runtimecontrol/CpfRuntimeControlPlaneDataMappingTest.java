package com.cpf.core.common.runtimecontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class CpfRuntimeControlPlaneDataMappingTest {
    @Test
    void repositoryRequiredNumberAndNonNullTimeFailClosed() throws Exception {
        CpfRuntimeControlPlaneRepository repository =
                new CpfRuntimeControlPlaneRepository(new JdbcTemplate(), new ObjectMapper());
        Method number = privateMethod(CpfRuntimeControlPlaneRepository.class, "number", Object.class);
        Method instant = privateMethod(CpfRuntimeControlPlaneRepository.class, "toInstant", Object.class);

        assertEquals(7L, number.invoke(repository, 7L));
        assertEquals(Instant.EPOCH, instant.invoke(repository, Timestamp.from(Instant.EPOCH)));
        assertInvocationCause(IllegalStateException.class, () -> number.invoke(repository, new Object[]{null}));
        assertInvocationCause(IllegalStateException.class, () -> number.invoke(repository, "7"));
        assertInvocationCause(IllegalStateException.class, () -> instant.invoke(repository, "not-an-instant"));
        assertInvocationCause(IllegalStateException.class, () -> instant.invoke(repository, 7L));
    }

    @Test
    void serviceRequiredNumberAndNonNullTimeFailClosed() throws Exception {
        CpfRuntimeControlPlaneRepository repository =
                new CpfRuntimeControlPlaneRepository(new JdbcTemplate(), new ObjectMapper());
        CpfRuntimeControlPlaneService service = new CpfRuntimeControlPlaneService(repository);
        Method number = privateMethod(CpfRuntimeControlPlaneService.class, "number", Object.class);
        Method instant = privateMethod(CpfRuntimeControlPlaneService.class, "instant", Object.class);

        assertEquals(9L, number.invoke(service, 9L));
        assertEquals(Instant.EPOCH, instant.invoke(service, Instant.EPOCH));
        assertInvocationCause(IllegalStateException.class, () -> number.invoke(service, new Object[]{null}));
        assertInvocationCause(IllegalStateException.class, () -> instant.invoke(service, "invalid"));
    }

    private static Method privateMethod(Class<?> type, String name, Class<?> parameter) throws Exception {
        Method method = type.getDeclaredMethod(name, parameter);
        method.setAccessible(true);
        return method;
    }

    private static void assertInvocationCause(Class<? extends Throwable> expected, Throwing runnable) {
        InvocationTargetException error = assertThrows(InvocationTargetException.class, runnable::run);
        org.junit.jupiter.api.Assertions.assertTrue(
                expected.isInstance(error.getCause()),
                "expected=" + expected.getName() + ", actual=" + error.getCause());
    }

    @FunctionalInterface
    private interface Throwing { void run() throws Exception; }
}
