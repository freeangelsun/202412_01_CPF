package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Remote Domain Call의 내부 transport endpoint입니다.
 * 외부기관 API가 아니며 서비스 신원/내부 네트워크 보안 정책 아래에서만 노출해야 합니다.
 */
@RestController
@RequestMapping("/_cpf/domain")
public final class CpfDomainCallController {
    private final CpfDefaultDomainOperationRegistry registry;
    private final ObjectMapper objectMapper;
    private final CpfDomainInvocationGuard invocationGuard;
    public CpfDomainCallController(CpfDefaultDomainOperationRegistry registry, ObjectMapper objectMapper,
            CpfDomainInvocationGuard invocationGuard) {
        this.registry = registry; this.objectMapper = objectMapper; this.invocationGuard = invocationGuard;
    }

    @PostMapping("/{systemCode}/{operationId}")
    @SuppressWarnings({"rawtypes", "unchecked"})
    public CpfDomainRemoteEnvelope invoke(
            @PathVariable String systemCode, @PathVariable String operationId, @RequestBody JsonNode payload,
            HttpServletRequest servletRequest) {
        CpfDomainOperation operation = registry.requireOperation(systemCode, operationId);
        invocationGuard.verify(servletRequest, operation);
        CpfRequest request = (CpfRequest) objectMapper.convertValue(payload, operation.requestType());
        CpfResult<?> result = operation.invoke(request);
        return CpfDomainRemoteEnvelope.from(result);
    }
}
