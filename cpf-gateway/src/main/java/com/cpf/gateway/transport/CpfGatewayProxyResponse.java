package com.cpf.gateway.transport;

import com.cpf.core.api.servicecall.CpfServiceCallResponseMetadata;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Downstream 상태·header·one-shot body stream을 보존하는 Gateway 응답입니다. */
public final class CpfGatewayProxyResponse implements CpfServiceCallResponseMetadata, AutoCloseable {
    private final int status;
    private final HttpHeaders headers;
    private final InputStream body;
    private final AtomicBoolean closed = new AtomicBoolean();

    public CpfGatewayProxyResponse(int status, HttpHeaders headers, InputStream body) {
        if (status < 100 || status > 599) throw new IllegalArgumentException("HTTP status 범위 오류: " + status);
        this.status = status;
        HttpHeaders copy = new HttpHeaders();
        if (headers != null) copy.putAll(headers);
        this.headers = HttpHeaders.readOnlyHttpHeaders(copy);
        this.body = body == null ? InputStream.nullInputStream() : body;
    }

    @Override
    public Integer httpStatus() {
        return status;
    }

    public int status() {
        return status;
    }

    public HttpHeaders headers() {
        return headers;
    }

    /** 응답 body 소유권을 새 header view로 이전합니다. 기존 객체는 더 이상 사용할 수 없습니다. */
    public CpfGatewayProxyResponse replaceHeaders(HttpHeaders replacement) {
        if (!closed.compareAndSet(false, true)) {
            throw new IllegalStateException("이미 소유권이 이전되거나 닫힌 Gateway 응답입니다.");
        }
        return new CpfGatewayProxyResponse(status, replacement, body);
    }

    public long transferTo(OutputStream output, int bufferSize) {
        if (closed.get()) throw new IllegalStateException("이미 닫힌 Gateway 응답입니다.");
        byte[] buffer = new byte[Math.max(4_096, bufferSize)];
        long total = 0L;
        try {
            int read;
            while ((read = body.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                total += read;
            }
            return total;
        } catch (IOException ex) {
            throw new UncheckedIOException("Gateway 응답 stream 전달에 실패했습니다.", ex);
        }
    }

    public byte[] readAllBytes() {
        if (closed.get()) throw new IllegalStateException("이미 닫힌 Gateway 응답입니다.");
        try {
            return body.readAllBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("Gateway 응답을 읽지 못했습니다.", ex);
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        try { body.close(); } catch (IOException ignored) { }
    }
}
