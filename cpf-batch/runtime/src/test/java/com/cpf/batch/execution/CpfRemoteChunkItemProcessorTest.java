package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Processor가 Side Effect 없이 계약 검증·정규화만 수행하는지 검증합니다. */
class CpfRemoteChunkItemProcessorTest {
    private final CpfRemoteChunkItemProcessor processor = new CpfRemoteChunkItemProcessor();

    @Test
    void normalizesValidatedRemoteChunkDefinitionWithoutExecutingBusinessHandler() {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("stepId", "remote-step");
        input.put("executorType", "SERVICE_CALL");
        input.put("executorReference", "SERVICE:account-close");
        input.put("parameters", Map.of("mode", "dry-run"));
        input.put("partitionCount", 2);
        input.put("restartable", true);

        Map<String, Object> normalized = processor.process(input);

        assertThat(normalized)
                .containsEntry("stepId", "remote-step")
                .containsEntry("executorType", "SERVICE_CALL")
                .containsEntry("executorReference", "SERVICE:account-close")
                .containsEntry("partitionCount", 2)
                .containsEntry("restartable", true);
        assertThat(normalized.get("parameters")).isEqualTo(Map.of("mode", "dry-run"));
    }

    @Test
    void rejectsMissingOrInvalidPartitionContractBeforeWriterSideEffect() {
        Map<String, Object> invalid = new LinkedHashMap<>();
        invalid.put("stepId", "remote-step");
        invalid.put("executorType", "SERVICE_CALL");
        invalid.put("executorReference", "SERVICE:account-close");
        invalid.put("parameters", Map.of());
        invalid.put("partitionCount", 0);

        assertThatThrownBy(() -> processor.process(invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("partitionCount");
    }
}
