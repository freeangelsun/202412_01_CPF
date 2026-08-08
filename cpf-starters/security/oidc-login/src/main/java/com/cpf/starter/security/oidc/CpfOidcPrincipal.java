package com.cpf.starter.security.oidc;
import java.util.*;
/** OIDC provider claim을 CPF user/tenant/authority context로 정규화한 snapshot입니다. */
public record CpfOidcPrincipal(String userId,String tenantId,Set<String> authorities,Map<String,Object> safeClaims){public CpfOidcPrincipal{if(userId==null||userId.isBlank())throw new IllegalArgumentException("userId required");authorities=authorities==null?Set.of():Set.copyOf(authorities);safeClaims=safeClaims==null?Map.of():Map.copyOf(safeClaims);} }
