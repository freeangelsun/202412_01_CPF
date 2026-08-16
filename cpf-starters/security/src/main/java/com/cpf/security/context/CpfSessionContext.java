package com.cpf.security.context;

import java.time.Instant;

/** Security Owner가 관리하는 세션 상태입니다. Core Context에는 인증된 최소 identity만 반영합니다. 원문 Session ID는 저장하지 않습니다. */
public record CpfSessionContext(
        String sessionReference,
        long generation,
        String subjectId,
        Instant issuedAt,
        Instant lastAccessAt,
        Instant expiresAt,
        Instant absoluteExpiresAt,
        String securityContextId,
        String deviceId,
        State state) {
    /** BFF/Session Runtime이 현재 요청에 저장할 때 사용하는 Owner 전용 attribute key입니다. */
    public static final String REQUEST_ATTRIBUTE = CpfSessionContext.class.getName();
    public enum State { ACTIVE, REVOKED, EXPIRED }
}
