package com.cpf.starter.data.transaction.jta;

import com.cpf.core.api.transaction.CpfXaRecoveryResourceProvider;
import com.cpf.core.api.transaction.CpfXaResourceHandle;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/** XADataSource를 Narayana recovery scan에서 재사용 가능한 CPF provider로 연결합니다. */
public final class CpfXaDataSourceRecoveryProvider implements CpfXaRecoveryResourceProvider, AutoCloseable {
    private final String resourceId;
    private final XADataSource dataSource;
    private final AtomicReference<XAConnection> current = new AtomicReference<>();

    public CpfXaDataSourceRecoveryProvider(String resourceId, XADataSource dataSource) {
        if (resourceId == null || resourceId.isBlank()) throw new IllegalArgumentException("resourceId required");
        this.resourceId = resourceId.trim();
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
    }

    @Override public String resourceId() { return resourceId; }

    @Override public synchronized List<CpfXaResourceHandle> recoveryResources() {
        closeCurrent();
        try {
            XAConnection connection = dataSource.getXAConnection();
            current.set(connection);
            return List.of(new CpfXaResourceHandle(resourceId, connection.getXAResource()));
        } catch (SQLException ex) {
            throw new IllegalStateException("XA recovery connection 생성 실패: " + resourceId, ex);
        }
    }

    @Override public synchronized void close() { closeCurrent(); }

    private void closeCurrent() {
        XAConnection connection = current.getAndSet(null);
        if (connection != null) {
            try { connection.close(); }
            catch (SQLException ex) { throw new IllegalStateException("XA recovery connection 종료 실패: " + resourceId, ex); }
        }
    }
}
