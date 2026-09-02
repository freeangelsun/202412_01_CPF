package com.cpf.batch.scheduler.internal;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JdbcSchedulerLeaderRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcSchedulerLeaderRepository(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public Optional<Lease> acquire(String key, String instanceId, Duration duration) {
        long leaseDurationMicros = leaseDurationMicros(duration);
        int changed = jdbc.update(sql.required("scheduler-leader-acquire-update"),
                instanceId, leaseDurationMicros, key, instanceId);
        if (changed == 0) {
            try {
                jdbc.update(sql.required("scheduler-leader-insert"),
                        key, instanceId, leaseDurationMicros);
            } catch (DuplicateKeyException conflict) {
                return Optional.empty();
            }
        }
        return jdbc.query(sql.required("scheduler-leader-find"),
                resultSet -> resultSet.next() && instanceId.equals(resultSet.getString(1))
                ? Optional.of(new Lease(instanceId, resultSet.getLong(2), resultSet.getTimestamp(3).toInstant()))
                : Optional.empty(), key);
    }

    public boolean heartbeat(String key, Lease lease, Duration duration) {
        return jdbc.update(sql.required("scheduler-leader-heartbeat"),
                leaseDurationMicros(duration), key, lease.instanceId(),
                lease.fencingToken()) == 1;
    }

    public boolean isCurrent(String key, Lease lease) {
        Integer count = jdbc.queryForObject(sql.required("scheduler-leader-is-current"),
                Integer.class, key, lease.instanceId(), lease.fencingToken());
        return count != null && count == 1;
    }

    /** Duration-only boundary: each Vendor Pack calculates lease timestamps from its UTC DB clock. */
    private static long leaseDurationMicros(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Scheduler lease duration must be positive");
        }
        try {
            long micros = Math.addExact(
                    Math.multiplyExact(duration.getSeconds(), 1_000_000L), duration.getNano() / 1_000);
            if (micros <= 0) {
                throw new IllegalArgumentException("Scheduler lease duration must be at least one microsecond");
            }
            return micros;
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException("Scheduler lease duration is too large", overflow);
        }
    }

    public record Lease(String instanceId, long fencingToken, Instant leaseUntil) {}
}
