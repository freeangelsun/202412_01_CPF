package com.cpf.education.operations.runtime.consumer;
import com.cpf.education.operations.runtime.application.EduValidationException;
import com.cpf.education.operations.runtime.model.EduExecutionCommand;
import java.util.*;
/** Fail-closed registry: a binding without one concrete adapter cannot execute. */
/** EduBusinessConsumerRegistry 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class EduBusinessConsumerRegistry {
    private final Map<EduConsumerType,EduBusinessConsumer> consumers;
    public EduBusinessConsumerRegistry(Collection<? extends EduBusinessConsumer> values){
        Map<EduConsumerType,EduBusinessConsumer> map=new EnumMap<>(EduConsumerType.class);
        for(EduBusinessConsumer value:values){var old=map.put(value.type(),value);if(old!=null)throw new IllegalStateException("Duplicate EDU consumer: "+value.type());}
        consumers=Collections.unmodifiableMap(map);
    }
    /** invoke 작업을 CPF 표준 계약에 따라 수행한다. */
    public EduBusinessConsumerResult invoke(EduConsumerBinding binding,EduExecutionCommand command,long fencingToken){
        EduBusinessConsumer c=consumers.get(binding.type());
        if(c==null)throw new EduValidationException("No concrete product consumer registered for "+binding.requirementId()+" type="+binding.type());
        return c.invoke(binding,command,fencingToken);
    }
    public Set<EduConsumerType> registeredTypes(){return consumers.keySet();}
}
