package com.cpf.education.operations.runtime.consumer.outbox;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.EduExecutionCommand;
import java.util.*;
/** Declares a durable external effect. EduExecutionService persists the outbox in the same operation flow. */
/** OutboxEduBusinessConsumer 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class OutboxEduBusinessConsumer implements EduBusinessConsumer {
    @Override public EduConsumerType type(){return EduConsumerType.OUTBOX;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long fence){return EduBusinessConsumerResult.pending("OUTBOX_REQUIRED",Map.of("destination",b.entryPoint(),"eventType",b.operation(),"businessKey",c.businessKey(),"fencingToken",fence));}
}
