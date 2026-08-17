package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
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
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

/** Creates one root CPF context after fail-closed header/trust validation and before Controller execution. */
public final class CpfWebContextFilter extends OncePerRequestFilter {
    public static final String RECEIVED_HEADERS_ATTRIBUTE = "cpf.web.received-headers";
    public static final String INGRESS_TRUST_ATTRIBUTE = "cpf.web.ingress-trust";
    private final CpfHttpInboundContextAdapter inbound;
    private final CpfBusinessDateProvider businessDates;
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfHttpIngressTrustResolver trustResolver;
    private final CpfTrustedProxyClientIpResolver clientIpResolver;
    private final CpfHeaderPolicyRegistry headerPolicies;
    private final CpfHeaderFailureRecorder failures;
    private final CpfRuntimeIdentity runtime;

    public CpfWebContextFilter(CpfHttpInboundContextAdapter inbound, CpfBusinessDateProvider businessDates,
            CpfTransactionIdGenerator transactionIds, CpfHttpIngressTrustResolver trustResolver,
            CpfTrustedProxyClientIpResolver clientIpResolver, CpfHeaderPolicyRegistry headerPolicies,
            CpfHeaderFailureRecorder failures, CpfRuntimeIdentity runtime) {
        this.inbound = inbound;
        this.businessDates = businessDates;
        this.transactionIds = transactionIds;
        this.trustResolver = trustResolver;
        this.clientIpResolver = clientIpResolver;
        this.headerPolicies = headerPolicies;
        this.failures = failures;
        this.runtime = runtime;
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
            String clientIp = clientIpResolver.resolve(request);
            Map<String,String> firstValues = received.asMap();
            CpfHttpIngressMetadata edge = new CpfHttpIngressMetadata(
                    null,
                    decision.verifiedCallerSystemCode(),
                    clientIp,
                    received.get(CpfHttpHeaderNames.COUNTRY_CODE),
                    received.get(CpfHttpHeaderNames.API_VERSION),
                    runtime.systemCode());
            var resolved = inbound.resolve(firstValues, decision.trust(), null, null, edge,
                    request.getMethod() + " " + request.getRequestURI(), businessDates.currentBusinessDate(), null, runtime);
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
