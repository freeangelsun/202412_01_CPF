package fps.pfw.common.filter;

import fps.pfw.common.logging.TransactionContext;
import fps.pfw.common.logging.TransactionHeader;
import fps.pfw.common.logging.TransactionIdGenerator;
import fps.pfw.common.workflow.FpsWorkflowContext;
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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionContextFilter extends OncePerRequestFilter {

    private final TransactionIdGenerator transactionIdGenerator;

    public TransactionContextFilter(TransactionIdGenerator transactionIdGenerator) {
        this.transactionIdGenerator = transactionIdGenerator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String incomingSpanId = firstText(
                request.getHeader(TransactionContext.HEADER_PARENT_SPAN_ID),
                request.getHeader(TransactionContext.HEADER_SPAN_ID));
        String transactionId = transactionIdGenerator.generateOrUse(
                request.getHeader(TransactionContext.HEADER_TRANSACTION_ID));

        TransactionContext.initialize(
                transactionId,
                request.getHeader(TransactionContext.HEADER_TRACE_ID),
                incomingSpanId,
                transactionId,
                buildTransactionHeader(request));
        FpsWorkflowContext.initializeFromHeaders(request);

        applyTraceHeaders(response);
        applySecurityHeaders(request, response);

        try {
            filterChain.doFilter(request, response);
        } finally {
            FpsWorkflowContext.clear();
            TransactionContext.clear();
        }
    }

    private void applyTraceHeaders(HttpServletResponse response) {
        response.setHeader(TransactionContext.HEADER_TRANSACTION_ID, TransactionContext.getOrCreateTransactionId());
        response.setHeader(TransactionContext.HEADER_TRACE_ID, TransactionContext.getOrCreateTraceId());
        response.setHeader(TransactionContext.HEADER_SPAN_ID, TransactionContext.getOrCreateSpanId());
    }

    private void applySecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");

        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
    }

    private String firstText(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }

    private TransactionHeader buildTransactionHeader(HttpServletRequest request) {
        return TransactionHeader.builder()
                .requestType(request.getHeader(TransactionContext.HEADER_REQUEST_TYPE))
                .originalChannelCode(request.getHeader(TransactionContext.HEADER_ORIGINAL_CHANNEL_CODE))
                .channelCode(request.getHeader(TransactionContext.HEADER_CHANNEL_CODE))
                .memberNo(request.getHeader(TransactionContext.HEADER_MEMBER_NO))
                .customerNo(request.getHeader(TransactionContext.HEADER_CUSTOMER_NO))
                .userId(request.getHeader(TransactionContext.HEADER_USER_ID))
                .screenId(request.getHeader(TransactionContext.HEADER_SCREEN_ID))
                .deviceId(request.getHeader(TransactionContext.HEADER_DEVICE_ID))
                .clientRequestTime(request.getHeader(TransactionContext.HEADER_CLIENT_REQUEST_TIME))
                .clientIp(request.getHeader(TransactionContext.HEADER_CLIENT_IP))
                .reservedField1(request.getHeader(TransactionContext.HEADER_RESERVED_FIELD_1))
                .reservedField2(request.getHeader(TransactionContext.HEADER_RESERVED_FIELD_2))
                .reservedField3(request.getHeader(TransactionContext.HEADER_RESERVED_FIELD_3))
                .reservedField4(request.getHeader(TransactionContext.HEADER_RESERVED_FIELD_4))
                .reservedField5(request.getHeader(TransactionContext.HEADER_RESERVED_FIELD_5))
                .wasId(transactionIdGenerator.getWasId())
                .build();
    }
}
