package com.cpf.backoffice.online.base;

import com.cpf.foundation.api.CpfBaseService;

/** MBW 업무 Service의 주제영역 공통 확장점입니다. */
public abstract class BackofficeBaseService extends CpfBaseService {

    /** 운영 작업 코드를 공통 형식으로 정규화합니다. */
    protected final String operationCode(String value) {
        return requireText(value, "operationCode").trim().toUpperCase(java.util.Locale.ROOT);
    }

    /** 위험 작업의 변경 사유를 필수·길이 경계로 검증합니다. */
    protected final String auditReason(String value) {
        String reason = requireText(value, "reason");
        if (reason.length() > 500) throw new IllegalArgumentException("reason length must be <= 500");
        return reason;
    }
}
