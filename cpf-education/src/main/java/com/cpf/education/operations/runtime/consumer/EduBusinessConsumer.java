package com.cpf.education.operations.runtime.consumer;
import com.cpf.education.operations.runtime.model.EduExecutionCommand;
/** Product-side executable consumer. Test doubles belong only in test source. */
/** EduBusinessConsumer 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface EduBusinessConsumer {
    EduConsumerType type();
    EduBusinessConsumerResult invoke(EduConsumerBinding binding, EduExecutionCommand command, long fencingToken);
}
