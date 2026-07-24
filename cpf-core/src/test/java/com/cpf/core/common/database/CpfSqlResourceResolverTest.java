package com.cpf.core.common.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfSqlResourceResolverTest {

    @TempDir
    Path tempDirectory;

    @Test
    void failsFastWhenCentralPackRootIsMissing() {
        MockEnvironment environment = new MockEnvironment().withProperty("cpf.db.vendor", "mariadb");

        assertThatThrownBy(() -> CpfSqlResourceResolver.mapperPattern(environment, "cpf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(CpfVendorResourceRoot.PROPERTY_NAME)
                .hasMessageContaining("fallback");
        assertThatThrownBy(() -> CpfSqlResourceResolver.flywayLocation(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(CpfVendorResourceRoot.PROPERTY_NAME);
    }

    @Test
    void rejectsInvalidModulePathBeforeResourceResolution() {
        assertThatThrownBy(() -> CpfSqlResourceResolver.mapperPattern(
                new MockEnvironment(), "../cpf"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void readsOnlySelectedCentralMapperPackInDeterministicOrder() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Path mapperRoot = packRoot.resolve("runtime/payment-domain/mybatis/query");
        Files.createDirectories(mapperRoot);
        Files.writeString(mapperRoot.resolve("ZMapper.xml"), "<mapper namespace=\"z\"/>");
        Files.writeString(mapperRoot.resolve("AMapper.xml"), "<mapper namespace=\"a\"/>");

        MockEnvironment environment = environment(packRoot, "mariadb");
        Resource[] resources = CpfSqlResourceResolver.mapperResources(environment, "payment-domain");

        assertThat(CpfSqlResourceResolver.mapperPattern(environment, "payment-domain"))
                .startsWith("file:")
                .endsWith("/runtime/payment-domain/mybatis/**/*.xml");
        assertThat(resources).extracting(Resource::getFilename)
                .containsExactly("AMapper.xml", "ZMapper.xml");
    }

    @Test
    void centralMapperPackDoesNotFallbackWhenSelectedModuleIsEmpty() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Files.createDirectories(packRoot.resolve("runtime/cpf/mybatis"));

        assertThatThrownBy(() -> CpfSqlResourceResolver.mapperResources(
                environment(packRoot, "mariadb"), "cpf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("비어 있습니다");
    }

    @Test
    void buildsFilesystemFlywayLocationFromSelectedPack() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Files.createDirectories(packRoot.resolve("migration"));

        assertThat(CpfSqlResourceResolver.flywayLocation(environment(packRoot, "mariadb")))
                .isEqualTo("filesystem:"
                        + packRoot.resolve("migration").toRealPath().toString().replace('\\', '/'));
    }

    @Test
    void rejectsPackForDifferentVendor() throws IOException {
        Path packRoot = centralPackRoot("mysql");
        Files.createDirectories(packRoot.resolve("runtime/cpf/mybatis"));

        assertThatThrownBy(() -> CpfSqlResourceResolver.mapperPattern(
                environment(packRoot, "mariadb"), "cpf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("일치하지 않습니다");
    }

    @Test
    void rejectsMapperSymlinkThatEscapesSelectedPack() throws IOException {
        Path packRoot = centralPackRoot("mariadb");
        Path mapperRoot = packRoot.resolve("runtime/cpf/mybatis");
        Files.createDirectories(mapperRoot);
        Path outsideMapper = tempDirectory.resolve("OutsideMapper.xml");
        Files.writeString(outsideMapper, "<mapper namespace=\"outside\"/>");
        Path linkedMapper = mapperRoot.resolve("LinkedMapper.xml");

        try {
            Files.createSymbolicLink(linkedMapper, outsideMapper);
        } catch (UnsupportedOperationException | IOException | SecurityException ex) {
            return;
        }

        assertThatThrownBy(() -> CpfSqlResourceResolver.mapperResources(
                environment(packRoot, "mariadb"), "cpf"))
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
