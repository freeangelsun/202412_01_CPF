package com.cpf.gateway.scg;

import com.cpf.gateway.api.CpfGatewayAuditEvent;
import com.cpf.gateway.api.CpfGatewayLedgerPort;
import com.cpf.foundation.context.header.CpfHeaderNames;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;
import com.cpf.gateway.logging.CpfGatewayCaptureService;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Sync/Async/Streaming 실제 종료 시 Capture와 Transaction을 단 한 번 종결합니다. */
@Component
public final class CpfGatewayLedgerCompletionFilter extends OncePerRequestFilter {
    private static final Set<String> HOP_BY_HOP = Set.of(
            "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
            "te", "trailer", "transfer-encoding", "upgrade");
    private static final Set<String> NEVER_RELAY = Set.of("set-cookie", "server");
    private static final Set<String> RATE_LIMIT_RESPONSE_HEADERS = Set.of(
            "retry-after", "x-ratelimit-remaining", "x-ratelimit-reset",
            "x-cpf-ratelimit-policy", "x-cpf-ratelimit-scope",
            "x-cpf-ratelimit-degraded", "x-cpf-ratelimit-reason");

    private final CpfGatewayLedgerRecoverySpool recovery;
    private final CpfGatewayAuditRecoverySpool auditRecovery;
    private final CpfGatewayCaptureService captureService;
    private final CpfGatewayRuntimePolicy runtimePolicy;
    private final long responseBodyBytesCap;
    private final String gatewayInstanceId;

    public CpfGatewayLedgerCompletionFilter(
            CpfGatewayLedgerRecoverySpool recovery,
            CpfGatewayAuditRecoverySpool auditRecovery,
            CpfGatewayCaptureService captureService,
            CpfGatewayRuntimePolicy runtimePolicy,
            CpfGatewaySafetyProperties safety) {
        this.recovery = recovery;
        this.auditRecovery = auditRecovery;
        this.captureService = captureService;
        this.runtimePolicy = runtimePolicy;
        this.responseBodyBytesCap = safety.getResponseBodyBytesCap();
        this.gatewayInstanceId = safety.getInstanceId();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        AtomicLong bytes = new AtomicLong();
        AtomicBoolean completed = new AtomicBoolean();
        Throwable[] failure = new Throwable[1];
        CountingResponse counting = new CountingResponse(
                request, response, bytes, responseBodyBytesCap, gatewayInstanceId,
                runtimePolicy, captureService);
        try {
            chain.doFilter(request, counting);
            if (request.isAsyncStarted()) {
                request.getAsyncContext().addListener(new AsyncListener() {
                    @Override public void onComplete(AsyncEvent event) {
                        finish(request, counting, bytes, completed, failure[0], false);
                    }
                    @Override public void onTimeout(AsyncEvent event) {
                        finish(request, counting, bytes, completed, event.getThrowable(), true);
                    }
                    @Override public void onError(AsyncEvent event) {
                        finish(request, counting, bytes, completed, event.getThrowable(), true);
                    }
                    @Override public void onStartAsync(AsyncEvent event) {
                        try {
                            event.getAsyncContext().addListener(this);
                        } catch (IllegalStateException ignored) {
                            // Async lifecycle가 이미 종료된 경우 기존 listener가 종결합니다.
                        }
                    }
                });
            }
        } catch (IOException | ServletException | RuntimeException exception) {
            failure[0] = exception;
            throw exception;
        } finally {
            if (!request.isAsyncStarted()) {
                finish(request, counting, bytes, completed, failure[0], failure[0] != null);
            }
        }
    }

    private void finish(
            HttpServletRequest request,
            CountingResponse response,
            AtomicLong bytes,
            AtomicBoolean completed,
            Throwable failure,
            boolean unknown) {
        if (!completed.compareAndSet(false, true)) return;
        response.prepareHeaders();
        response.finishCapture(failure != null || unknown);

        Object value = request.getAttribute(CpfScgPrimaryHandler.TX_ATTR);
        Object startedValue = request.getAttribute(CpfScgPrimaryHandler.START_ATTR);
        if (!(value instanceof String tx) || !(startedValue instanceof OffsetDateTime started)) return;

        int status = response.getStatus();
        boolean unresolved = unknown || failure != null;
        String result = unresolved
                ? "UNKNOWN_RESULT"
                : status >= 500 ? "FAILED" : status >= 400 ? "REJECTED" : "SUCCESS";
        String stage = unresolved ? "CLIENT_OR_STREAM" : status >= 500 ? "UPSTREAM_RESPONSE" : "";
        recovery.complete(new CpfGatewayLedgerPort.TransactionCompletion(
                tx,
                nullable(request.getAttribute(CpfScgPrimaryHandler.TARGET_ATTR)),
                result,
                Integer.toString(status),
                "",
                stage,
                unresolved,
                Duration.between(started, OffsetDateTime.now()).toMillis(),
                bytes.get(),
                OffsetDateTime.now()));

        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
        attributes.put("routeId", nullable(request.getAttribute(CpfScgPrimaryHandler.ROUTE_ATTR)));
        attributes.put("responseBytes", Long.toString(bytes.get()));
        attributes.put("unknownResult", Boolean.toString(unresolved));
        if (!stage.isBlank()) attributes.put("failureStage", stage);
        auditRecovery.record(new CpfGatewayAuditEvent(
                tx,
                nullable(request.getAttribute(CpfScgPrimaryHandler.EXECUTION_ATTR)),
                nullable(request.getAttribute(CpfScgPrimaryHandler.PRINCIPAL_ATTR)),
                nullable(request.getAttribute(CpfScgPrimaryHandler.REASON_ATTR)),
                "AFTER",
                result,
                nullable(request.getAttribute(CpfScgPrimaryHandler.TARGET_ATTR)),
                status,
                java.time.Instant.now(),
                attributes));
    }

    private static String nullable(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static final class CountingResponse extends HttpServletResponseWrapper {
        private final HttpServletRequest request;
        private final AtomicLong bytes;
        private final long cap;
        private final String gatewayInstanceId;
        private final CpfGatewayRuntimePolicy runtimePolicy;
        private final CpfGatewayCaptureService captureService;
        private final AtomicBoolean prepared = new AtomicBoolean();
        private final AtomicBoolean captureFinished = new AtomicBoolean();
        private final MessageDigest digest;
        private ByteArrayOutputStream preview;
        private int captureLimit = -1;
        private ServletOutputStream output;
        private PrintWriter writer;

        private CountingResponse(
                HttpServletRequest request,
                HttpServletResponse response,
                AtomicLong bytes,
                long cap,
                String gatewayInstanceId,
                CpfGatewayRuntimePolicy runtimePolicy,
                CpfGatewayCaptureService captureService) {
            super(response);
            this.request = request;
            this.bytes = bytes;
            this.cap = cap;
            this.gatewayInstanceId = gatewayInstanceId;
            this.runtimePolicy = runtimePolicy;
            this.captureService = captureService;
            try {
                this.digest = MessageDigest.getInstance("SHA-256");
            } catch (Exception impossible) {
                throw new IllegalStateException("SHA-256 unavailable", impossible);
            }
        }

        @Override
        public void setHeader(String name, String value) {
            if (allowHeader(name)) super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if (allowHeader(name)) super.addHeader(name, value);
        }

        @Override
        public void setDateHeader(String name, long date) {
            if (allowHeader(name)) super.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            if (allowHeader(name)) super.addDateHeader(name, date);
        }

        @Override
        public void setIntHeader(String name, int value) {
            if (allowHeader(name)) super.setIntHeader(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            if (allowHeader(name)) super.addIntHeader(name, value);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null) throw new IllegalStateException("getWriter already called");
            prepareHeaders();
            if (output == null) {
                ServletOutputStream delegate = super.getOutputStream();
                output = new ServletOutputStream() {
                    @Override public void write(int value) throws IOException {
                        byte[] one = {(byte) value};
                        record(one, 0, 1);
                        delegate.write(value);
                    }
                    @Override public void write(byte[] value, int offset, int length) throws IOException {
                        record(value, offset, length);
                        delegate.write(value, offset, length);
                    }
                    @Override public boolean isReady() { return delegate.isReady(); }
                    @Override public void setWriteListener(WriteListener listener) {
                        delegate.setWriteListener(listener);
                    }
                    @Override public void flush() throws IOException { delegate.flush(); }
                    @Override public void close() throws IOException { delegate.close(); }
                };
            }
            return output;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (output != null) throw new IllegalStateException("getOutputStream already called");
            prepareHeaders();
            if (writer == null) {
                PrintWriter delegate = super.getWriter();
                Writer countingWriter = new Writer() {
                    @Override public void write(char[] chars, int offset, int length) throws IOException {
                        Charset charset = getCharacterEncoding() == null
                                ? StandardCharsets.UTF_8
                                : Charset.forName(getCharacterEncoding());
                        byte[] encoded = new String(chars, offset, length).getBytes(charset);
                        record(encoded, 0, encoded.length);
                        delegate.write(chars, offset, length);
                    }
                    @Override public void flush() { delegate.flush(); }
                    @Override public void close() { delegate.close(); }
                };
                writer = new PrintWriter(countingWriter);
            }
            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            prepareHeaders();
            super.flushBuffer();
        }

        private boolean allowHeader(String name) {
            if (name == null || request.getAttribute(CpfScgPrimaryHandler.TX_ATTR) == null) return true;
            String lower = name.toLowerCase(Locale.ROOT);
            if (HOP_BY_HOP.contains(lower) || NEVER_RELAY.contains(lower)) return false;
            Object rateLimit = request.getAttribute(CpfScgPrimaryHandler.RATE_LIMIT_DECISION_ATTR);
            if (rateLimit instanceof com.cpf.gateway.api.CpfGatewayRateLimitPort.Decision decision
                    && !decision.allowed()
                    && RATE_LIMIT_RESPONSE_HEADERS.contains(lower)) {
                return true;
            }
            return runtimePolicy.allowResponseHeader(lower);
        }

        private void prepareHeaders() {
            if (!prepared.compareAndSet(false, true)) return;
            Object tx = request.getAttribute(CpfScgPrimaryHandler.TX_ATTR);
            if (tx == null) return;
            setCanonicalHeader(CpfHeaderNames.GATEWAY_INSTANCE_ID,
                    gatewayInstanceId);
            setCanonicalHeader(CpfHeaderNames.GATEWAY_ROUTE_ID,
                    request.getAttribute(CpfScgPrimaryHandler.ROUTE_ATTR));
            setCanonicalHeader(CpfHeaderNames.GATEWAY_ROUTE_VERSION,
                    request.getAttribute(CpfScgPrimaryHandler.ROUTE_VERSION_ATTR));
            Object cors = request.getAttribute(CpfScgPrimaryHandler.CORS_DECISION_ATTR);
            if (cors instanceof CpfGatewayRuntimePolicy.CorsDecision decision
                    && !decision.allowOrigin().isBlank()) {
                super.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, decision.allowOrigin());
                super.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        Boolean.toString(decision.allowCredentials()));
                super.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE,
                        Long.toString(decision.maxAgeSeconds()));
                if (!decision.exposedHeaders().isEmpty()) {
                    super.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                            String.join(",", decision.exposedHeaders()));
                }
                super.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
            }
        }

        private void setCanonicalHeader(String name, Object value) {
            if (value != null) super.setHeader(name, String.valueOf(value));
        }

        private synchronized void record(byte[] value, int offset, int length) throws IOException {
            if (length <= 0) return;
            long total = bytes.addAndGet(length);
            if (total > cap) {
                bytes.addAndGet(-length);
                throw new IOException("Gateway response body exceeds configured cap");
            }
            digest.update(value, offset, length);
            int limit = captureLimit();
            int remaining = limit - preview.size();
            if (remaining > 0) preview.write(value, offset, Math.min(remaining, length));
        }

        private int captureLimit() {
            if (captureLimit >= 0) return captureLimit;
            Object policy = request.getAttribute(CpfScgPrimaryHandler.LOG_POLICY_ATTR);
            captureLimit = policy instanceof LogPolicyDecision decision
                    ? captureService.responseCaptureLimit(decision)
                    : 0;
            preview = new ByteArrayOutputStream(Math.min(captureLimit, 65_536));
            return captureLimit;
        }

        private void finishCapture(boolean truncated) {
            if (!captureFinished.compareAndSet(false, true)) return;
            Object policyValue = request.getAttribute(CpfScgPrimaryHandler.LOG_POLICY_ATTR);
            Object txValue = request.getAttribute(CpfScgPrimaryHandler.TX_ATTR);
            if (!(policyValue instanceof LogPolicyDecision policy) || !(txValue instanceof String tx)) return;
            try {
                captureService.captureResponseHeaders(tx, responseHeaders(), policy);
                int limit = captureLimit();
                byte[] captured = limit == 0 ? new byte[0] : preview.toByteArray();
                captureService.captureResponseBody(
                        tx,
                        captured,
                        bytes.get(),
                        HexFormat.of().formatHex(digest.digest()),
                        truncated || bytes.get() > captured.length,
                        policy);
            } catch (RuntimeException failure) {
                // Capture ledger는 자체 recovery spool을 사용하지만 정책/보호 Adapter 오류도 원 응답을 오염시키지 않습니다.
            }
        }

        private HttpHeaders responseHeaders() {
            HttpHeaders headers = new HttpHeaders();
            for (String name : getHeaderNames()) {
                headers.put(name, new ArrayList<>(getHeaders(name)));
            }
            return headers;
        }
    }
}
