package com.cpf.admin.opr.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdmNotificationSqlPortabilityTest {

    @Test
    void commonNotificationRuntimeDoesNotUseMariaDbOnlySql() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/cpf/admin/opr/service/AdmNotificationService.java"));

        assertThat(source)
                .doesNotContain("ON DUPLICATE KEY")
                .doesNotContain("LAST_INSERT_ID")
                .doesNotContain("LIMIT ?")
                .contains("GeneratedKeyHolder")
                .contains("new String[] {\"rule_id\"}")
                .contains("new String[] {\"delivery_id\"}")
                .contains("setMaxRows");
    }
}
