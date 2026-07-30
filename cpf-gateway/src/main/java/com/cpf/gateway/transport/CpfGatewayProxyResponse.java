package com.cpf.gateway.transport;

import com.cpf.core.api.servicecall.CpfServiceCallResponseMetadata;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/** Downstream 상태·header·one-shot body stream과 실제 전달 완료 시점을 보존하는 Gateway 응답입니다. */
public final class CpfGatewayProxyResponse implements CpfServiceCallResponseMetadata, AutoCloseable {
    private final int status;
    private final HttpHeaders headers;
    private final InputStream body;
    private final AtomicBoolean closed;
    private final TransferObserver observer;
    private final AtomicBoolean terminal;

    public CpfGatewayProxyResponse(int status, HttpHeaders headers, InputStream body) {
        this(status, headers, body, new AtomicBoolean(), TransferObserver.noop(), new AtomicBoolean());
    }

    private CpfGatewayProxyResponse(
            int status,
            HttpHeaders headers,
            InputStream body,
            AtomicBoolean closed,
            TransferObserver observer,
            AtomicBoolean terminal) {
        if (status < 100 || status > 599) throw new IllegalArgumentException("HTTP status 범위 오류: " + status);
        this.status = status;
        HttpHeaders copy = new HttpHeaders();
        if (headers != null) copy.putAll(headers);
        this.headers = HttpHeaders.readOnlyHttpHeaders(copy);
        this.body = body == null ? InputStream.nullInputStream() : body;
        this.closed = closed;
        this.observer = Objects.requireNonNull(observer, "observer");
        this.terminal = terminal;
    }

    @Override
    public Integer httpStatus() { return status; }
    public int status() { return status; }
    public HttpHeaders headers() { return headers; }

    /** 응답 body 소유권을 새 header view로 이전합니다. 기존 객체는 더 이상 사용할 수 없습니다. */
    public CpfGatewayProxyResponse replaceHeaders(HttpHeaders replacement) {
        takeOwnership();
        return new CpfGatewayProxyResponse(
                status, replacement, body, new AtomicBoolean(), observer, terminal);
    }

    /** 응답 Stream 소유권을 Capture/Metric Wrapper로 이전합니다. */
    public CpfGatewayProxyResponse mapBody(Function<InputStream, InputStream> mapper) {
        takeOwnership();
        InputStream mapped = mapper.apply(body);
        if (mapped == null) throw new IllegalArgumentException("응답 body mapper는 null을 반환할 수 없습니다.");
        return new CpfGatewayProxyResponse(status, headers, mapped, new AtomicBoolean(), observer, terminal);
    }

    /** 실제 Client 전달 성공/실패/중단 시점에 Ledger를 확정하는 Observer를 연결합니다. */
    public CpfGatewayProxyResponse observe(TransferObserver replacement) {
        takeOwnership();
        return new CpfGatewayProxyResponse(
                status, headers, body, new AtomicBoolean(), Objects.requireNonNull(replacement, "replacement"),
                new AtomicBoolean());
    }

    public long transferTo(OutputStream output, int bufferSize) {
        ensureOpen();
        byte[] buffer = new byte[Math.max(4_096, bufferSize)];
        long total = 0L;
        try {
            int read;
            while ((read = body.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                total += read;
            }
            complete(total);
            return total;
        } catch (IOException ex) {
            UncheckedIOException failure = new UncheckedIOException("Gateway 응답 stream 전달에 실패했습니다.", ex);
            fail(failure, total);
            throw failure;
        } catch (RuntimeException failure) {
            fail(failure, total);
            throw failure;
        }
    }

    public byte[] readAllBytes() {
        ensureOpen();
        try {
            byte[] bytes = body.readAllBytes();
            complete(bytes.length);
            return bytes;
        } catch (IOException ex) {
            UncheckedIOException failure = new UncheckedIOException("Gateway 응답을 읽지 못했습니다.", ex);
            fail(failure, 0L);
            throw failure;
        } catch (RuntimeException failure) {
            fail(failure, 0L);
            throw failure;
        }
    }

    private void complete(long bytes) {
        if (terminal.compareAndSet(false, true)) observer.completed(Math.max(0L, bytes));
    }

    private void fail(RuntimeException failure, long transferredBytes) {
        if (terminal.compareAndSet(false, true)) {
            observer.failed(failure, Math.max(0L, transferredBytes));
        }
    }

    private void takeOwnership() {
        if (!closed.compareAndSet(false, true)) {
            throw new IllegalStateException("이미 소유권이 이전되거나 닫힌 Gateway 응답입니다.");
        }
    }

    private void ensureOpen() {
        if (closed.get()) throw new IllegalStateException("이미 닫힌 Gateway 응답입니다.");
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        RuntimeException closeFailure = null;
        try {
            body.close();
        } catch (IOException ex) {
            closeFailure = new UncheckedIOException("Gateway 응답 stream 종료에 실패했습니다.", ex);
        }
        if (terminal.compareAndSet(false, true)) {
            if (closeFailure == null) observer.abandoned(0L); else observer.failed(closeFailure, 0L);
        }
        if (closeFailure != null) throw closeFailure;
    }

    public interface TransferObserver {
        void completed(long transferredBytes);
        void failed(RuntimeException failure, long transferredBytes);
        void abandoned(long transferredBytes);

        static TransferObserver noop() {
            return new TransferObserver() {
                @Override public void completed(long transferredBytes) { }
                @Override public void failed(RuntimeException failure, long transferredBytes) { }
                @Override public void abandoned(long transferredBytes) { }
            };
        }
    }
}
