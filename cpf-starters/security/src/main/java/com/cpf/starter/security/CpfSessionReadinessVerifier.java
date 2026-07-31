package com.cpf.starter.security;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;

/** Product profile에서 JDBC Session/Vault Schema와 Secure Cookie·암호키 정책을 fail-closed로 확인합니다. */
final class CpfSessionReadinessVerifier implements SmartInitializingSingleton {
    private static final byte[] DEVELOPMENT_EPHEMERAL_KEY = createDevelopmentKey();
    private static final Set<String> SESSION_COLUMNS = Set.of(
            "PRIMARY_ID", "SESSION_ID", "CREATION_TIME", "LAST_ACCESS_TIME",
            "MAX_INACTIVE_INTERVAL", "EXPIRY_TIME");
    private static final Set<String> ATTRIBUTE_COLUMNS = Set.of(
            "SESSION_PRIMARY_ID", "ATTRIBUTE_NAME", "ATTRIBUTE_BYTES");
    private static final Set<String> VAULT_COLUMNS = Set.of(
            "HANDLE_ID", "KEY_ID", "ACCESS_IV", "ACCESS_CIPHER_TEXT",
            "REFRESH_IV", "REFRESH_CIPHER_TEXT", "ACCESS_EXPIRES_AT",
            "REFRESH_EXPIRES_AT", "CREATED_AT", "UPDATED_AT", "VERSION_NO");

    private final DataSource dataSource;
    private final Environment environment;
    private final CpfServerSessionProperties properties;

    CpfSessionReadinessVerifier(
            DataSource dataSource,
            Environment environment,
            CpfServerSessionProperties properties) {
        this.dataSource = dataSource;
        this.environment = environment;
        this.properties = properties;
    }

    @Override
    public void afterSingletonsInstantiated() {
        boolean product = CpfServerSessionSecurityAutoConfiguration.isProductProfile(environment);
        if (product && !properties.secure()) {
            throw new IllegalStateException("CPF privileged console session requires Secure cookie.");
        }
        if (product && properties.allowedOrigins().isEmpty()) {
            throw new IllegalStateException("CPF product profile requires explicit trusted origins.");
        }
        if (product && properties.allowedOrigins().stream().anyMatch(origin -> !origin.startsWith("https://"))) {
            throw new IllegalStateException("CPF product profile trusted origins must use HTTPS.");
        }
        byte[] key = decodeKey(properties.credentialKeyBase64(), product);
        if (key.length != 32) {
            throw new IllegalStateException("CPF BFF credential key must be 256 bits.");
        }

        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                throw new IllegalStateException("JDBC session datasource is not ready.");
            }
            DatabaseMetaData metadata = connection.getMetaData();
            requireColumns(metadata, "SPRING_SESSION", SESSION_COLUMNS);
            requireColumns(metadata, "SPRING_SESSION_ATTRIBUTES", ATTRIBUTE_COLUMNS);
            requireColumns(metadata, "CPF_BFF_CREDENTIAL_VAULT", VAULT_COLUMNS);
            requireIndex(metadata, "SPRING_SESSION", "SESSION_ID");
            requireIndex(metadata, "SPRING_SESSION", "EXPIRY_TIME");
            requireIndex(metadata, "CPF_BFF_CREDENTIAL_VAULT", "REFRESH_EXPIRES_AT");
        } catch (Exception failure) {
            throw new IllegalStateException("CPF JDBC session/vault readiness verification failed.", failure);
        }
    }

    static byte[] decodeKey(String encoded, boolean required) {
        if (encoded == null || encoded.isBlank()) {
            if (required) {
                throw new IllegalStateException("CPF_BFF_CREDENTIAL_KEY is required in product profiles.");
            }
            return DEVELOPMENT_EPHEMERAL_KEY.clone();
        }
        try {
            return Base64.getDecoder().decode(encoded.trim());
        } catch (IllegalArgumentException failure) {
            throw new IllegalStateException("CPF BFF credential key is not valid Base64.", failure);
        }
    }

    private static byte[] createDevelopmentKey() {
        byte[] key = new byte[32];
        new java.security.SecureRandom().nextBytes(key);
        return key;
    }

    private static void requireColumns(
            DatabaseMetaData metadata,
            String table,
            Set<String> required) throws Exception {
        Set<String> actual = new HashSet<>();
        for (String candidate : Set.of(table, table.toLowerCase(Locale.ROOT))) {
            try (ResultSet columns = metadata.getColumns(null, null, candidate, null)) {
                while (columns.next()) {
                    actual.add(columns.getString("COLUMN_NAME").toUpperCase(Locale.ROOT));
                }
            }
        }
        if (!actual.containsAll(required)) {
            Set<String> missing = new HashSet<>(required);
            missing.removeAll(actual);
            throw new IllegalStateException("Missing JDBC columns " + table + ": " + missing);
        }
    }

    private static void requireIndex(
            DatabaseMetaData metadata,
            String table,
            String column) throws Exception {
        boolean found = false;
        for (String candidate : Set.of(table, table.toLowerCase(Locale.ROOT))) {
            try (ResultSet indexes = metadata.getIndexInfo(null, null, candidate, false, false)) {
                while (indexes.next()) {
                    String indexed = indexes.getString("COLUMN_NAME");
                    if (indexed != null && column.equalsIgnoreCase(indexed)) {
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            throw new IllegalStateException("Missing JDBC index for " + table + "." + column);
        }
    }
}
