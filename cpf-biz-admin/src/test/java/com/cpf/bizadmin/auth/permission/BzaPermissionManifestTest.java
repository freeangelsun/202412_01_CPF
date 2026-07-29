package com.cpf.bizadmin.auth.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BzaPermissionManifestTest {
    private final BzaPermissionManifest manifest = new BzaPermissionManifest(new ObjectMapper());

    @Test
    void resolvesGenericAndDangerousActionsFromOneManifest() {
        assertThat(manifest.resolve("GET", "organizations")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("READ");
        assertThat(manifest.resolve("POST", "organizations")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("WRITE");
        assertThat(manifest.resolve("DELETE", "organizations/ORG001")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("DELETE");
        assertThat(manifest.resolve("POST", "permissions/simulate")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("SIMULATE");
    }

    @Test
    void unknownApiIsNotImplicitlyAuthorized() {
        assertThat(manifest.resolve("GET", "unknown-resource")).isEmpty();
    }
}
