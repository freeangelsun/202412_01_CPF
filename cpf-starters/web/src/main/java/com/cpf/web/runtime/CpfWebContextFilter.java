package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfException;
import com.cpf.core.api.error.CpfFrameworkErrorCode;
import com.cpf.core.api.tracking.CpfSubjectCandidate;
import com.cpf.core.api.tracking.CpfSubjectRole;
import com.cpf.core.api.tracking.CpfSubjectSourceType;
import com.cpf.core.api.tracking.CpfSubjectTrustLevel;
import com.cpf.core.api.tracking.CpfSubjectType;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.tracking.CpfSubjectCollector;
import com.cpf.web.api.CpfHttpHeaders;
import com.cpf.web.context.CpfHeaderFailureRecorder;
import com.cpf.web.context.CpfHeaderPolicyRegistry;
import com.cpf.web.context.CpfHeaderValidationException;
import com.cpf.web.context.CpfHttpHeaderNames;
import com.cpf.web.context.CpfHttpInboundContextAdapter;
import com.cpf.web.context.CpfHttpIngressMetadata;
import com.cpf.web.context.CpfHttpHeadersContext;
import com.cpf.web.context.CpfHttpIngressTrustResolver;
import com.cpf.web.context.CpfRuntimeIdentity;
import com.cpf.web.context.CpfTrustedProxyClientIpResolver;
import com.cpf.web.context.CpfWebContexts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import org.springframework.web.filter.OncePerRequestFilter;

/** Creates one root CPF context after fail-closed header/trust validation and before Controller execution. */
public final class CpfWebContextFilter extends OncePerRequestFilter {
    public static final String RECEIVED_HEADERS_ATTRIBUTE = "cpf.web.received-headers";
    public static final String INGRESS_TRUST_ATTRIBUTE = "cpf.web.ingress-trust";
    /** Trusted caller System identity resolved by mTLS/security/peer registry; never sourced from Channel headers. */
    public static final String VERIFIED_CALLER_SYSTEM_ATTRIBUTE = "cpf.web.verified-caller-system";
    private final CpfHttpInboundContextAdapter inbound;
    private final CpfBusinessDateProvider businessDates;
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfHttpIngressTrustResolver trustResolver;
    private final CpfTrustedProxyClientIpResolver clientIpResolver;
    private final CpfHeaderPolicyRegistry headerPolicies;
    private final CpfHeaderFailureRecorder failures;
    private final CpfRuntimeIdentity runtime;
    private final CpfSubjectCollector subjectCollector;
    private final String managementBasePath;
    private final List<String> managementRootPaths;

    public CpfWebContextFilter(CpfHttpInboundContextAdapter inbound, CpfBusinessDateProvider businessDates,
            CpfTransactionIdGenerator transactionIds, CpfHttpIngressTrustResolver trustResolver,
            CpfTrustedProxyClientIpResolver clientIpResolver, CpfHeaderPolicyRegistry headerPolicies,
            CpfHeaderFailureRecorder failures, CpfRuntimeIdentity runtime) {
        this(inbound, businessDates, transactionIds, trustResolver, clientIpResolver, headerPolicies, failures,
                runtime, null, "/actuator");
    }

    public CpfWebContextFilter(CpfHttpInboundContextAdapter inbound, CpfBusinessDateProvider businessDates,
            CpfTransactionIdGenerator transactionIds, CpfHttpIngressTrustResolver trustResolver,
            CpfTrustedProxyClientIpResolver clientIpResolver, CpfHeaderPolicyRegistry headerPolicies,
            CpfHeaderFailureRecorder failures, CpfRuntimeIdentity runtime, CpfSubjectCollector subjectCollector) {
        this(inbound, businessDates, transactionIds, trustResolver, clientIpResolver, headerPolicies, failures,
                runtime, subjectCollector, "/actuator");
    }

    public CpfWebContextFilter(CpfHttpInboundContextAdapter inbound, CpfBusinessDateProvider businessDates,
            CpfTransactionIdGenerator transactionIds, CpfHttpIngressTrustResolver trustResolver,
            CpfTrustedProxyClientIpResolver clientIpResolver, CpfHeaderPolicyRegistry headerPolicies,
            CpfHeaderFailureRecorder failures, CpfRuntimeIdentity runtime, CpfSubjectCollector subjectCollector,
            String managementBasePath) {
        this(inbound, businessDates, transactionIds, trustResolver, clientIpResolver, headerPolicies, failures,
                runtime, subjectCollector, managementBasePath, List.of());
    }

    public CpfWebContextFilter(CpfHttpInboundContextAdapter inbound, CpfBusinessDateProvider businessDates,
            CpfTransactionIdGenerator transactionIds, CpfHttpIngressTrustResolver trustResolver,
            CpfTrustedProxyClientIpResolver clientIpResolver, CpfHeaderPolicyRegistry headerPolicies,
            CpfHeaderFailureRecorder failures, CpfRuntimeIdentity runtime, CpfSubjectCollector subjectCollector,
            String managementBasePath, Collection<String> managementRootPaths) {
        this.inbound = inbound;
        this.businessDates = businessDates;
        this.transactionIds = transactionIds;
        this.trustResolver = trustResolver;
        this.clientIpResolver = clientIpResolver;
        this.headerPolicies = headerPolicies;
        this.failures = failures;
        this.runtime = runtime;
        this.subjectCollector = subjectCollector;
        this.managementBasePath = normalizeManagementBasePath(managementBasePath);
        this.managementRootPaths = managementRootPaths == null ? List.of() : managementRootPaths.stream()
                .filter(path -> path != null && !path.isBlank())
                .map(CpfWebContextFilter::normalizeManagementBasePath)
                .distinct()
                .toList();
    }

    /** Management endpoints keep Spring Security/Validation/Trace but never inherit business System6. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        String normalizedRequestPath = requestPath;
        if (!"/".equals(managementBasePath) && matchesBoundary(normalizedRequestPath, managementBasePath)) return true;
        return managementRootPaths.stream().anyMatch(path -> matchesBoundary(normalizedRequestPath, path));
    }

    private static String normalizeManagementBasePath(String value) {
        String path = value == null || value.isBlank() ? "/actuator" : value.strip();
        if (!path.startsWith("/")) path = "/" + path;
        while (path.length() > 1 && path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return path;
    }

    private static boolean matchesBoundary(String requestPath, String boundary) {
        return requestPath.equals(boundary) || (!"/".equals(boundary) && requestPath.startsWith(boundary + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CpfHttpHeaders received = capture(request);
        request.setAttribute(RECEIVED_HEADERS_ATTRIBUTE, received);
        try {
            headerPolicies.validate(received);
            CpfHttpIngressTrustResolver.Decision decision = trustResolver.resolve(request);
            request.setAttribute(INGRESS_TRUST_ATTRIBUTE, decision.trust());
            if (decision.verifiedCallerSystemCode() != null) {
                request.setAttribute(VERIFIED_CALLER_SYSTEM_ATTRIBUTE, decision.verifiedCallerSystemCode());
            }
            String clientIp = clientIpResolver.resolve(request);
            Map<String,String> firstValues = received.asMap();
            CpfHttpIngressMetadata edge = new CpfHttpIngressMetadata(
                    null,
                    decision.verifiedCallerSystemCode(),
                    clientIp,
                    received.get(CpfHttpHeaderNames.COUNTRY_CODE),
                    received.get(CpfHttpHeaderNames.API_VERSION),
                    runtime.currentChannel());
            var resolved = inbound.resolve(firstValues, decision.trust(), null, null, edge,
                    request.getMethod() + " " + request.getRequestURI(), businessDates.currentBusinessDate(), null, runtime);
            collectSubjects(received, resolved.snapshot().context());
            try (AutoCloseable ignored = CpfContexts.bind(resolved.snapshot());
                 AutoCloseable ignoredWeb = CpfWebContexts.bind(resolved.interaction());
                 AutoCloseable ignoredHeaders = CpfHttpHeadersContext.bind(received)) {
                chain.doFilter(request, response);
            } catch (ServletException | IOException | RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ServletException("CPF context close failed", e);
            }
        } catch (CpfHeaderValidationException ex) {
            String transactionId = failureTransactionId(received.get(CpfHttpHeaderNames.TRANSACTION_ID));
            recordFailure(ex, transactionId, request);
            writeError(response, ex, transactionId);
        }
    }


    private void collectSubjects(CpfHttpHeaders headers, com.cpf.core.api.context.CpfContext context) {
        if (subjectCollector == null || context == null) return;
        String typeText = text(headers.get(CpfHttpHeaderNames.SUBJECT_TYPE));
        String subjectId = text(headers.get(CpfHttpHeaderNames.SUBJECT_ID));
        if (typeText == null && subjectId == null) {
            // Mandatory pipeline, optional value: providers may contribute trusted identity even with no Subject header.
            subjectCollector.collect(context);
            return;
        }
        if (typeText == null || subjectId == null) {
            String missing = typeText == null ? CpfHttpHeaderNames.SUBJECT_TYPE : CpfHttpHeaderNames.SUBJECT_ID;
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER, missing,
                    "Optional Subject metadata requires both X-Subject-Type and X-Subject-Id.", 400, "SUBJECT_METADATA_MALFORMED");
        }
        CpfSubjectType type;
        try {
            type = CpfSubjectType.valueOf(typeText.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                    CpfHttpHeaderNames.SUBJECT_TYPE, "Unsupported Subject Type.", 400, "SUBJECT_TYPE_INVALID");
        }
        try {
            subjectCollector.collect(context, List.of(new CpfSubjectCandidate(type, CpfSubjectRole.ACTOR, subjectId,
                    CpfSubjectSourceType.OPTIONAL_SUBJECT_HEADER, CpfSubjectTrustLevel.CLAIMED)));
        } catch (CpfException ex) {
            if (ex.fallbackError() == CpfErrorCode.CONFLICT) {
                throw new CpfHeaderValidationException(CpfFrameworkErrorCode.INVALID_TRANSACTION_METADATA,
                        CpfHttpHeaderNames.SUBJECT_ID, "Subject identity conflicts with the existing transaction identity.",
                        409, "SUBJECT_IDENTITY_CONFLICT");
            }
            throw ex;
        }
    }

    private static String text(String value) {
        if (value == null) return null;
        String normalized = value.strip();
        return normalized.isEmpty() ? null : normalized;
    }

    private CpfHttpHeaders capture(HttpServletRequest request) {
        LinkedHashMap<String,List<String>> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) return CpfHttpHeaders.empty();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            ArrayList<String> values = new ArrayList<>();
            Enumeration<String> each = request.getHeaders(name);
            while (each != null && each.hasMoreElements()) values.add(each.nextElement());
            headers.put(name, values);
        }
        return CpfHttpHeaders.capture(headers);
    }

    private String failureTransactionId(String inboundTransactionId) {
        if (inboundTransactionId != null && CpfTransactionIds.isCanonical(inboundTransactionId.trim())) {
            return inboundTransactionId.trim();
        }
        String generated = transactionIds.newTransactionId();
        return CpfTransactionIds.isCanonical(generated) ? generated : null;
    }

    private void recordFailure(CpfHeaderValidationException ex, String transactionId, HttpServletRequest request) {
        try {
            failures.record(new CpfHeaderFailureRecorder.Failure(
                    transactionId,
                    request.getHeader(CpfHttpHeaderNames.TRACEPARENT),
                    runtime.systemCode(), runtime.application(), runtime.instance(),
                    ex.headerName(), ex.category(), ex.errorCode().statusCode(), ex.httpStatus(),
                    request.getMethod(), request.getRequestURI(), clientIpResolver.resolve(request), Instant.now()));
        } catch (RuntimeException recorderFailure) {
            // Header rejection must never become an availability failure because an evidence sink is degraded.
            logger.warn("CPF header failure recorder degraded", recorderFailure);
        }
    }

    private void writeError(HttpServletResponse response, CpfHeaderValidationException ex, String transactionId) throws IOException {
        response.resetBuffer();
        response.setStatus(ex.httpStatus());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        String body = "{"
                + "\"status\":" + ex.httpStatus() + ","
                + "\"errorCode\":\"" + json(ex.errorCode().statusCode()) + "\","
                + "\"category\":\"" + json(ex.category()) + "\","
                + "\"header\":\"" + json(ex.headerName()) + "\","
                + "\"transactionId\":\"" + json(transactionId) + "\","
                + "\"message\":\"CPF request header contract validation failed.\","
                + "\"timestamp\":\"" + Instant.now() + "\"}"
                ;
        response.getWriter().write(body);
        response.flushBuffer();
    }

    private String json(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "");
    }
}
