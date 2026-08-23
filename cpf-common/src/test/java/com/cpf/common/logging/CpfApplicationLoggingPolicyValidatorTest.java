package com.cpf.common.logging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfApplicationLoggingPolicyValidatorTest {
    private final CpfApplicationLoggingPolicyValidator validator =
            new CpfApplicationLoggingPolicyValidator();

    @Test
    void rejectsTraversalDuplicateFileAndInvalidRetentionRelationship() {
        assertThatThrownBy(() -> validator.validate(policy("../outside", runtime(5, 365))))
                .hasMessageContaining("spring.application.name");

        Map<String, CpfLogFilePolicy> duplicates = new LinkedHashMap<>();
        duplicates.put("runtime", runtime(5, 365));
        duplicates.put("error", new CpfLogFilePolicy(
                true, "runtime.log", "ERROR", CpfLogFilePolicy.Rolling.DAILY, 5, 365));
        assertThatThrownBy(() -> validator.validate(new CpfApplicationLoggingPolicy(
                Path.of("logs"), "demo", "local", duplicates)))
                .hasMessageContaining("중복 file-name");

        assertThatThrownBy(() -> validator.validate(policy("demo", runtime(5, 5))))
                .hasMessageContaining("delete-after-days");
    }

    @Test
    void rejectsExternalFileNameAndUnsupportedLevel() {
        assertThatThrownBy(() -> validator.validate(policy("demo", new CpfLogFilePolicy(
                true, "../runtime.log", null, CpfLogFilePolicy.Rolling.DAILY, 5, 365))))
                .hasMessageContaining("하위경로가 없는");
        assertThatThrownBy(() -> validator.validate(policy("demo", new CpfLogFilePolicy(
                true, "runtime.log", "VERBOSE", CpfLogFilePolicy.Rolling.DAILY, 5, 365))))
                .hasMessageContaining("TRACE/DEBUG/INFO/WARN/ERROR");
    }

    private static CpfApplicationLoggingPolicy policy(String application, CpfLogFilePolicy file) {
        return new CpfApplicationLoggingPolicy(Path.of("logs"), application, "local", Map.of("runtime", file));
    }

    private static CpfLogFilePolicy runtime(int compress, int delete) {
        return new CpfLogFilePolicy(
                true, "runtime.log", null, CpfLogFilePolicy.Rolling.DAILY, compress, delete);
    }
}
