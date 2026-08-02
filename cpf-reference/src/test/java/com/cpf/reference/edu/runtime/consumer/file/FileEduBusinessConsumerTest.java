package com.cpf.reference.edu.runtime.consumer.file;

import com.cpf.reference.edu.runtime.consumer.EduBusinessConsumerResult;
import com.cpf.reference.edu.runtime.consumer.EduConsumerBinding;
import com.cpf.reference.edu.runtime.consumer.EduConsumerType;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class FileEduBusinessConsumerTest {
    @TempDir
    Path root;

    @Test
    void writesInsideRequirementRootAndReturnsPortableRelativePath() {
        FileEduBusinessConsumer consumer = new FileEduBusinessConsumer(root, new ObjectMapper());
        EduConsumerBinding binding = new EduConsumerBinding(
                "EDU-DEV-08",
                EduConsumerType.FILE,
                "cpf-reference",
                "file-consumer",
                "WRITE",
                "reference-file-contract",
                "write-file",
                "cpf.edu.business-file-root",
                30,
                List.of("fileName"));
        EduExecutionCommand command = new EduExecutionCommand(
                "order\\2026/../sample",
                "fixture-idempotency-key",
                0,
                "fixture-operator",
                Set.of("EDU_DEVELOPER"),
                "SELF",
                "verify portable path",
                "fixture-request-id",
                "fixture-trace-id",
                Map.of("fileName", "sample.json"),
                EduFailurePoint.NONE,
                false,
                false);

        EduBusinessConsumerResult result = consumer.invoke(binding, command, 7L);

        String relativePath = String.valueOf(result.data().get("path"));
        assertThat(result.code()).isEqualTo("FILE_COMMITTED");
        assertThat(relativePath)
                .startsWith("edu-dev-08/")
                .doesNotContain("\\")
                .doesNotContain("../");
        assertThat(Files.isRegularFile(root.resolve(relativePath))).isTrue();
        assertThat(result.data().get("sha256").toString()).hasSize(64);
    }
}
