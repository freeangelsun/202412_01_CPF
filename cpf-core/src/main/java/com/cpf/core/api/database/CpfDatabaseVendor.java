package com.cpf.core.api.database;

import java.util.Arrays;
import java.util.Locale;

/**
 * CPF가 지원하는 DB Vendor와 물리 resource 선택 계약입니다.
 *
 * <p>업무 Controller/Service/Domain은 이 타입으로 분기하지 않습니다. Datasource,
 * Migration과 SQL Adapter 경계에서만 사용합니다.</p>
 */
public enum CpfDatabaseVendor {
    MARIADB("mariadb", "org.mariadb.jdbc.Driver", 3306, "classpath:db/migration/mariadb"),
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver", 3306, "classpath:db/migration/mysql"),
    POSTGRESQL("postgresql", "org.postgresql.Driver", 5432, "classpath:db/migration/postgresql"),
    ORACLE("oracle", "oracle.jdbc.OracleDriver", 1521, "classpath:db/migration/oracle"),
    SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", 1433,
            "classpath:db/migration/sqlserver");

    private final String id;
    private final String driverClassName;
    private final int defaultPort;
    private final String flywayLocation;

    CpfDatabaseVendor(
            String id,
            String driverClassName,
            int defaultPort,
            String flywayLocation) {
        this.id = id;
        this.driverClassName = driverClassName;
        this.defaultPort = defaultPort;
        this.flywayLocation = flywayLocation;
    }

    public String id() {
        return id;
    }

    public String driverClassName() {
        return driverClassName;
    }

    public int defaultPort() {
        return defaultPort;
    }

    public String flywayLocation() {
        return flywayLocation;
    }

    public String myBatisDatabaseId() {
        return id;
    }

    /**
     * Spring Batch가 선택할 Vendor metadata resource 식별자입니다.
     */
    public String springBatchDatabaseType() {
        return switch (this) {
            case MARIADB -> "MARIADB";
            case MYSQL -> "MYSQL";
            case POSTGRESQL -> "POSTGRES";
            case ORACLE -> "ORACLE";
            case SQLSERVER -> "SQLSERVER";
        };
    }

    /**
     * 명시 URL이 없을 때 표준 host/port/database property로 JDBC URL을 만듭니다.
     */
    public String jdbcUrl(String host, Integer configuredPort, String databaseName) {
        String safeHost = requireText(host, "host");
        String safeDatabaseName = requireText(databaseName, "databaseName");
        int port = configuredPort == null ? defaultPort : configuredPort;
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException("port는 1~65535여야 합니다.");
        }
        return switch (this) {
            case MARIADB -> "jdbc:mariadb://%s:%d/%s".formatted(safeHost, port, safeDatabaseName);
            case MYSQL -> "jdbc:mysql://%s:%d/%s".formatted(safeHost, port, safeDatabaseName);
            case POSTGRESQL -> "jdbc:postgresql://%s:%d/%s".formatted(safeHost, port, safeDatabaseName);
            case ORACLE -> "jdbc:oracle:thin:@//%s:%d/%s".formatted(safeHost, port, safeDatabaseName);
            case SQLSERVER -> "jdbc:sqlserver://%s:%d;databaseName=%s"
                    .formatted(safeHost, port, safeDatabaseName);
        };
    }

    public boolean accepts(String jdbcUrl) {
        if (jdbcUrl == null) {
            return false;
        }
        String normalized = jdbcUrl.trim().toLowerCase(Locale.ROOT);
        return switch (this) {
            case MARIADB -> normalized.startsWith("jdbc:mariadb:");
            case MYSQL -> normalized.startsWith("jdbc:mysql:");
            case POSTGRESQL -> normalized.startsWith("jdbc:postgresql:");
            case ORACLE -> normalized.startsWith("jdbc:oracle:");
            case SQLSERVER -> normalized.startsWith("jdbc:sqlserver:");
        };
    }

    public static CpfDatabaseVendor from(String value) {
        String normalized = requireText(value, "cpf.db.vendor")
                .toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "");
        return Arrays.stream(values())
                .filter(vendor -> vendor.id.replace("-", "").equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 cpf.db.vendor입니다: " + value
                                + " (mariadb|mysql|postgresql|oracle|sqlserver)"));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return value.trim();
    }
}
