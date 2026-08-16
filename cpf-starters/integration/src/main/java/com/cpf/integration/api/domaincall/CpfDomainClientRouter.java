package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.domain.CpfDomainBinding;
import com.cpf.core.api.domain.CpfDomainBindingMode;
import com.cpf.core.api.domain.CpfDomainBindingResolver;
import com.cpf.core.api.result.CpfResult;
import java.util.Objects;

/**
 * Generated Typed Domain Client의 단일 실행 Router입니다.
 * Business Source는 동일하고 AUTO/LOCAL/REMOTE 선택만 Runtime Binding이 담당합니다.
 */
public final class CpfDomainClientRouter {
    private final CpfDomainBindingResolver bindings;
    private final CpfDomainOperationRegistry localOperations;
    private final CpfDomainRemoteTransport remoteTransport;

    public CpfDomainClientRouter(
            CpfDomainBindingResolver bindings,
            CpfDomainOperationRegistry localOperations,
            CpfDomainRemoteTransport remoteTransport) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
        this.localOperations = Objects.requireNonNull(localOperations, "localOperations");
        this.remoteTransport = Objects.requireNonNull(remoteTransport, "remoteTransport");
    }

    /** Runtime Binding을 해석해 동일 typed contract로 Local 또는 Remote Operation을 실행합니다. */
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, I request, Class<O> responseType) {
        Objects.requireNonNull(request, "request");
        CpfDomainBinding binding = Objects.requireNonNull(bindings.resolve(systemCode), "Domain Binding");
        boolean localAvailable = localOperations.has(systemCode, operationId);
        if (binding.mode() == CpfDomainBindingMode.LOCAL || (binding.mode() == CpfDomainBindingMode.AUTO && localAvailable)) {
            if (!localAvailable) {
                return CpfResult.technicalFailure("CPF-DOMAIN-LOCAL-NOT-FOUND", systemCode + "/" + operationId + " local operation이 없습니다.");
            }
            return localOperations.invoke(systemCode, operationId, request, responseType);
        }
        if (binding.mode() == CpfDomainBindingMode.AUTO && (binding.serviceId() == null || binding.serviceId().isBlank())) {
            return CpfResult.technicalFailure("CPF-DOMAIN-BINDING-MISSING", systemCode + " remote serviceId가 없습니다.");
        }
        return remoteTransport.invoke(systemCode, operationId, binding, request, responseType);
    }
}
