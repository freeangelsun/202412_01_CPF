package com.cpf.core.common.filter;

import com.cpf.core.common.header.CpfHeaderExtractor;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.logging.TransactionHeader;
import com.cpf.core.common.logging.TransactionIdGenerator;
import com.cpf.core.common.transaction.CpfInboundTransactionIdPolicy;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.common.workflow.CpfWorkflowContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TransactionContextFilter extends OncePerRequestFilter {

    private final TransactionIdGenerator transactionIdGenerator;
    private final CpfInboundTransactionIdPolicy transactionIdPolicy;

    public TransactionContextFilter(TransactionIdGenerator transactionIdGenerator,
            CpfInboundTransactionIdPolicy transactionIdPolicy) {
        this.transactionIdGenerator = transactionIdGenerator;
        this.transactionIdPolicy = transactionIdPolicy;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String incomingSpanId = firstText(
                request.getHeader(TransactionContext.HEADER_PARENT_SPAN_ID),
                request.getHeader(TransactionContext.HEADER_SPAN_ID));
        final String transactionId;
        try {
            transactionId = transactionIdPolicy.resolve(request, transactionIdGenerator).transactionId();
        } catch (CpfValidationException rejected) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid CPF transaction context");
            return;
        }

        TransactionContext.initialize(
                transactionId,
                request.getHeader(TransactionContext.HEADER_TRACE_ID),
                incomingSpanId,
                transactionId,
                buildTransactionHeader(request));
        CpfWorkflowContext.initializeFromHeaders(request);

        applyTraceHeaders(response);
        applySecurityHeaders(request, response);

        try {
            filterChain.doFilter(request, response);
        } finally {
            CpfWorkflowContext.clear();
            TransactionContext.clear();
        }
    }

    private void applyTraceHeaders(HttpServletResponse response) {
        response.setHeader(TransactionContext.HEADER_TRANSACTION_ID, TransactionContext.getOrCreateTransactionId());
        response.setHeader(TransactionContext.HEADER_TRACE_ID, TransactionContext.getOrCreateTraceId());
        response.setHeader(TransactionContext.HEADER_SPAN_ID, TransactionContext.getOrCreateSpanId());
        if (TransactionContext.correlationId() != null) {
            response.setHeader(TransactionContext.HEADER_CORRELATION_ID, TransactionContext.correlationId());
        }
    }

    private void applySecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        if (request.getRequestURI() != null && request.getRequestURI().startsWith("/adm")) {
            response.setHeader(
                    "Content-Security-Policy",
                    "default-src 'self'; script-src 'self'; "
                            + "style-src 'self'; connect-src 'self'; img-src 'self' data:; frame-ancestors 'none'");
        } else {
            response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        }

        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private TransactionHeader buildTransactionHeader(HttpServletRequest request) {
        return CpfHeaderExtractor.toTransactionHeader(request, transactionIdGenerator.getWasId());
    }
}

