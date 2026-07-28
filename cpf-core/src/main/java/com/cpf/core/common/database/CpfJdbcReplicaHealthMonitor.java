package com.cpf.core.common.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 공식 3 Vendor의 Replica apply lag를 JDBC로 조회합니다. 조회 실패는 fail-safe unavailable입니다. */
public final class CpfJdbcReplicaHealthMonitor implements CpfReplicaHealthMonitor {
    private static final Pattern ORACLE_INTERVAL = Pattern.compile("[+-]?(\\d+)\\s+(\\d+):(\\d+):(\\d+)(?:\\.(\\d+))?");
    private final DataSource replicaDataSource;

    public CpfJdbcReplicaHealthMonitor(DataSource replicaDataSource) {
        this.replicaDataSource = replicaDataSource;
    }

    @Override
    public Status current() {
        try (Connection connection = replicaDataSource.getConnection()) {
            if (!connection.isValid(2)) return Status.unavailable("REPLICA_CONNECTION_INVALID");
            String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (product.contains("postgresql")) return Status.healthy(postgresqlLag(connection));
            if (product.contains("mariadb")) return Status.healthy(mariaDbLag(connection));
            if (product.contains("oracle")) return Status.healthy(oracleLag(connection));
            return Status.unavailable("UNSUPPORTED_REPLICA_VENDOR");
        } catch (Exception ex) {
            return Status.unavailable("REPLICA_LAG_QUERY_FAILED");
        }
    }

    private long postgresqlLag(Connection connection) throws Exception {
        String sql = "SELECT CASE WHEN pg_is_in_recovery() THEN "
                + "COALESCE(EXTRACT(EPOCH FROM (clock_timestamp()-pg_last_xact_replay_timestamp()))*1000,0) ELSE 0 END";
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) throw new IllegalStateException("PostgreSQL lag row 없음");
            return Math.max(0L, Math.round(rs.getDouble(1)));
        }
    }

    private long mariaDbLag(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("SHOW REPLICA STATUS")) {
            if (!rs.next()) throw new IllegalStateException("MariaDB replica status 없음");
            Long seconds = nullableLong(rs, "Seconds_Behind_Source");
            if (seconds == null) seconds = nullableLong(rs, "Seconds_Behind_Master");
            if (seconds == null) throw new IllegalStateException("MariaDB replica lag 미확인");
            return Math.multiplyExact(Math.max(0L, seconds), 1000L);
        }
    }

    private Long nullableLong(ResultSet rs, String column) {
        try {
            long value = rs.getLong(column);
            return rs.wasNull() ? null : value;
        } catch (Exception ignored) {
            return null;
        }
    }

    private long oracleLag(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT VALUE FROM V$DATAGUARD_STATS WHERE NAME='apply lag'")) {
            if (!rs.next()) throw new IllegalStateException("Oracle apply lag 없음");
            String value = rs.getString(1);
            Matcher matcher = ORACLE_INTERVAL.matcher(value == null ? "" : value.trim());
            if (!matcher.matches()) throw new IllegalStateException("Oracle apply lag 형식 오류");
            long days = Long.parseLong(matcher.group(1));
            long hours = Long.parseLong(matcher.group(2));
            long minutes = Long.parseLong(matcher.group(3));
            long seconds = Long.parseLong(matcher.group(4));
            long millis = matcher.group(5) == null ? 0L
                    : Long.parseLong((matcher.group(5) + "000").substring(0, 3));
            return Duration.ofDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds).toMillis() + millis;
        }
    }
}
