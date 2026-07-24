package com.cpf.core.common.database;

import com.cpf.core.api.database.CpfDatabaseVendor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfVendorSqlCatalogTest {
    private static final Map<String, Integer> CPF_STATEMENT_PARAMETER_COUNTS = Map.ofEntries(
            Map.entry("idempotency-reserve", 7),
            Map.entry("idempotency-find", 2),
            Map.entry("idempotency-complete", 5),
            Map.entry("idempotency-restart", 6),
            Map.entry("idempotency-expire-before", 2),
            Map.entry("reconciliation-register", 10),
            Map.entry("reconciliation-find", 5),
            Map.entry("reconciliation-resolve", 5),
            Map.entry("file-transfer-already-processed", 4),
            Map.entry("file-transfer-insert", 13),
            Map.entry("file-transfer-update", 4),
            Map.entry("file-transfer-find-history", 7),
            Map.entry("channel-registry-find-all", 0),
            Map.entry("channel-policy-find-all", 0),
            Map.entry("channel-policy-version-current", 0),
            Map.entry("channel-registry-upsert", 13),
            Map.entry("channel-policy-upsert", 15),
            Map.entry("channel-policy-version-insert", 6),
            Map.entry("execution-catalog-upsert", 18),
            Map.entry("execution-catalog-find-all", 0),
            Map.entry("execution-catalog-resolve-alias", 1),
            Map.entry("transaction-meta-table-available", 0),
            Map.entry("transaction-meta-upsert", 14),
            Map.entry("transaction-meta-mark-missing-inactive", 1),
            Map.entry("transaction-meta-find-by-id", 1),
            Map.entry("transaction-meta-find-all", 7),
            Map.entry("transaction-meta-inactivate", 2));

    @TempDir
    Path tempDirectory;

    @Test
    void createsDeterministicVendorResourcePath() {
        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(
                CpfDatabaseVendor.ORACLE,
                "cpf",
                getClass().getClassLoader());

        assertThat(catalog.resourcePath("claim-outbox"))
                .isEqualTo("sql/vendor/oracle/cpf/claim-outbox.sql");
    }

    @Test
    void failsClosedWhenSelectedVendorStatementIsMissing() {
        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(
                CpfDatabaseVendor.SQLSERVER,
                "cpf",
                getClass().getClassLoader());

        assertThatThrownBy(() -> catalog.required("not-present"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sql/vendor/sqlserver/cpf/not-present.sql");
    }

    @Test
    void rejectsPathTraversalTokens() {
        assertThatThrownBy(() -> CpfVendorSqlCatalog.create(
                CpfDatabaseVendor.MARIADB,
                "../cpf",
                getClass().getClassLoader()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void externalCentralPackHasPriorityWhenResourceRootIsSelected() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Path repository = packRoot.resolve("runtime/cpf/repository");
        Files.createDirectories(repository);
        Files.writeString(repository.resolve("channel-policy-find-all.sql"), "SELECT 'external-pack'");

        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot), "cpf");

        assertThat(catalog.resourcePath("channel-policy-find-all"))
                .isEqualTo("runtime/cpf/repository/channel-policy-find-all.sql");
        assertThat(catalog.required("channel-policy-find-all"))
                .isEqualTo("SELECT 'external-pack'");
    }

    @Test
    void selectedExternalPackNeverFallsBackToClasspath() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Files.createDirectories(packRoot.resolve("runtime/cpf/repository"));

        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot), "cpf");

        assertThatThrownBy(() -> catalog.required("channel-policy-find-all"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("runtime")
                .hasMessageContaining("channel-policy-find-all.sql");
    }

    @Test
    void rejectsExternalPackForDifferentVendor() throws IOException {
        Path packRoot = centralPackRoot("mysql");
        Files.createDirectories(packRoot.resolve("runtime/cpf/repository"));

        assertThatThrownBy(() -> CpfVendorSqlCatalog.create(environment(packRoot), "cpf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("일치하지 않습니다")
                .hasMessageContaining("selected=mariadb")
                .hasMessageContaining("pack=mysql");
    }

    @Test
    void rejectsExternalRepositorySymlinkThatEscapesSelectedPack() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Path repository = packRoot.resolve("runtime/cpf/repository");
        Files.createDirectories(repository);
        Path outsideSql = tempDirectory.resolve("outside.sql");
        Files.writeString(outsideSql, "SELECT 'outside'");
        Path linkedSql = repository.resolve("channel-policy-find-all.sql");

        try {
            Files.createSymbolicLink(linkedSql, outsideSql);
        } catch (UnsupportedOperationException | IOException | SecurityException ex) {
            return;
        }

        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot), "cpf");
        assertThatThrownBy(() -> catalog.required("channel-policy-find-all"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("symbolic link");
    }

    @Test
    void allSupportedVendorsProvideTheSameCpfStatementAndParameterContracts() {
        for (CpfDatabaseVendor vendor : CpfDatabaseVendor.values()) {
            CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(
                    vendor,
                    "cpf",
                    getClass().getClassLoader());
            CPF_STATEMENT_PARAMETER_COUNTS.forEach((statementKey, expectedCount) ->
                    assertThat(countParameters(catalog.required(statementKey)))
                            .as("%s:%s parameter contract", vendor.id(), statementKey)
                            .isEqualTo(expectedCount.longValue()));
        }
    }

    @Test
    void vendorSpecificUpsertSyntaxStaysInsideResourcePacks() {
        assertThat(sql(CpfDatabaseVendor.MARIADB, "channel-registry-upsert"))
                .contains("ON DUPLICATE KEY UPDATE");
        assertThat(sql(CpfDatabaseVendor.MYSQL, "execution-catalog-upsert"))
                .contains("ON DUPLICATE KEY UPDATE");
        assertThat(sql(CpfDatabaseVendor.POSTGRESQL, "channel-policy-upsert"))
                .contains("ON CONFLICT");
        assertThat(sql(CpfDatabaseVendor.ORACLE, "channel-policy-upsert"))
                .startsWith("MERGE INTO");
        assertThat(sql(CpfDatabaseVendor.SQLSERVER, "execution-catalog-upsert"))
                .startsWith("MERGE INTO");
    }

    private String sql(CpfDatabaseVendor vendor, String statementKey) {
        return CpfVendorSqlCatalog.create(vendor, "cpf", getClass().getClassLoader())
                .required(statementKey);
    }

    private long countParameters(String sql) {
        return sql.chars().filter(character -> character == '?').count();
    }

    private Path centralPackRoot(String vendor) throws IOException {
        Path root = tempDirectory.resolve("vendor").resolve(vendor);
        Files.createDirectories(root);
        Files.writeString(
                root.resolve("pack.json"),
                """
                {
                  "vendor": "%s",
                  "schemaVersion": 1,
                  "status": "test"
                }
                """.formatted(vendor));
        return root;
    }

    private MockEnvironment environment(Path packRoot) {
        return new MockEnvironment()
                .withProperty("cpf.db.vendor", "mariadb")
                .withProperty(CpfVendorResourceRoot.PROPERTY_NAME, packRoot.toString());
    }
}
