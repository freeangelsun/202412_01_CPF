package com.cpf.bizadmin.auth.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BzaPermissionManifestTest {
    private final BzaPermissionManifest manifest = new BzaPermissionManifest(new ObjectMapper());

    @Test
    void resolvesGenericAndDangerousActionsFromOneManifest() {
        assertThat(manifest.resolve("GET", "backoffice/organizations")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("READ");
        assertThat(manifest.resolve("POST", "backoffice/organizations")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("WRITE");
        assertThat(manifest.resolve("DELETE", "backoffice/organizations/ORG001")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("DELETE");
        assertThat(manifest.resolve("POST", "permissions/simulate")).get()
                .extracting(BzaPermissionManifest.ApiPermission::actionCode).isEqualTo("SIMULATE");
        assertThat(manifest.resolve("GET", "attachments/7/download")).get()
                .isEqualTo(new BzaPermissionManifest.ApiPermission("ATTACHMENT", "DOWNLOAD"));
        assertThat(manifest.resolve("GET", "approvals/inbox")).get()
                .isEqualTo(new BzaPermissionManifest.ApiPermission("APPROVAL", "READ"));
        assertThat(manifest.resolve("POST", "approvals/submissions/7/decisions")).get()
                .isEqualTo(new BzaPermissionManifest.ApiPermission("APPROVAL", "DECIDE"));
    }

    @Test
    void unknownApiIsNotImplicitlyAuthorized() {
        assertThat(manifest.resolve("GET", "unknown-resource")).isEmpty();
    }
}
