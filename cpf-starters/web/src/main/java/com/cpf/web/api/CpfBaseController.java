package com.cpf.web.api;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.validation.CpfValidation;
import java.net.URI;
import org.springframework.http.ResponseEntity;

/**
 * CPF Web/API Controller의 Framework 공통 확장점입니다.
 *
 * <p>Domain Common Base가 이 타입을 확장하고 Business Controller가 Domain Base를 확장합니다.
 * Web Context, Validation, 표준 Response와 비민감 실행 상관관계 helper를 제공합니다.</p>
 */
public abstract class CpfBaseController {
    protected CpfBaseController() { }

    protected final CpfContext requireCurrentContext() { return CpfContexts.requireCurrent(); }
    protected final CpfContextSnapshot requireContext() { return CpfContexts.requireSnapshot(); }
    protected final String requireText(String value, String fieldName) { return CpfValidation.requireText(value, fieldName); }
    /** requireValue 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final <T> T requireValue(T value, String fieldName) { return CpfValidation.requireValue(value, fieldName); }
    protected final void requireRule(boolean condition, String message) { CpfValidation.require(condition, message); }

    protected final <T> ResponseEntity<T> ok(T body) { return ResponseEntity.ok(body); }
    protected final <T> ResponseEntity<T> created(URI location, T body) {
        return ResponseEntity.created(requireValue(location, "location")).body(body);
    }
    /** noContent 작업을 CPF 표준 계약에 따라 수행한다. */
    protected final ResponseEntity<Void> noContent() { return ResponseEntity.noContent().build(); }

    /** Logging/Audit/Diagnostics가 공통으로 사용할 수 있는 비민감 실행 상관관계 값입니다. */
    protected final CpfWebExecutionFacts executionFacts(String operation) {
        CpfContext context = requireCurrentContext();
        return new CpfWebExecutionFacts(
                requireText(operation, "operation"),
                context.transactionId(), context.executionId(), context.actorId(), context.tenantId());
    }

    public record CpfWebExecutionFacts(
            String operation, String transactionId, String executionId, String actorId, String tenantId) { }
}
