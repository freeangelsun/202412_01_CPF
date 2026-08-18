package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.result.CpfResult;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.cpf.integration.api.domaincall.CpfDomainOperation;
import com.cpf.integration.api.domaincall.CpfDomainOperationRegistry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Spring이 수집한 managed Domain Operation을 중복 없이 구성하고 Local/Remote 공통 정책 경계를 강제합니다. */
public final class CpfDefaultDomainOperationRegistry implements CpfDomainOperationRegistry {
    private final Map<String, CpfDomainOperation<?, ?>> operations;
    private final List<CpfOperationAccessPolicy> policies;

    public CpfDefaultDomainOperationRegistry(
            List<CpfDomainOperation<?, ?>> operations,
            List<CpfOperationAccessPolicy> policies) {
        Map<String, CpfDomainOperation<?, ?>> collected = new LinkedHashMap<>();
        for (CpfDomainOperation<?, ?> operation : operations == null ? List.<CpfDomainOperation<?, ?>>of() : operations) {
            String key = key(operation.systemCode(), operation.operationId());
            if (collected.putIfAbsent(key, operation) != null) {
                throw new IllegalStateException("Domain Operation 중복: " + key);
            }
        }
        this.operations = Map.copyOf(collected);
        this.policies = policies == null ? List.of() : List.copyOf(policies);
    }

    /** Test/standalone compatibility. 정책이 없으므로 실제 invoke는 fail-close합니다. */
    public CpfDefaultDomainOperationRegistry(List<CpfDomainOperation<?, ?>> operations) {
        this(operations, List.of());
    }

    @Override
    public boolean has(String systemCode, String operationId) {
        return operations.containsKey(key(systemCode, operationId));
    }

    CpfDomainOperation<?, ?> requireOperation(String systemCode, String operationId) {
        CpfDomainOperation<?, ?> operation = operations.get(key(systemCode, operationId));
        if (operation == null) {
            throw new IllegalArgumentException("Domain Operation을 찾을 수 없습니다: " + systemCode + "/" + operationId);
        }
        return operation;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            InvocationMetadata metadata,
            String systemCode,
            String operationId,
            I request,
            Class<O> responseType) {
        CpfDomainOperation<I, O> operation = (CpfDomainOperation<I, O>) requireOperation(systemCode, operationId);
        if (!operation.requestType().isInstance(request)) {
            throw new IllegalArgumentException("Domain request type 불일치: " + operation.requestType().getName());
        }
        if (!responseType.isAssignableFrom(operation.responseType())) {
            throw new IllegalArgumentException("Domain response type 불일치: " + operation.responseType().getName());
        }
        CpfResult<O> denied = verifyPolicy(metadata, operation);
        return denied == null ? operation.invoke(request) : denied;
    }

    private <O extends CpfResponse> CpfResult<O> verifyPolicy(
            InvocationMetadata metadata, CpfDomainOperation<?, ?> operation) {
        if (metadata == null) {
            return CpfResult.technicalFailure("OPERATION_POLICY_METADATA_MISSING",
                    "Canonical Domain invocation metadata is required.");
        }
        if (policies.size() != 1) {
            return CpfResult.technicalFailure("OPERATION_POLICY_UNAVAILABLE",
                    "Canonical operation access policy runtime is unavailable or ambiguous.");
        }
        CpfContext context;
        try {
            context = CpfContexts.requireCurrent();
        } catch (RuntimeException missing) {
            return CpfResult.technicalFailure("CPF-DOMAIN-CONTEXT-MISSING",
                    "Canonical CPF context is required before Domain policy evaluation.");
        }
        CpfOperationAccessPolicy.Decision decision = policies.getFirst().evaluate(
                new CpfOperationAccessPolicy.Request(
                        operation.operationId(),
                        metadata.callerSystemCode(),
                        operation.systemCode(),
                        context.callerChannel(),
                        metadata.authenticated(),
                        metadata.signed(),
                        metadata.trustedInternal()));
        if (decision.allowed()) return null;
        return CpfResult.technicalFailure(decision.reasonCode(),
                "Domain operation access policy denied request: " + decision.reasonCode());
    }

    private static String key(String systemCode, String operationId) {
        if (systemCode == null || systemCode.isBlank() || operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("systemCode/operationId는 필수입니다.");
        }
        return systemCode.trim().toUpperCase(Locale.ROOT) + ":" + operationId.trim().toLowerCase(Locale.ROOT);
    }
}
