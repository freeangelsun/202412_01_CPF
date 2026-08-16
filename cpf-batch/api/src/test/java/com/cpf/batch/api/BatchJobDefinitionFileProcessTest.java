package com.cpf.batch.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class BatchJobDefinitionFileProcessTest {
    @Test
    void fileProcessUsesPublishedProcessorReference() {
        BatchJobDefinition definition = definition("PROCESSOR:REF_CSV_COUNT", validParameters());
        assertEquals("REF_CSV_COUNT", definition.processorId());
    }

    @Test
    void fileProcessRejectsMissingSourceSchema() {
        assertThrows(IllegalArgumentException.class,
                () -> definition("PROCESSOR:REF_CSV_COUNT", List.of(
                        parameter("sourceAlias", "PATH_ALIAS", true))));
    }

    @Test
    void fileProcessRejectsFreeFormExecutorReference() {
        assertThrows(IllegalArgumentException.class,
                () -> definition("REF_CSV_COUNT", validParameters()));
    }

    private static BatchJobDefinition definition(
            String executorReference, List<BatchParameterDefinition> parameters) {
        return new BatchJobDefinition(
                "REF.FILE.COUNT", 1L, "Reference CSV Count",
                BatchJobDefinition.ExecutorType.FILE_PROCESS,
                BatchJobDefinition.State.DRAFT,
                "REF", "Reference FILE_PROCESS sample",
                new BatchJobDefinition.Trigger(
                        BatchJobDefinition.TriggerType.MANUAL, "", "Asia/Seoul",
                        BatchJobDefinition.MisfirePolicy.FAIL_CLOSED, true),
                parameters, List.of(), BatchJobDefinition.ResourcePolicy.defaults(),
                BatchJobDefinition.RecoveryPolicy.defaults(),
                BatchJobDefinition.AlertPolicy.defaults(),
                executorReference, "", "qa31", "FILE_PROCESS validation test",
                null, null, 0L);
    }

    private static List<BatchParameterDefinition> validParameters() {
        return List.of(
                parameter("sourceAlias", "PATH_ALIAS", true),
                parameter("sourcePath", "STRING", true));
    }

    private static BatchParameterDefinition parameter(String name, String type, boolean required) {
        return new BatchParameterDefinition(
                name, type, required, null, false, false,
                name, "", List.of(), "", "", "", null, null,
                type.endsWith("REFERENCE") || "PATH_ALIAS".equals(type) ? type : "", "", true);
    }
}
