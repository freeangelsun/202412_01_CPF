package com.cpf.reference.edu.runtime.consumer;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
/** Product-side executable consumer. Test doubles belong only in test source. */
public interface EduBusinessConsumer {
    EduConsumerType type();
    EduBusinessConsumerResult invoke(EduConsumerBinding binding, EduExecutionCommand command, long fencingToken);
}
