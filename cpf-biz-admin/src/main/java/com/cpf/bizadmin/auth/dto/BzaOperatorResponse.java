package com.cpf.bizadmin.auth.dto;

import java.time.Instant;
import java.util.List;

/** BZA 인증 주체의 현재 계정·권한 상태를 노출하는 안전한 응답 계약입니다. */
public record BzaOperatorResponse(
        long operatorId,
        String loginId,
        String operatorName,
        String roleCode,
        String accountStatus,
        String useYn,
        String lockYn,
        int failCount,
        String passwordChangeRequiredYn,
        Instant passwordExpireAt,
        Instant lastLoginAt,
        List<String> menus,
        List<String> buttons) {
    public BzaOperatorResponse {
        menus = menus == null ? List.of() : List.copyOf(menus);
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }
}
