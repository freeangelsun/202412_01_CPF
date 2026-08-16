package com.cpf.admin.common.base;

import com.cpf.core.api.error.CpfValidationException;
import com.cpf.web.api.CpfBaseController;
import jakarta.servlet.http.HttpServletRequest;

/** ADM 운영 API Controller의 공통 인증·운영자 경계입니다. */
public abstract class AdmBaseController extends CpfBaseController {
    /** 인증 Filter가 검증한 운영자만 변경 주체로 사용합니다. */
    protected String requireOperator(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute("adm.operatorId");
        if (!(value instanceof String operatorId) || operatorId.isBlank()) {
            throw new CpfValidationException("검증된 ADM 운영자 ID가 없어 변경 요청을 수행할 수 없습니다.");
        }
        return operatorId.trim();
    }

    /** clientIp 작업을 CPF 표준 계약에 따라 수행한다. */
    protected String clientIp(HttpServletRequest request) {
        return request == null ? null : request.getRemoteAddr();
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
