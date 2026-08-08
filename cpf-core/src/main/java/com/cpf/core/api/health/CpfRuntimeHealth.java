package com.cpf.core.api.health;
import java.time.Instant; import java.util.List; import java.util.Map;
public record CpfRuntimeHealth(String systemId,String instanceId,CpfHealthStatus liveness,CpfHealthStatus readiness,CpfHealthStatus startup,boolean draining,boolean maintenance,String version,String buildSha,Instant startedAt,long uptimeMillis,List<String> profiles,List<String> capabilities,List<CpfDependencyHealth> dependencies,Map<String,String> publicDiagnostics){
 public CpfRuntimeHealth { profiles=profiles==null?List.of():List.copyOf(profiles); capabilities=capabilities==null?List.of():List.copyOf(capabilities); dependencies=dependencies==null?List.of():List.copyOf(dependencies); publicDiagnostics=publicDiagnostics==null?Map.of():Map.copyOf(publicDiagnostics); }
}
