package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.web.api.CpfController;
import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import java.util.List;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfOperationIdResolver;
import com.cpf.web.context.CpfOperationOwnerResolver;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.context.CpfRequestOperationResolver;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** Enforces controller context and binds the resolved canonical operation before business code executes. */
@SuppressWarnings("deprecation")
public final class CpfControllerContextInterceptor implements HandlerInterceptor {
    private static final String OPERATION_SCOPE_ATTRIBUTE = CpfControllerContextInterceptor.class.getName() + ".operationScope";

    private final CpfControllerPolicyProperties properties;
    private final CpfOperationIdResolver operationIds;
    private final CpfRuntimeIdentity runtime;
    private final List<CpfOperationAccessPolicy> accessPolicies;
    private final List<CpfRequestOperationResolver> requestOperationResolvers;
    private final List<CpfOperationOwnerResolver> operationOwnerResolvers;

    public CpfControllerContextInterceptor(CpfControllerPolicyProperties properties,
            CpfOperationIdResolver operationIds, CpfRuntimeIdentity runtime, List<CpfOperationAccessPolicy> accessPolicies) {
        this(properties, operationIds, runtime, accessPolicies, List.of());
    }

    public CpfControllerContextInterceptor(CpfControllerPolicyProperties properties,
            CpfOperationIdResolver operationIds, CpfRuntimeIdentity runtime, List<CpfOperationAccessPolicy> accessPolicies,
            List<CpfRequestOperationResolver> requestOperationResolvers) {
        this(properties, operationIds, runtime, accessPolicies, requestOperationResolvers, List.of());
    }

    public CpfControllerContextInterceptor(CpfControllerPolicyProperties properties,
            CpfOperationIdResolver operationIds, CpfRuntimeIdentity runtime, List<CpfOperationAccessPolicy> accessPolicies,
            List<CpfRequestOperationResolver> requestOperationResolvers,
            List<CpfOperationOwnerResolver> operationOwnerResolvers) {
        this.properties = properties; this.operationIds = operationIds; this.runtime = runtime;
        this.accessPolicies = accessPolicies == null ? List.of() : List.copyOf(accessPolicies);
        this.requestOperationResolvers = requestOperationResolvers == null ? List.of() : List.copyOf(requestOperationResolvers);
        this.operationOwnerResolvers = operationOwnerResolvers == null ? List.of() : List.copyOf(operationOwnerResolvers);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isEnabled() || !(handler instanceof HandlerMethod method)) return true;
        boolean managed = AnnotatedElementUtils.hasAnnotation(method.getBeanType(), CpfController.class)
                || AnnotatedElementUtils.hasAnnotation(method.getBeanType(), CpfRestController.class);
        CpfContext currentContext = CpfContexts.current();
        if (managed && currentContext == null) {
            throw new IllegalStateException("Managed @CpfController request has no bound CPF Context: " + method.getBeanType().getName());
        }
        if (currentContext == null) return true;

        String resolvedOperation = resolvedTargetOperation(request, method);
        String ownerSystem = resolvedOwnerSystem(method, resolvedOperation);
        boolean businessTransaction = isBusinessTransaction(method);
        if (businessTransaction && ownerSystem == null) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Business operation owner SystemCode is unavailable; topology must use an explicit owner descriptor.",
                    503, "OPERATION_OWNER_UNRESOLVED");
        }
        Object trust = request.getAttribute(CpfWebContextFilter.INGRESS_TRUST_ATTRIBUTE);
        boolean trustedInternal = trust == CpfHttpIngressTrust.TRUSTED_INTERNAL;
        if (managed) {
            Object raw = request.getAttribute(CpfWebContextFilter.RECEIVED_HEADERS_ATTRIBUTE);
            if (!(raw instanceof CpfHttpHeaders headers)) {
                throw new IllegalStateException("Managed CPF request has no captured CPF headers");
            }
            // System 정합 검증은 **업무 Domain 거래에만** 적용한다(Harness 30.16).
            // ADM(Platform Control Plane)/Gateway/Channel Front/1-WAS topology 는 canonical SystemCode 를
            // 가지지 않으며, 없다는 이유로 가상 값을 만들지 않는다. 그 Component 들의 lineage 는
            // 정본 ChannelCode 계약이 담당하므로 여기서 System 을 대조하지 않는다.
            if (ownerSystem != null && !ownerSystem.isBlank()) {
                assertSystem(headers.getRequired(CpfHttpHeaderNames.SYSTEM_CODE), ownerSystem,
                        CpfHttpHeaderNames.SYSTEM_CODE, "SYSTEM_CODE_MISMATCH");
                assertSystem(headers.getRequired(CpfHttpHeaderNames.TARGET_SYSTEM_CODE), ownerSystem,
                        CpfHttpHeaderNames.TARGET_SYSTEM_CODE, "TARGET_SYSTEM_CODE_MISMATCH");
            } else {
                // ADM/Gateway/Channel Front처럼 SystemCode가 없는 Owner에 Business System6을
                // 실어 통과시키면 가상 SystemCode False Green이 된다. Systemless ingress는
                // adapter가 전부 비었을 때만 만들고, controller에서도 실제 Owner 기준으로 재확인한다.
                rejectUnexpectedSystemMetadata(headers);
            }

            String declared = headers.getRequired(CpfHttpHeaderNames.TARGET_OPERATION_ID);
            if (!resolvedOperation.equals(declared)) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.TARGET_OPERATION_ID,
                        "Target operation header does not match the resolved handler operationId.",
                        409, "TARGET_OPERATION_MISMATCH");
            }
        }

        if (managed) enforceOperationPolicy(request, currentContext, resolvedOperation, ownerSystem, trustedInternal);

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

    private void enforceOperationPolicy(HttpServletRequest request, CpfContext context, String operationId,
            String ownerSystem, boolean trustedInternal) {
        if (accessPolicies.size() != 1) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.TARGET_OPERATION_ID,
                    "Canonical operation access policy runtime is unavailable or ambiguous.", 503, "OPERATION_POLICY_UNAVAILABLE");
        }
        boolean authenticated = context.identity() != null
                && context.identity().principalType() != CpfContext.CpfPrincipalType.ANONYMOUS;
        String trustedCallerSystem = stringAttribute(request, CpfWebContextFilter.VERIFIED_CALLER_SYSTEM_ATTRIBUTE);
        // Operation Access Policy 는 **target System 을 키로 하는 업무 거래 정책**이다(Harness 30.16).
        // SystemCode 를 가지지 않는 Component(ADM Platform Control Plane / Gateway / Channel Front /
        // 1-WAS topology)는 이 정책의 대상이 아니며, 그 Component 의 접근통제는 자기 계층이 소유한다
        // (예: ADM 은 AdmApiAuthFilter 의 세션/권한). 없다는 이유로 가상 SystemCode 를 만들지 않는다.
        if (ownerSystem == null || ownerSystem.isBlank()) {
            return;
        }
        CpfOperationAccessPolicy.Decision decision = accessPolicies.getFirst().evaluate(new CpfOperationAccessPolicy.Request(
                operationId,
                trustedInternal ? trustedCallerSystem : null,
                ownerSystem,
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
        String resolved = null;
        for (CpfRequestOperationResolver resolver : requestOperationResolvers) {
            String candidate = resolver.resolve(request, method);
            if (candidate == null || candidate.isBlank()) continue;
            candidate = candidate.trim();
            if (resolved != null && !resolved.equals(candidate)) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.TARGET_OPERATION_ID,
                        "Multiple request operation resolvers returned conflicting canonical operationIds.",
                        503, "OPERATION_RESOLVER_AMBIGUOUS");
            }
            resolved = candidate;
        }
        return resolved != null ? resolved : operationIds.resolve(method);
    }

    private String resolvedOwnerSystem(HandlerMethod method, String operationId) {
        CpfOperationOwnerResolver.CpfOperationOwner owner = null;
        for (CpfOperationOwnerResolver resolver : operationOwnerResolvers) {
            CpfOperationOwnerResolver.CpfOperationOwner candidate = resolver.resolve(method, operationId);
            if (candidate == null) continue;
            if (owner != null && !owner.systemCode().equalsIgnoreCase(candidate.systemCode())) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.TARGET_OPERATION_ID,
                        "Multiple operation-owner descriptors disagree on the canonical SystemCode.",
                        503, "OPERATION_OWNER_AMBIGUOUS");
            }
            owner = candidate;
        }
        if (owner != null) {
            String runtimeSystem = runtime.systemCode();
            if (runtimeSystem != null && !runtimeSystem.isBlank()
                    && !runtimeSystem.equalsIgnoreCase(owner.systemCode())) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.TARGET_SYSTEM_CODE,
                        "Runtime SystemCode does not match the explicit Business Operation owner.",
                        503, "OPERATION_OWNER_RUNTIME_MISMATCH");
            }
            return owner.systemCode();
        }
        return runtime.systemCode();
    }

    private static boolean isBusinessTransaction(HandlerMethod method) {
        return AnnotatedElementUtils.findMergedAnnotation(method.getMethod(), CpfOnlineTransaction.class) != null
                || AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), CpfOnlineTransaction.class) != null;
    }

    private void assertSystem(String headerValue, String expected, String header, String category) {
        if (!expected.equalsIgnoreCase(headerValue)) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    header, "Target/current System header does not match this runtime.", 409, category);
        }
    }

    private static void rejectUnexpectedSystemMetadata(CpfHttpHeaders headers) {
        for (String header : List.of(CpfHttpHeaderNames.ORIGINAL_SYSTEM_CODE, CpfHttpHeaderNames.SYSTEM_CODE,
                CpfHttpHeaderNames.CALLER_SYSTEM_CODE, CpfHttpHeaderNames.TARGET_SYSTEM_CODE)) {
            String value = headers.get(header);
            if (value != null && !value.isBlank()) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        header, "SystemCode metadata is not allowed for a systemless Operation owner.",
                        409, "SYSTEMLESS_OPERATION_SYSTEM_METADATA");
            }
        }
    }

    private static String stringAttribute(HttpServletRequest request, String name) {
        Object value = request.getAttribute(name);
        return value instanceof String text && !text.isBlank() ? text.trim() : null;
    }
}
