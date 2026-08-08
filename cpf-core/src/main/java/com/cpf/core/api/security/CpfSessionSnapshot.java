package com.cpf.core.api.security;
import java.time.Instant; import java.util.Map;
public record CpfSessionSnapshot(String sessionId,String tenantId,String principalId,Instant createdAt,Instant lastAccessedAt,Instant expiresAt,long generation,boolean revoked,Map<String,String> attributes){ public CpfSessionSnapshot{attributes=attributes==null?Map.of():Map.copyOf(attributes);} }
