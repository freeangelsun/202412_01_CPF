package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class CpfBatchClassBasedAopContractTest {

    @Test
    void transactionalControlPlaneAdapterSupportsClassBasedProxying() {
        Class<?> owner = JdbcBatchExecutionControlPlaneAdapter.class;
        assertTrue(Arrays.stream(owner.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(Transactional.class)));
        assertFalse(Modifier.isFinal(owner.getModifiers()));
    }
}
