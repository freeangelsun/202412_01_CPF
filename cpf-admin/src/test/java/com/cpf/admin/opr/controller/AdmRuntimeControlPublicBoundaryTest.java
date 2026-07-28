package com.cpf.admin.opr.controller;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdmRuntimeControlPublicBoundaryTest {

    @Test
    void controllerDependsOnlyOnRuntimeControlPublicApi() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java"));

        assertThat(source)
                .contains("com.cpf.core.api.runtimecontrol")
                .doesNotContain("com.cpf.core.common.runtimecontrol");
    }
}
