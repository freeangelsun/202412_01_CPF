package com.cpf.gateway.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class CpfGatewayProxyResponseTest {
    @Test
    void terminalSuccessOccursAfterActualBodyRead() {
        AtomicBoolean completed = new AtomicBoolean();
        AtomicLong bytes = new AtomicLong();
        CpfGatewayProxyResponse response = new CpfGatewayProxyResponse(
                200, new HttpHeaders(), new ByteArrayInputStream(new byte[]{1, 2, 3}))
                .observe(new Observer(completed, bytes));
        assertFalse(completed.get());
        assertEquals(3, response.readAllBytes().length);
        assertTrue(completed.get());
        assertEquals(3L, bytes.get());
        response.close();
    }

    @Test
    void failedTransferReportsConfirmedBytesFromCompletedChunks() {
        AtomicLong failedBytes = new AtomicLong(-1L);
        byte[] payload = new byte[5_000];
        CpfGatewayProxyResponse response = new CpfGatewayProxyResponse(
                200, new HttpHeaders(), new ByteArrayInputStream(payload))
                .observe(new CpfGatewayProxyResponse.TransferObserver() {
                    @Override public void completed(long transferredBytes) {
                        throw new AssertionError("must not complete");
                    }
                    @Override public void failed(RuntimeException failure, long transferredBytes) {
                        failedBytes.set(transferredBytes);
                    }
                    @Override public void abandoned(long transferredBytes) {
                        throw new AssertionError("must not abandon");
                    }
                });
        OutputStream output = new OutputStream() {
            private int writes;
            @Override public void write(int value) throws IOException {
                throw new UnsupportedOperationException();
            }
            @Override public void write(byte[] bytes, int offset, int length) throws IOException {
                if (++writes >= 2) throw new IOException("client disconnected");
            }
        };
        org.junit.jupiter.api.Assertions.assertThrows(UncheckedIOException.class,
                () -> response.transferTo(output, 4096));
        assertEquals(4_096L, failedBytes.get());
        response.close();
    }

    @Test
    void abandonedTransferReportsZeroWhenNoBodyWasConsumed() {
        AtomicLong abandonedBytes = new AtomicLong(-1L);
        CpfGatewayProxyResponse response = new CpfGatewayProxyResponse(
                200, new HttpHeaders(), InputStream.nullInputStream())
                .observe(new CpfGatewayProxyResponse.TransferObserver() {
                    @Override public void completed(long transferredBytes) { }
                    @Override public void failed(RuntimeException failure, long transferredBytes) {
                        throw failure;
                    }
                    @Override public void abandoned(long transferredBytes) {
                        abandonedBytes.set(transferredBytes);
                    }
                });
        response.close();
        assertEquals(0L, abandonedBytes.get());
    }

    private static final class Observer implements CpfGatewayProxyResponse.TransferObserver {
        private final AtomicBoolean completed;
        private final AtomicLong bytes;
        private Observer(AtomicBoolean completed, AtomicLong bytes) {
            this.completed = completed;
            this.bytes = bytes;
        }
        @Override public void completed(long transferredBytes) {
            completed.set(true);
            bytes.set(transferredBytes);
        }
        @Override public void failed(RuntimeException failure, long transferredBytes) { throw failure; }
        @Override public void abandoned(long transferredBytes) { throw new AssertionError("must not abandon"); }
    }
}
