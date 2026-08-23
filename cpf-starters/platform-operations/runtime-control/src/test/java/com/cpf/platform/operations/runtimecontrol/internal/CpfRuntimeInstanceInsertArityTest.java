package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInstanceRegistration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

class CpfRuntimeInstanceInsertArityTest {

    @Test
    void inactiveIdentityClaimBindsEveryInsertPlaceholder() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository = repository(jdbc);

        invoke(repository, "claimInactiveServiceInstanceIdentity");

        assertExactInstanceInsertArity(jdbc);
    }

    @Test
    void zeroRowActiveProjectionBindsEveryInsertPlaceholder() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        CpfRuntimeControlPlaneRepository repository = repository(jdbc);

        invoke(repository, "upsertServiceInstance");

        assertExactInstanceInsertArity(jdbc);
    }

    private static CpfRuntimeControlPlaneRepository repository(JdbcTemplate jdbc) {
        @SuppressWarnings("unchecked")
        ObjectProvider<JdbcTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(jdbc);
        return new CpfRuntimeControlPlaneRepository(provider, new ObjectMapper());
    }

    private static void invoke(CpfRuntimeControlPlaneRepository repository, String methodName) throws Exception {
        Method method = CpfRuntimeControlPlaneRepository.class
                .getDeclaredMethod(methodName, CpfRuntimeInstanceRegistration.class);
        method.setAccessible(true);
        method.invoke(repository, registration());
    }

    private static void assertExactInstanceInsertArity(JdbcTemplate jdbc) {
        Invocation insert = mockingDetails(jdbc).getInvocations().stream()
                .filter(invocation -> invocation.getArguments().length > 0)
                .filter(invocation -> invocation.getArgument(0) instanceof String sql
                        && sql.startsWith("INSERT INTO OPS_SERVICE_INSTANCE"))
                .findFirst()
                .orElseThrow();
        String sql = insert.getArgument(0);
        int placeholders = Math.toIntExact(sql.chars().filter(character -> character == '?').count());
        Object[] invocationArguments = insert.getArguments();
        int boundValues = invocationArguments.length == 2 && invocationArguments[1] instanceof Object[] values
                ? values.length
                : invocationArguments.length - 1;

        assertThat(placeholders).isEqualTo(19);
        assertThat(boundValues).isEqualTo(placeholders);
    }

    private static CpfRuntimeInstanceRegistration registration() {
        Instant now = Instant.parse("2026-08-23T13:00:00Z");
        return new CpfRuntimeInstanceRegistration(
                "bat-smoke-instance-1", "BAT", "BAT_API", "local", null, null,
                "http://127.0.0.1:8282", "runtime-smoke-v1", "commit", "WORKER",
                "AUTO_CONFIGURATION", "1", "hash", Map.of("BATCH", "1|hash"), Map.of(),
                null, null, "HOST-A", "BAT", "cpf-batch-worker", "WORKER", 8123L,
                "25", "1", "1", now, now, 60);
    }
}
