package com.cpf.batch.centercut.runtime;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.CenterCutHandler;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainClientRouter;
import com.cpf.integration.api.domaincall.CpfDomainPayload;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Objects;

/** DB Work Item을 CPF 공식 Domain Invocation Router로 실행하는 Center-Cut 업무 adapter입니다. */
public final class CpfDomainInvocationCenterCutHandler implements CenterCutHandler {
    public static final String HANDLER_KEY = "cpfDomainInvocationCenterCutHandler";
    private final CpfDomainClientRouter domains;
    private final ObjectMapper mapper;

    public CpfDomainInvocationCenterCutHandler(CpfDomainClientRouter domains, ObjectMapper mapper) {
        this.domains = Objects.requireNonNull(domains, "domains");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    @Override
    public String handlerKey() {
        return HANDLER_KEY;
    }

    @Override
    public Result handle(Context context) {
        Invocation invocation = parse(context.payload());
        CpfResult<CpfDomainPayload> outcome = domains.invoke(
                invocation.systemCode(), invocation.operationId(),
                new CpfDomainPayload(invocation.request()), CpfDomainPayload.class);
        if (outcome.isSuccess()) {
            return new Result("SUCCESS", json(outcome.requireData().values()),
                    "CPF Domain operation completed", false, false);
        }
        String detail = failureDetail(outcome);
        if (outcome.isUnknown()) {
            return new Result("UNKNOWN_RESULT", null, detail, false, true);
        }
        if (outcome.isTechnicalFailure()) {
            return new Result("RETRY", null, detail, true, false);
        }
        return new Result("FAILED", null, detail, false, false);
    }

    private Invocation parse(String payload) {
        try {
            Map<String, Object> value = mapper.readValue(payload, new TypeReference<>() { });
            String systemCode = requiredCode(value.get("systemCode"), "systemCode");
            String operationId = requiredCode(value.get("operationId"), "operationId");
            Object requestValue = value.get("request");
            if (!(requestValue instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("Center-Cut Domain request must be a JSON object");
            }
            Map<String, Object> request = mapper.convertValue(requestValue, new TypeReference<>() { });
            return new Invocation(systemCode, operationId, request);
        } catch (IllegalArgumentException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new IllegalArgumentException("Center-Cut Domain invocation payload is invalid", failure);
        }
    }

    private String json(Map<String, Object> value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception failure) {
            throw new IllegalStateException("CPF Domain response serialization failed", failure);
        }
    }

    private static String failureDetail(CpfResult<?> result) {
        String code = Objects.toString(result.errorCode(), "CPF-DOMAIN-FAILED");
        String message = SensitiveTextSanitizer.sanitize(result.errorMessage());
        return message == null || message.isBlank() ? code : code + ": " + message;
    }

    private static String requiredCode(Object value, String name) {
        String text = Objects.toString(value, "").trim();
        if (!text.matches("[A-Za-z][A-Za-z0-9._:-]{0,119}")) {
            throw new IllegalArgumentException("Invalid Center-Cut Domain " + name);
        }
        return text;
    }

    private record Invocation(String systemCode, String operationId, Map<String, Object> request) { }
}
