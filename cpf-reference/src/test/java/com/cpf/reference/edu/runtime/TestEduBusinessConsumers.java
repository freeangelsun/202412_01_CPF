package com.cpf.reference.edu.runtime;

import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;

import java.util.*;

/**
 * Test-scope-only deterministic product consumer doubles.
 * Product runtime never selects these consumers; they exist solely to test the
 * shared execution state machine without requiring external infrastructure.
 */
public final class TestEduBusinessConsumers {
    private TestEduBusinessConsumers() {}

    public static EduBusinessConsumerRegistry registry() {
        List<EduBusinessConsumer> consumers = new ArrayList<>();
        for (EduConsumerType type : EduConsumerType.values()) {
            consumers.add(new DeterministicConsumer(type));
        }
        return new EduBusinessConsumerRegistry(consumers);
    }

    private record DeterministicConsumer(EduConsumerType type) implements EduBusinessConsumer {
        @Override
        public EduBusinessConsumerResult invoke(EduConsumerBinding binding,
                                                EduExecutionCommand command,
                                                long fencingToken) {
            Map<String, Object> data = Map.of(
                    "requirementId", binding.requirementId(),
                    "consumerType", type.name(),
                    "entryPoint", binding.entryPoint(),
                    "businessKey", command.businessKey(),
                    "fencingToken", fencingToken);
            return type == EduConsumerType.OUTBOX
                    ? EduBusinessConsumerResult.pending("TEST_OUTBOX_PENDING", data)
                    : EduBusinessConsumerResult.completed("TEST_" + type.name() + "_OK", data);
        }
    }
}
