package com.cpf.backoffice.online.auth.dto;

import java.time.Instant;
import java.util.List;

/** MBW 인증 주체의 현재 계정·권한 상태를 노출하는 안전한 응답 계약입니다. */
public record BackofficeOperatorResponse(
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
    public BackofficeOperatorResponse {
        menus = menus == null ? List.of() : List.copyOf(menus);
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }
}
