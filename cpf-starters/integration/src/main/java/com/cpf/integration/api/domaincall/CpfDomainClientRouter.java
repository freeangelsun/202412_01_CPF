package com.cpf.integration.api.domaincall;

import com.cpf.core.api.base.CpfRequest;
import com.cpf.core.api.base.CpfResponse;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
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
        return invoke(systemCode, operationId, request, responseType, CpfDomainCallOptions.none());
    }

    /** 상대 Domain과 합의된 선택 Header를 Framework Source 변경 없이 전달합니다. */
    public <I extends CpfRequest, O extends CpfResponse> CpfResult<O> invoke(
            String systemCode, String operationId, I request, Class<O> responseType, CpfDomainCallOptions options) {
        Objects.requireNonNull(request, "request");
        CpfDomainCallOptions effectiveOptions = options == null ? CpfDomainCallOptions.none() : options;
        CpfDomainBinding binding = Objects.requireNonNull(bindings.resolve(systemCode), "Domain Binding");
        boolean localAvailable = localOperations.has(systemCode, operationId);
        if (binding.mode() == CpfDomainBindingMode.LOCAL || (binding.mode() == CpfDomainBindingMode.AUTO && localAvailable)) {
            if (!localAvailable) {
                return CpfResult.technicalFailure("CPF-DOMAIN-LOCAL-NOT-FOUND", systemCode + "/" + operationId + " local operation이 없습니다.");
            }
            var current = CpfContexts.current();
            if (current == null) return localOperations.invoke(systemCode, operationId, request, responseType);
            var localHop = current.localDomainHop(systemCode, operationId);
            try (AutoCloseable ignored = CpfContexts.bind(CpfContextSnapshot.capture(localHop))) {
                return localOperations.invoke(systemCode, operationId, request, responseType);
            // 원격 Domain 호출 실패의 원래 의미를 보존해 표준 CPF 호출 오류·UNKNOWN 판정 경계로 전달합니다.
            } catch (RuntimeException e) {
                throw e;
            // 원격 Domain 호출 실패의 원래 의미를 보존해 표준 CPF 호출 오류·UNKNOWN 판정 경계로 전달합니다.
            } catch (Exception e) {
                throw new IllegalStateException("CPF local Domain context restore failed", e);
            }
        }
        if (binding.mode() == CpfDomainBindingMode.AUTO && (binding.serviceId() == null || binding.serviceId().isBlank())) {
            return CpfResult.technicalFailure("CPF-DOMAIN-BINDING-MISSING", systemCode + " remote serviceId가 없습니다.");
        }
        var current = CpfContexts.current();
        if (current == null) {
            return remoteTransport.invoke(systemCode, operationId, binding, request, responseType, effectiveOptions);
        }
        var outbound = current.withTargetOperation(operationId);
        try (AutoCloseable ignored = CpfContexts.bind(CpfContextSnapshot.capture(outbound))) {
            return remoteTransport.invoke(systemCode, operationId, binding, request, responseType, effectiveOptions);
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 현재 Operation과 Target Operation을 분리하고 CPF Domain Client를 사용하는 내부 Domain 호출 Golden Path의 정책을 유지합니다.
        } catch (RuntimeException e) {
            throw e;
        // 실패·동시성·복구 경계에서도 원래 의미를 잃지 않도록 현재 Operation과 Target Operation을 분리하고 CPF Domain Client를 사용하는 내부 Domain 호출 Golden Path의 정책을 유지합니다.
        } catch (Exception e) {
            throw new IllegalStateException("CPF remote Domain context restore failed", e);
        }
    }
}
