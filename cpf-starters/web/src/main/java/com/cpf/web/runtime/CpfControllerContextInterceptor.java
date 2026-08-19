package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import java.util.List;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfOperationIdResolver;
import com.cpf.web.context.CpfRuntimeIdentity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** Enforces controller context and binds the resolved canonical operation before business code executes. */
public final class CpfControllerContextInterceptor implements HandlerInterceptor {
    private static final String OPERATION_SCOPE_ATTRIBUTE = CpfControllerContextInterceptor.class.getName() + ".operationScope";

    private final CpfControllerPolicyProperties properties;
    private final CpfOperationIdResolver operationIds;
    private final CpfRuntimeIdentity runtime;
    private final List<CpfOperationAccessPolicy> accessPolicies;

    public CpfControllerContextInterceptor(CpfControllerPolicyProperties properties,
            CpfOperationIdResolver operationIds, CpfRuntimeIdentity runtime, List<CpfOperationAccessPolicy> accessPolicies) {
        this.properties = properties; this.operationIds = operationIds; this.runtime = runtime;
        this.accessPolicies = accessPolicies == null ? List.of() : List.copyOf(accessPolicies);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isEnabled() || !(handler instanceof HandlerMethod method)) return true;
        CpfRestController annotation = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), CpfRestController.class);
        CpfContext currentContext = CpfContexts.current();
        if (annotation != null && currentContext == null) {
            throw new IllegalStateException("Managed @CpfRestController request has no bound CPF Context: " + method.getBeanType().getName());
        }
        if (currentContext == null) return true;

        String resolvedOperation = resolvedTargetOperation(request, method);
        Object trust = request.getAttribute(CpfWebContextFilter.INGRESS_TRUST_ATTRIBUTE);
        boolean trustedInternal = trust == CpfHttpIngressTrust.TRUSTED_INTERNAL;
        if (trustedInternal) {
            Object raw = request.getAttribute(CpfWebContextFilter.RECEIVED_HEADERS_ATTRIBUTE);
            if (!(raw instanceof CpfHttpHeaders headers)) {
                throw new IllegalStateException("Trusted internal request has no captured CPF headers");
            }
            String currentSystem = runtime.systemCode();
            assertSystem(headers.getRequired(CpfHttpHeaderNames.SYSTEM_CODE), currentSystem,
                    CpfHttpHeaderNames.SYSTEM_CODE, "SYSTEM_CODE_MISMATCH");
            assertSystem(headers.getRequired(CpfHttpHeaderNames.TARGET_SYSTEM_CODE), currentSystem,
                    CpfHttpHeaderNames.TARGET_SYSTEM_CODE, "TARGET_SYSTEM_CODE_MISMATCH");

            String declared = headers.getRequired(CpfHttpHeaderNames.TARGET_OPERATION_ID);
            if (!resolvedOperation.equals(declared)) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.TARGET_OPERATION_ID,
                        "Target operation header does not match the resolved handler operationId.",
                        409, "TARGET_OPERATION_MISMATCH");
            }
        }

        if (annotation != null) enforceOperationPolicy(request, currentContext, resolvedOperation, trustedInternal);

        CpfContext resolvedContext = currentContext.withResolvedOperation(resolvedOperation, method.getMethod().getName());
        AutoCloseable scope = CpfContexts.bind(CpfContextSnapshot.capture(resolvedContext));
        request.setAttribute(OPERATION_SCOPE_ATTRIBUTE, scope);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        Object scope = request.getAttribute(OPERATION_SCOPE_ATTRIBUTE);
        request.removeAttribute(OPERATION_SCOPE_ATTRIBUTE);
        if (scope instanceof AutoCloseable closeable) closeable.close();
    }

    private void enforceOperationPolicy(HttpServletRequest request, CpfContext context, String operationId, boolean trustedInternal) {
        if (accessPolicies.size() != 1) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Canonical operation access policy runtime is unavailable or ambiguous.", 503, "OPERATION_POLICY_UNAVAILABLE");
        }
        boolean authenticated = context.identity() != null
                && context.identity().principalType() != CpfContext.CpfPrincipalType.ANONYMOUS;
        String trustedCallerSystem = stringAttribute(request, CpfWebContextFilter.VERIFIED_CALLER_SYSTEM_ATTRIBUTE);
        CpfOperationAccessPolicy.Decision decision = accessPolicies.getFirst().evaluate(new CpfOperationAccessPolicy.Request(
                operationId,
                trustedInternal ? trustedCallerSystem : null,
                runtime.systemCode(),
                context.callerChannel(),
                authenticated, false, trustedInternal));
        if (!decision.allowed()) {
            int status = "CALLER_NOT_REGISTERED".equals(decision.reasonCode()) || "CALLER_DISABLED".equals(decision.reasonCode()) ? 403 : 409;
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID, "Operation access policy denied request: "+decision.reasonCode(),
                    status, decision.reasonCode());
        }
        request.setAttribute("cpf.operation.policy-version", decision.policyVersion());
        request.setAttribute("cpf.operation.policy-decision", "ALLOW");
    }

    private String resolvedTargetOperation(HttpServletRequest request, HandlerMethod method) {
        String uri = request.getRequestURI();
        String marker = "/_cpf/domain/";
        int index = uri == null ? -1 : uri.indexOf(marker);
        if (index >= 0) {
            String tail = uri.substring(index + marker.length());
            String[] segments = tail.split("/");
            if (segments.length >= 2 && !segments[1].isBlank()) return segments[1];
        }
        return operationIds.resolve(method);
    }

    private void assertSystem(String headerValue, String expected, String header, String category) {
        if (!expected.equalsIgnoreCase(headerValue)) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    header, "Target/current System header does not match this runtime.", 409, category);
        }
    }

    private static String stringAttribute(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value instanceof String text && !text.isBlank() ? text.trim() : null;
    }
}
