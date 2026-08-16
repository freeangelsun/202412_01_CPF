package com.cpf.integration.resilience.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Static contract gate for the three official CPF database vendors. */
public final class CpfStateVendorSqlHarness {
    private static final List<String> VENDORS = List.of("oracle", "postgresql", "mariadb");
    private static final List<String> TABLES = List.of(
            "cpf_state_shard", "cpf_operation_state", "cpf_state_command", "cpf_state_audit");

    private CpfStateVendorSqlHarness() {}

    public static void main(String[] args) throws Exception {
        for (String vendor : VENDORS) {
            String install = resource(vendor, "install.sql");
            String upgrade = resource(vendor, "upgrade.sql");
            String rollback = resource(vendor, "rollback.sql");
            String verify = resource(vendor, "verify.sql");
            require(install.equals(upgrade), vendor + " install and upgrade must be deterministic");
            for (String table : TABLES) {
                require(install.contains(table), vendor + " install missing " + table);
                require(rollback.contains(table), vendor + " rollback missing " + table);
                require(verify.contains(table), vendor + " verify missing " + table);
            }
            require(install.contains("recorded_at"), vendor + " command TTL index is required");
            require(install.contains("state_version"), vendor + " optimistic version is required");
            require(install.contains("command_hash"), vendor + " immutable command hash is required");
            require(install.contains("257"), vendor + " capacity/command shard rows are required");
            require(rollback.indexOf("cpf_state_command") < rollback.indexOf("cpf_operation_state"),
                    vendor + " rollback must drop child command table before state table");
            String normalized = install.toLowerCase(java.util.Locale.ROOT);
            require(!normalized.contains("mysql") && !normalized.contains("mssql")
                            && !normalized.contains(" h2 "),
                    vendor + " script must not claim unsupported vendors");
        }
        System.out.println("CPF_STATE_VENDOR_SQL_HARNESS_PASS");
    }

    private static String resource(String vendor, String file) throws IOException {
        String path = "cpf-state-db/" + vendor + "/" + file;
        try (InputStream input = CpfStateVendorSqlHarness.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (input == null) throw new IOException("missing classpath resource: " + path);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
