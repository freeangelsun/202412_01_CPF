package com.cpf.education.base;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.api.CpfBaseService;
import java.util.Locale;

/**
 * Education 업무 Service가 재사용하는 2단계 Domain Base Service입니다.
 *
 * <p>EDU 예제의 TransactionId 보존과 업무 키 정규화 규칙을 공통화합니다. Retry/Recovery에서도
 * 새 TransactionId를 생성하지 않고 최초 inbound Context의 값을 그대로 사용하게 합니다.</p>
 */
public abstract class EducationBaseService extends CpfBaseService {
    /** Education 모듈의 표준 시스템 코드입니다. */
    protected static final String SYSTEM_CODE = "EDU";

    /**
     * 현재 CPF Context의 TransactionId를 검증하여 반환합니다.
     *
     * @return 최초 inbound에서 생성 또는 수용된 TransactionId
     */
    protected final String requireTransactionId() {
        return requireText(CpfContexts.requireCurrent().transactionId(), "transactionId");
    }

    /**
     * 교육 업무 키를 trim/대문자로 정규화하고 최대 길이를 제한합니다.
     *
     * @param value 입력 업무 키
     * @return 정규화된 업무 키
     */
    protected final String normalizeEducationKey(String value) {
        String normalized = requireText(value, "educationKey").toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw new com.cpf.core.api.error.CpfValidationException("educationKey는 64자 이하여야 합니다.");
        }
        return normalized;
    }
}
