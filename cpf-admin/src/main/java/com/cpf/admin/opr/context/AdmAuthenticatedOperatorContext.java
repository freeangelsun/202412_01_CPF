package com.cpf.admin.opr.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ADM 인증 필터가 검증한 현재 운영자만 하위 Owner 호출에 제공합니다.
 */
@Component
public class AdmAuthenticatedOperatorContext {
    public String currentOperatorId() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            Object operatorId = request.getAttribute("adm.operatorId");
            if (operatorId instanceof String value && !value.isBlank()) {
                return value.trim();
            }
        }
        throw new IllegalStateException("검증된 ADM operator context가 필요합니다.");
    }
}
