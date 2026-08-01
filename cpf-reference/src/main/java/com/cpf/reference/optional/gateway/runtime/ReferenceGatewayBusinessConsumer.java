package com.cpf.reference.optional.gateway.runtime;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.consumer.jdbc.JdbcEduBusinessConsumer;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
import java.util.*;
/**
 * Product-independent REF Gateway simulator. It persists route/registry/health/rate state in refDB
 * through the shared JDBC business record instead of calling cpf-gateway or any generated domain.
 */
public final class ReferenceGatewayBusinessConsumer implements EduBusinessConsumer {
 private final JdbcEduBusinessConsumer jdbc;
 public ReferenceGatewayBusinessConsumer(JdbcEduBusinessConsumer jdbc){this.jdbc=Objects.requireNonNull(jdbc);}
 @Override public EduConsumerType type(){return EduConsumerType.REFERENCE_GATEWAY;}
 @Override public EduBusinessConsumerResult invoke(EduConsumerBinding binding,EduExecutionCommand command,long fencingToken){
  EduConsumerBinding jdbcBinding=new EduConsumerBinding(binding.requirementId(),EduConsumerType.JDBC_COMMAND,"cpf-reference",
    "CPF_EDU_BUSINESS_RECORD",binding.operation(),"cpf-reference REF Gateway simulator + refDB contract",
    binding.runtimeCommand(),binding.configurationKey(),binding.timeoutSeconds(),binding.argumentFields());
  EduBusinessConsumerResult stored=jdbc.invoke(jdbcBinding,command,fencingToken);
  Map<String,Object> data=new LinkedHashMap<>(stored.data());
  data.put("simulator","REFERENCE_GATEWAY");data.put("routeKey",command.businessKey());data.put("traceId",command.traceId());
  return EduBusinessConsumerResult.completed("REFERENCE_GATEWAY_COMMITTED",Map.copyOf(data));
 }
}
