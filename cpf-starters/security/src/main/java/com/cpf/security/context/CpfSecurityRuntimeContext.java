package com.cpf.security.context;

import java.time.Instant;
import java.util.Locale;

/** Security Owner 내부/운영용 인증 메타데이터입니다. Core Context generic component가 아닙니다. */
public record CpfSecurityRuntimeContext(
        String securityContextId,
        String authenticationMethod,
        String assuranceLevel,
        Instant authenticatedAt,
        String authorizationContextId,
        String policyVersion,
        String delegationId,
        String riskDecisionId,
        String trustZone) {
    /** Servlet 기반 Security Runtime이 현재 요청에 저장할 때 사용하는 Owner 전용 attribute key입니다. */
    public static final String REQUEST_ATTRIBUTE = CpfSecurityRuntimeContext.class.getName();
    public CpfSecurityRuntimeContext {
        for (String value : new String[]{securityContextId, authorizationContextId, delegationId, riskDecisionId}) {
            rejectCredential(value);
        }
    }
    private static void rejectCredential(String value) {
        if (value == null) return;
        String lower = value.toLowerCase(Locale.ROOT);
        String[] banned = {"bearer ", "basic ", "password:", "password=", "api-key=", "api_key=", "access_token=", "secret="};
        for (String marker : banned) {
            if (lower.contains(marker)) throw new IllegalArgumentException("credential material is forbidden in security context");
        }
    }
}
