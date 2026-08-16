package com.cpf.file.context;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Restores the captured File Context for every streaming operation and closes without thread-local leakage. */
public final class CpfContextAwareInputStream extends FilterInputStream {
    private final CpfContextSnapshot snapshot;

    public CpfContextAwareInputStream(InputStream input, CpfContextSnapshot snapshot) {
        super(input);
        this.snapshot = snapshot;
    }

    @Override
    public int read() throws IOException {
        return withContext(super::read);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return withContext(() -> super.read(buffer, offset, length));
    }

    @Override
    public long skip(long count) throws IOException {
        return withContext(() -> super.skip(count));
    }

    @Override
    public void close() throws IOException {
        withContext(() -> {
            super.close();
            return null;
        });
    }

    private <T> T withContext(IoSupplier<T> operation) throws IOException {
        try (AutoCloseable ignored = CpfContexts.bind(snapshot)) {
            return operation.get();
        } catch (IOException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new IOException("CPF context scope close failed", failure);
        }
    }

    @FunctionalInterface
    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
