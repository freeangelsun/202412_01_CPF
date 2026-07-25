package com.cpf.admin.common.base;

import com.cpf.core.api.base.CpfBaseController;
import com.cpf.core.api.error.CpfValidationException;
import jakarta.servlet.http.HttpServletRequest;

/** ADM 운영 API Controller의 공통 보안 경계입니다. */
public abstract class AdmBaseController extends CpfBaseController {
    /** 인증 Filter가 검증한 운영자만 변경 주체로 사용합니다. Body/query fallback을 신뢰하지 않습니다. */
    protected String requireOperator(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute("adm.operatorId");
        if (!(value instanceof String operatorId) || operatorId.isBlank()) {
            throw new CpfValidationException("검증된 ADM 운영자 ID가 없어 변경 요청을 수행할 수 없습니다.");
        }
        return operatorId.trim();
    }

    protected String clientIp(HttpServletRequest request) {
        return request == null ? null : request.getRemoteAddr();
    }
}
