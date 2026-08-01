package com.cpf.reference.edu.runtime.consumer.outbox;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import java.util.*;
/** Declares a durable external effect. EduExecutionService persists the outbox in the same operation flow. */
public final class OutboxEduBusinessConsumer implements EduBusinessConsumer {
    @Override public EduConsumerType type(){return EduConsumerType.OUTBOX;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long fence){return EduBusinessConsumerResult.pending("OUTBOX_REQUIRED",Map.of("destination",b.entryPoint(),"eventType",b.operation(),"businessKey",c.businessKey(),"fencingToken",fence));}
}
