package com.cpf.batch.control.deploy;

import com.cpf.batch.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.*;

@Service
public class RuntimeLifecycleService {
 private final JdbcTemplate jdbc; private final RestClient.Builder builder;
 public RuntimeLifecycleService(JdbcTemplate jdbc,RestClient.Builder builder){this.jdbc=jdbc;this.builder=builder;}
 public AgentCommandResult operate(String instanceId,String operation,String requestedBy,String reason){
  require(instanceId,"instanceId");require(requestedBy,"requestedBy");require(reason,"reason");String op=operation.toLowerCase(Locale.ROOT);
  if(!Set.of("start","stop","restart","drain","resume","rollback","status").contains(op))throw new IllegalArgumentException("Unsupported runtime operation: "+operation);
  Map<String,Object> row=jdbc.queryForMap("SELECT di.agent_base_url,dc.service_id FROM bat_deployment_instance di JOIN bat_deployment_cell dc ON dc.cell_id=di.cell_id WHERE di.instance_id=?",instanceId);
  String agent=Objects.toString(row.get("agent_base_url"),"");String service=Objects.toString(row.get("service_id"),"");
  if(agent.isBlank()||service.isBlank())throw new IllegalStateException("Deployment inventory is incomplete for "+instanceId);
  try{
   AgentCommandResult result=builder.baseUrl(agent).build().post().uri("/api/v1/agent/services/{service}/{op}",service,op).retrieve().body(AgentCommandResult.class);
   return result==null?unknown(service,op,"NO_RESULT"):result;
  }catch(RuntimeException e){return unknown(service,op,"TRANSPORT_UNKNOWN");}
 }
 private static AgentCommandResult unknown(String service,String op,String code){Instant now=Instant.now();return new AgentCommandResult(UUID.randomUUID().toString(),service,op,CommandState.UNKNOWN_RESULT,code,"Agent result is unknown; reconcile before retry",null,now,now);}
 private static void require(String v,String f){if(v==null||v.isBlank())throw new IllegalArgumentException(f+" is required");}
}
