package com.cpf.integration.http.internal.domaincall;

import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.web.context.CpfRequestOperationResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

/** Generic Domain transport가 요청 문자열보다 실제 Domain Registry를 먼저 해석하도록 하는 resolver입니다. */
final class CpfDomainRequestOperationResolver implements CpfRequestOperationResolver {
    private final CpfDefaultDomainOperationRegistry registry;

    CpfDomainRequestOperationResolver(CpfDefaultDomainOperationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String resolve(HttpServletRequest request, HandlerMethod handlerMethod) {
        if (!CpfDomainCallController.class.isAssignableFrom(handlerMethod.getBeanType())) return null;
        Object attributes = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attributes instanceof Map<?, ?> variables)) {
            throw new IllegalArgumentException("Domain transport path variables are unavailable before policy evaluation.");
        }
        String systemCode = text(variables.get("systemCode"));
        String requestedOperationId = text(variables.get("operationId"));
        CpfDomainOperation<?, ?> actual = registry.requireOperation(systemCode, requestedOperationId);
        return actual.operationId();
    }

    private static String text(Object value) {
        if (value == null || String.valueOf(value).isBlank()) throw new IllegalArgumentException("Domain systemCode/operationId is required.");
        return String.valueOf(value).trim();
    }
}
