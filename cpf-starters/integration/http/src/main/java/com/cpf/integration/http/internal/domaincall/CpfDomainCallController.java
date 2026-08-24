package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
            @PathVariable String systemCode, @PathVariable String operationId, @RequestBody byte[] wirePayload,
            HttpServletRequest servletRequest) {
        CpfDomainOperation operation = registry.requireOperation(systemCode, operationId);
        var metadata = invocationGuard.verify(servletRequest, operation);
        JsonNode payload = parsePayload(wirePayload);
        CpfRequest request = (CpfRequest) objectMapper.convertValue(payload, operation.requestType());
        CpfResult<?> result = registry.invoke(metadata, systemCode, operationId, request, operation.responseType());
        return CpfDomainRemoteEnvelope.from(result);
    }

    private JsonNode parsePayload(byte[] wirePayload) {
        if (wirePayload == null || wirePayload.length == 0) throw malformedPayload(null);
        try {
            JsonNode payload = objectMapper.readTree(wirePayload);
            if (payload == null || !payload.isObject()) throw malformedPayload(null);
            return payload;
        } catch (IOException failure) {
            throw malformedPayload(failure);
        }
    }

    private static ResponseStatusException malformedPayload(Throwable cause) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "CPF Domain request payload must be one valid JSON object.", cause);
    }
}
