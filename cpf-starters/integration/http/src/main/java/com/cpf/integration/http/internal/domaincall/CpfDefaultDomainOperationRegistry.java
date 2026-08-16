package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.result.CpfResult;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.integration.api.domaincall.CpfDomainOperationRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Spring이 수집한 managed Domain Operation을 중복 없이 Runtime Registry로 구성합니다. */
public final class CpfDefaultDomainOperationRegistry implements CpfDomainOperationRegistry {
    private final Map<String, CpfDomainOperation<?, ?>> operations;
    public CpfDefaultDomainOperationRegistry(List<CpfDomainOperation<?, ?>> operations) {
        Map<String, CpfDomainOperation<?, ?>> collected = new LinkedHashMap<>();
        for (CpfDomainOperation<?, ?> operation : operations == null ? List.<CpfDomainOperation<?, ?>>of() : operations) {
            String key = key(operation.systemCode(), operation.operationId());
            if (collected.putIfAbsent(key, operation) != null) throw new IllegalStateException("Domain Operation 중복: " + key);
        }
        this.operations = Map.copyOf(collected);
    }
    @Override public boolean has(String systemCode, String operationId) { return operations.containsKey(key(systemCode, operationId)); }

    CpfDomainOperation<?, ?> requireOperation(String systemCode, String operationId) {
        CpfDomainOperation<?, ?> operation = operations.get(key(systemCode, operationId));
        if (operation == null) throw new IllegalArgumentException("Domain Operation을 찾을 수 없습니다: " + systemCode + "/" + operationId);
        return operation;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, I request, Class<O> responseType) {
        CpfDomainOperation<I, O> operation = (CpfDomainOperation<I, O>) requireOperation(systemCode, operationId);
        if (!operation.requestType().isInstance(request)) throw new IllegalArgumentException("Domain request type 불일치: " + operation.requestType().getName());
        if (!responseType.isAssignableFrom(operation.responseType())) throw new IllegalArgumentException("Domain response type 불일치: " + operation.responseType().getName());
        return operation.invoke(request);
    }

    private static String key(String systemCode, String operationId) {
        if (systemCode == null || systemCode.isBlank() || operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("systemCode/operationId는 필수입니다.");
        }
        return systemCode.trim().toUpperCase(Locale.ROOT) + ":" + operationId.trim().toLowerCase(Locale.ROOT);
    }
}
