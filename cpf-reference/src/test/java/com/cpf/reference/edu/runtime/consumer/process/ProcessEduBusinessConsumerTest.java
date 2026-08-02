package com.cpf.reference.edu.runtime.consumer.process;

import com.cpf.reference.edu.runtime.consumer.EduBusinessConsumerResult;
import com.cpf.reference.edu.runtime.consumer.EduConsumerBinding;
import com.cpf.reference.edu.runtime.consumer.EduConsumerType;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ProcessEduBusinessConsumerTest {
    @TempDir
    Path repositoryRoot;

    @Test
    void streamsNormalOutputWithinTheBound() throws IOException {
        writeScript("normal.py", "print('bounded-output')\n");
        ProcessEduBusinessConsumer consumer =
                new ProcessEduBusinessConsumer(repositoryRoot, new ObjectMapper(), 128);

        EduBusinessConsumerResult result = consumer.invoke(binding("normal.py"), command(), 7L);

        assertThat(result.code()).isEqualTo("PROCESS_OK");
        assertThat(result.data())
                .containsEntry("script", "normal.py")
                .containsEntry("exitCode", 0);
        assertThat(result.data().get("outputDigest")).asString().hasSize(64);
    }

    @Test
    void terminatesWhenOutputExceedsTheBound() throws IOException {
        writeScript("oversize.py", "import sys\nsys.stdout.write('x' * 4096)\nsys.stdout.flush()\n");
        ProcessEduBusinessConsumer consumer =
                new ProcessEduBusinessConsumer(repositoryRoot, new ObjectMapper(), 128);

        Throwable failure = catchThrowable(
                () -> consumer.invoke(binding("oversize.py"), command(), 7L));

        assertThat(failure).isInstanceOf(IllegalStateException.class);
        assertThat(failure.getMessage()).contains("process output exceeds 128 bytes");
    }

    @Test
    void capturesBoundedErrorOutputAndSanitizesSecrets() throws IOException {
        writeScript("failure.py", "print('token=super-secret')\nraise SystemExit(7)\n");
        ProcessEduBusinessConsumer consumer =
                new ProcessEduBusinessConsumer(repositoryRoot, new ObjectMapper(), 128);

        Throwable failure = catchThrowable(
                () -> consumer.invoke(binding("failure.py"), command(), 7L));

        assertThat(failure).isInstanceOf(IllegalStateException.class);
        assertThat(failure.getMessage())
                .contains("script exit=7")
                .contains("token=***")
                .doesNotContain("super-secret");
    }

    private void writeScript(String name, String source) throws IOException {
        Files.writeString(repositoryRoot.resolve(name), source, StandardCharsets.UTF_8);
    }

    private EduConsumerBinding binding(String script) {
        return new EduConsumerBinding(
                "EDU-OPS-01",
                EduConsumerType.PROCESS,
                "cpf-reference",
                script,
                "RUN",
                "reference-process-contract",
                "run-process",
                "cpf.repository-root",
                10,
                List.of());
    }

    private EduExecutionCommand command() {
        return new EduExecutionCommand(
                "business-1",
                "idempotency-1",
                0,
                "fixture-operator",
                Set.of("CPF_REFERENCE_OPERATOR"),
                "REF",
                "bounded process output",
                "request-1",
                "trace-1",
                Map.of("sample", "value"),
                EduFailurePoint.NONE,
                false,
                false);
    }
}
