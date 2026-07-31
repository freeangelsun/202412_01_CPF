package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayLedgerPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 실제 Servlet 응답 쓰기 종료 시점에 Gateway Transaction을 종결합니다. */
@Component
public final class CpfGatewayLedgerCompletionFilter extends OncePerRequestFilter {
    private final CpfGatewayLedgerPort ledger;
    public CpfGatewayLedgerCompletionFilter(CpfGatewayLedgerPort ledger) { this.ledger = ledger; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        AtomicLong bytes = new AtomicLong();
        HttpServletResponseWrapper counting = new HttpServletResponseWrapper(response) {
            @Override public ServletOutputStream getOutputStream() throws IOException {
                ServletOutputStream delegate = super.getOutputStream();
                return new ServletOutputStream() {
                    @Override public void write(int value) throws IOException { delegate.write(value); bytes.incrementAndGet(); }
                    @Override public void write(byte[] value, int offset, int length) throws IOException { delegate.write(value, offset, length); bytes.addAndGet(length); }
                    @Override public boolean isReady() { return delegate.isReady(); }
                    @Override public void setWriteListener(WriteListener listener) { delegate.setWriteListener(listener); }
                };
            }
        };
        Throwable failure = null;
        try { chain.doFilter(request, counting); }
        catch (IOException | ServletException | RuntimeException ex) { failure = ex; throw ex; }
        finally {
            Object value = request.getAttribute(CpfScgPrimaryHandler.TX_ATTR);
            Object startedValue = request.getAttribute(CpfScgPrimaryHandler.START_ATTR);
            if (value instanceof String tx && startedValue instanceof OffsetDateTime started) {
                int status = response.getStatus();
                boolean unknown = failure != null;
                String result = unknown ? "UNKNOWN_RESULT" : status >= 500 ? "FAILED" : status >= 400 ? "REJECTED" : "SUCCESS";
                String stage = unknown ? "CLIENT_OR_STREAM" : status >= 500 ? "UPSTREAM_RESPONSE" : "";
                ledger.complete(new CpfGatewayLedgerPort.TransactionCompletion(tx,
                        String.valueOf(request.getAttribute(CpfScgPrimaryHandler.TARGET_ATTR)), result,
                        Integer.toString(status), "", stage, unknown,
                        Duration.between(started, OffsetDateTime.now()).toMillis(), bytes.get(), OffsetDateTime.now()));
            }
        }
    }
}
