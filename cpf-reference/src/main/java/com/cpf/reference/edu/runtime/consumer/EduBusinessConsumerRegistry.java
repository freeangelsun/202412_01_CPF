package com.cpf.reference.edu.runtime.consumer;
import com.cpf.reference.edu.runtime.application.EduValidationException;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import java.util.*;
/** Fail-closed registry: a binding without one concrete adapter cannot execute. */
public final class EduBusinessConsumerRegistry {
    private final Map<EduConsumerType,EduBusinessConsumer> consumers;
    public EduBusinessConsumerRegistry(Collection<? extends EduBusinessConsumer> values){
        Map<EduConsumerType,EduBusinessConsumer> map=new EnumMap<>(EduConsumerType.class);
        for(EduBusinessConsumer value:values){var old=map.put(value.type(),value);if(old!=null)throw new IllegalStateException("Duplicate EDU consumer: "+value.type());}
        consumers=Collections.unmodifiableMap(map);
    }
    public EduBusinessConsumerResult invoke(EduConsumerBinding binding,EduExecutionCommand command,long fencingToken){
        EduBusinessConsumer c=consumers.get(binding.type());
        if(c==null)throw new EduValidationException("No concrete product consumer registered for "+binding.requirementId()+" type="+binding.type());
        return c.invoke(binding,command,fencingToken);
    }
    public Set<EduConsumerType> registeredTypes(){return consumers.keySet();}
}
