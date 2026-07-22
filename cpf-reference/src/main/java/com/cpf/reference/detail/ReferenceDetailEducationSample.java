package com.cpf.reference.detail;

import java.util.Map;

/**
 * 상세 조회에서 권한 확인과 마스킹을 함께 수행하는 샘플입니다.
 */
public class ReferenceDetailEducationSample {

    public Map<String, String> detail(boolean allowed, String email) {
        if (!allowed) {
            throw new IllegalArgumentException("상세 조회 권한이 없습니다.");
        }
        String masked = email.substring(0, 1) + "***" + email.substring(email.indexOf('@'));
        return Map.of("email", masked, "audit", "READ_DETAIL");
    }
}
