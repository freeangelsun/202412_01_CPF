package com.cpf.starter.data.transaction.jta;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jta.TransactionManager;
import com.cpf.core.api.transaction.CpfXaRecoveryResourceProvider;
import jakarta.transaction.Transaction;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * Disposable DB3 환경에서만 실행하는 prepare -> JVM halt -> restart/recovery 검증 진입점입니다.
 * 필요한 설정은 system property로 주입하며 secret을 출력하지 않습니다.
 */
public final class CpfXaCrashRecoveryHarness {
    private CpfXaCrashRecoveryHarness() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !(args[0].equals("prepare-kill") || args[0].equals("recover"))) {
            throw new IllegalArgumentException("mode must be prepare-kill or recover");
        }
        String txId = required("cpf.xa.harness.transaction-id");
        String sql = required("cpf.xa.harness.insert-sql");
        String countSql = required("cpf.xa.harness.count-sql");
        Path marker = Path.of(System.getProperty("cpf.xa.harness.marker", "build/xa-harness/PREPARED"));
        java.nio.file.Files.createDirectories(marker.getParent());

        CpfXaDataSourceFactory factory = new CpfXaDataSourceFactory();
        XADataSource ds1 = factory.create(required("cpf.xa.harness.vendor1"), required("cpf.xa.harness.url1"),
                required("cpf.xa.harness.user1"), required("cpf.xa.harness.password1").toCharArray());
        XADataSource ds2 = factory.create(required("cpf.xa.harness.vendor2"), required("cpf.xa.harness.url2"),
                required("cpf.xa.harness.user2"), required("cpf.xa.harness.password2").toCharArray());

        try (CpfXaDataSourceRecoveryProvider p1 = new CpfXaDataSourceRecoveryProvider("db1", ds1);
             CpfXaDataSourceRecoveryProvider p2 = new CpfXaDataSourceRecoveryProvider("db2", ds2);
             CpfNarayanaRecoveryRegistrar ignored = registrar(List.of(p1, p2))) {
            if (args[0].equals("prepare-kill")) {
                runPrepareKill(ds1, ds2, txId, sql, marker);
            } else {
                RecoveryManager.manager().scan();
                RecoveryManager.manager().scan();
                long one = count(ds1, countSql, txId);
                long two = count(ds2, countSql, txId);
                if (one > 1 || two > 1) throw new IllegalStateException("duplicate side effect: db1=" + one + ", db2=" + two);
                if (one != two) throw new IllegalStateException("inconsistent XA recovery result: db1=" + one + ", db2=" + two);
                System.out.println("CPF_XA_RECOVERY_RESULT transactionId=" + txId + " db1=" + one + " db2=" + two + " duplicate=0");
            }
        }
    }

    private static CpfNarayanaRecoveryRegistrar registrar(List<CpfXaRecoveryResourceProvider> providers) {
        return new CpfNarayanaRecoveryRegistrar(providers);
    }

    private static void runPrepareKill(XADataSource ds1, XADataSource ds2, String txId, String sql, Path marker) throws Exception {
        var tm = TransactionManager.transactionManager();
        tm.setTransactionTimeout(Math.toIntExact(Duration.ofSeconds(30).toSeconds()));
        tm.begin();
        Transaction tx = tm.getTransaction();
        AtomicInteger prepared = new AtomicInteger();
        XAConnection xa1 = ds1.getXAConnection();
        XAConnection xa2 = ds2.getXAConnection();
        try (Connection c1 = xa1.getConnection(); Connection c2 = xa2.getConnection()) {
            tx.enlistResource(new CpfXaCrashProbeResource(xa1.getXAResource(), marker, 2, prepared));
            tx.enlistResource(new CpfXaCrashProbeResource(xa2.getXAResource(), marker, 2, prepared));
            insert(c1, sql, txId, "DB1");
            insert(c2, sql, txId, "DB2");
            System.setProperty("cpf.xa.harness.crash", "true");
            tx.commit();
            throw new IllegalStateException("Expected JVM halt after second successful XA prepare");
        } finally {
            try { xa1.close(); } finally { xa2.close(); }
        }
    }

    private static void insert(Connection connection, String sql, String txId, String resourceId) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, txId);
            ps.setString(2, resourceId);
            if (ps.executeUpdate() != 1) throw new IllegalStateException("Expected one harness business row");
        }
    }

    private static long count(XADataSource dataSource, String sql, String txId) throws Exception {
        XAConnection xa = dataSource.getXAConnection();
        try (Connection c = xa.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, txId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("count query returned no row");
                return rs.getLong(1);
            }
        } finally {
            xa.close();
        }
    }

    private static String required(String name) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("required system property: " + name);
        return value;
    }
}
