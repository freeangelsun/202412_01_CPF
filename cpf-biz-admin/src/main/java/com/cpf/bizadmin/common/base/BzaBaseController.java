package com.cpf.bizadmin.common.base;

import com.cpf.web.api.CpfBaseController;

/**
 * BZA Business Controller가 재사용하는 실제 Domain Base 동작입니다.
 * operator/reason 및 paging 경계를 공통 검증하여 Controller별 구현 편차를 막습니다.
 */
public abstract class BzaBaseController extends CpfBaseController {
    protected final String requiredOperator(String value) { return requireText(value, "operatorId"); }
    protected final String requiredReason(String value) {
        String reason = requireText(value, "reason");
        if (reason.length() > 500) throw new IllegalArgumentException("reason length must be <= 500");
        return reason;
    }
    /** page 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final int page(Integer value) { return value == null ? 0 : Math.max(0, value); }
    protected final int size(Integer value) {
        int resolved = value == null ? 50 : value;
        if (resolved < 1 || resolved > 200) throw new IllegalArgumentException("size must be 1..200");
        return resolved;
    }

    /** 관리 작업의 CPF 실행 상관관계를 감사/진단에 전달합니다. */
    protected final CpfWebExecutionFacts operationFacts(String operation) {
        return executionFacts(operationCode(operation));
    }

    /** 관리 작업 코드를 공통 형식으로 정규화합니다. */
    protected final String operationCode(String value) {
        return requireText(value, "operationCode").trim().toUpperCase(java.util.Locale.ROOT);
    }
}
