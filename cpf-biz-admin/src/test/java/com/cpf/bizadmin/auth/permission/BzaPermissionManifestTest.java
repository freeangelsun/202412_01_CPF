package com.cpf.bizadmin.auth.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BzaPermissionManifestTest {
    private final BzaPermissionManifest manifest =
            new BzaPermissionManifest(new ObjectMapper());

    @Test
    void resolvesCanonicalProductMenuGroups() {
        assertThat(manifest.resolveApiMenuCode("admin-users/page")).contains("AUTHORIZATION");
        assertThat(manifest.resolveApiMenuCode("directory/user-roles/page")).contains("AUTHORIZATION");
        assertThat(manifest.resolveApiMenuCode("backoffice/organizations")).contains("ORGANIZATION");
        assertThat(manifest.resolveApiMenuCode("notifications")).contains("SETTING");
        assertThat(manifest.resolveApiMenuCode("download-audits")).contains("AUDIT");
        assertThat(manifest.resolveApiMenuCode("not-registered")).isEmpty();
    }

    @Test
    void mariaDbProductSeedProjectsEveryCanonicalMenuGroup() throws IOException {
        Path repositoryRoot = repositoryRoot();
        Path projection = repositoryRoot.resolve(manifest.sourceProjection());
        String sql = Files.readString(projection, StandardCharsets.UTF_8);

        for (String menuGroup : manifest.menuGroups()) {
            assertThat(sql)
                    .as("BZA product seed menu group %s", menuGroup)
                    .contains("'BZA_" + menuGroup + "'");
        }
        assertThat(sql)
                .contains("SELECT 'BZA_ADMIN', menu_code, 'ALL'")
                .contains("FROM bza_menu")
                .contains("WHERE use_yn = 'Y'");
    }

    private Path repositoryRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null && !Files.isRegularFile(current.resolve("settings.gradle"))) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("CPF repository root를 찾을 수 없습니다.");
        }
        return current;
    }
}
