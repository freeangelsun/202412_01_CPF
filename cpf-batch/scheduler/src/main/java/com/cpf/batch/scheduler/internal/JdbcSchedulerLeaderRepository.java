package com.cpf.batch.scheduler.internal;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Repository
public class JdbcSchedulerLeaderRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcSchedulerLeaderRepository(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    @Transactional
    public Optional<Lease> acquire(String key, String instanceId, Duration duration) {
        Instant now = Instant.now();
        Instant until = now.plus(duration);
        int changed = jdbc.update(sql.required("scheduler-leader-acquire-update"),
                instanceId, Timestamp.from(until), Timestamp.from(now), key, instanceId, Timestamp.from(now));
        if (changed == 0) {
            try {
                jdbc.update(sql.required("scheduler-leader-insert"),
                        key, instanceId, Timestamp.from(until), Timestamp.from(now));
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
                Timestamp.from(Instant.now().plus(duration)), key, lease.instanceId(),
                lease.fencingToken()) == 1;
    }

    public boolean isCurrent(String key, Lease lease) {
        Integer count = jdbc.queryForObject(sql.required("scheduler-leader-is-current"),
                Integer.class, key, lease.instanceId(), lease.fencingToken());
        return count != null && count == 1;
    }

    public record Lease(String instanceId, long fencingToken, Instant leaseUntil) {}
}
