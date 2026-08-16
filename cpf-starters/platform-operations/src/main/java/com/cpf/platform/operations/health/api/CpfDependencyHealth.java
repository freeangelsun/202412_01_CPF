package com.cpf.platform.operations.health.api;
import java.time.Instant; import java.util.Map;
/** CpfDependencyHealth 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfDependencyHealth(String capability,String dependency,CpfHealthStatus status,String reason,Instant checkedAt,long latencyMillis,Map<String,String> diagnostics){
 public CpfDependencyHealth { diagnostics=diagnostics==null?Map.of():Map.copyOf(diagnostics); }
}
