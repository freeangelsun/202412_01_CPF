package com.cpf.core.api.health;
import java.time.Instant; import java.util.Map;
public record CpfDependencyHealth(String capability,String dependency,CpfHealthStatus status,String reason,Instant checkedAt,long latencyMillis,Map<String,String> diagnostics){
 public CpfDependencyHealth { diagnostics=diagnostics==null?Map.of():Map.copyOf(diagnostics); }
}
