package com.cpf.core.common.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfVendorSqlCatalogTest {

    @TempDir
    Path tempDirectory;

    @Test
    void failsFastWhenCentralPackRootIsMissing() {
        MockEnvironment environment = new MockEnvironment().withProperty("cpf.db.vendor", "mariadb");

        assertThatThrownBy(() -> CpfVendorSqlCatalog.create(environment, "cpf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(CpfVendorResourceRoot.PROPERTY_NAME)
                .hasMessageContaining("fallback");
    }

    @Test
    void readsStatementOnlyFromSelectedCentralPack() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Path repository = packRoot.resolve("runtime/cpf/repository");
        Files.createDirectories(repository);
        Files.writeString(repository.resolve("channel-policy-find-all.sql"), "SELECT 'central-pack'");

        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot, "mariadb"), "cpf");

        assertThat(catalog.resourcePath("channel-policy-find-all"))
                .isEqualTo("runtime/cpf/repository/channel-policy-find-all.sql");
        assertThat(catalog.required("channel-policy-find-all"))
                .isEqualTo("SELECT 'central-pack'");
    }

    @Test
    void missingCentralStatementFailsClosed() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Files.createDirectories(packRoot.resolve("runtime/cpf/repository"));
        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot, "mariadb"), "cpf");

        assertThatThrownBy(() -> catalog.required("not-present"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("runtime")
                .hasMessageContaining("not-present.sql");
    }

    @Test
    void rejectsUnsafeModuleAndStatementTokens() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        assertThatThrownBy(() -> CpfVendorSqlCatalog.create(environment(packRoot, "mariadb"), "../cpf"))
                .isInstanceOf(IllegalArgumentException.class);

        Files.createDirectories(packRoot.resolve("runtime/cpf/repository"));
        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot, "mariadb"), "cpf");
        assertThatThrownBy(() -> catalog.required("../secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsCentralPackForDifferentVendor() throws IOException {
        Path packRoot = centralPackRoot("mysql");

        assertThatThrownBy(() -> CpfVendorSqlCatalog.create(environment(packRoot, "mariadb"), "cpf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("selected=mariadb")
                .hasMessageContaining("pack=mysql");
    }

    @Test
    void rejectsRepositorySymlinkThatEscapesSelectedPack() throws IOException {
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

        CpfVendorSqlCatalog catalog = CpfVendorSqlCatalog.create(environment(packRoot, "mariadb"), "cpf");
        assertThatThrownBy(() -> catalog.required("channel-policy-find-all"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("symbolic link");
    }

    private Path centralPackRoot(String vendor) throws IOException {
        Path root = tempDirectory.resolve("vendor").resolve(vendor);
        Files.createDirectories(root);
        Files.writeString(root.resolve("pack.json"), """
                {
                  "vendor": "%s",
                  "schemaVersion": 1,
                  "status": "test"
                }
                """.formatted(vendor));
        return root;
    }

    private MockEnvironment environment(Path packRoot, String vendor) {
        return new MockEnvironment()
                .withProperty("cpf.db.vendor", vendor)
                .withProperty(CpfVendorResourceRoot.PROPERTY_NAME, packRoot.toString());
    }
}
