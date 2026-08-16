package com.cpf.core.api.error;

import java.util.Map;

/** 예상 가능한 업무 규칙 위반입니다. DB Catalog용 오류 참조와 안전한 업무 fallback을 함께 운반합니다. */
public final class CpfBusinessException extends CpfException {
    public CpfBusinessException(String detail) {
        super(CpfErrorCode.BUSINESS_RULE_VIOLATION, detail);
    }

    public CpfBusinessException(CpfErrorCode fallback, String detail) {
        super(fallback, detail);
    }

    /** 동적 업무 오류 정의도 enum 추가 없이 동일한 Business Exception 경로로 전달합니다. */
    public CpfBusinessException(CpfErrorDefinition fallback, String detail) {
        super(fallback, detail);
    }

    /** CpfBusinessException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfBusinessException(CpfErrorCode fallback, String detail, Map<String, Object> arguments) {
        super(fallback, detail, arguments);
    }

    /** 동적/확장 업무 오류 정의와 메시지 인자를 함께 보존합니다. */
    public CpfBusinessException(CpfErrorDefinition fallback, String detail, Map<String, Object> arguments) {
        super(fallback, detail, arguments);
    }

    /**
     * 업무/기관 신규 오류코드는 Java enum을 추가하지 않고 이 reference만 전달합니다.
     * messageCode를 문자열로 합성하지 않으며 Common Error Catalog가 DB에서 해석합니다.
     */
    public CpfBusinessException(String errorReference, Map<String, Object> arguments) {
        super(errorReference, CpfErrorCode.BUSINESS_RULE_VIOLATION, null, arguments);
    }

    public CpfBusinessException(String errorReference, String detail, Map<String, Object> arguments) {
        super(errorReference, CpfErrorCode.BUSINESS_RULE_VIOLATION, detail, arguments);
    }
}
