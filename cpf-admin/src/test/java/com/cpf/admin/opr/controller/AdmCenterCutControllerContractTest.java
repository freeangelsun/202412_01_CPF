package com.cpf.admin.opr.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

class AdmCenterCutControllerContractTest {
    @Test
    void mutationsAreExecutionScopedAndJobScopeMutationsDoNotExist() {
        Method reprocess = method("reprocessFailedExecution");
        Method reconcile = method("reconcileUnknownExecution");
        assertThat(reprocess.getAnnotation(PostMapping.class).value())
                .containsExactly("/executions/{executionId}/reprocess-failed");
        assertThat(reconcile.getAnnotation(PostMapping.class).value())
                .containsExactly("/executions/{executionId}/reconcile-unknown");
        assertThat(Arrays.stream(AdmCenterCutController.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(java.util.Objects::nonNull)
                .flatMap(mapping -> Arrays.stream(mapping.value())))
                .noneMatch(path -> path.contains("/jobs/") &&
                        (path.contains("reprocess") || path.contains("reconcile")));
    }

    private static Method method(String name) {
        return Arrays.stream(AdmCenterCutController.class.getDeclaredMethods())
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
