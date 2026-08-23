package com.cpf.backoffice.online.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackofficeBootstrapRunnerSecurityTest {
    @TempDir Path temp;

    @Test
    void acceptsOwnerOnlySecretAndDestroysIt() throws Exception {
        Path secret = temp.resolve("secret.txt");
        Files.writeString(secret, "Sensitive!12345", StandardCharsets.UTF_8);
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            Files.setPosixFilePermissions(secret, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } else {
            setOwnerOnlyWindows(secret);
        }
        assertThat(BackofficeBootstrapRunner.secureSecretFile(secret.toString(), "TEST")).isEqualTo(secret.toAbsolutePath());
        BackofficeBootstrapRunner.destroySecretFile(secret);
        assertThat(secret).doesNotExist();
    }

    @Test
    void rejectsSecretReadableByGroupOrOthers() throws Exception {
        if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) return;
        Path secret = temp.resolve("unsafe.txt");
        Files.writeString(secret, "Sensitive!12345", StandardCharsets.UTF_8);
        Files.setPosixFilePermissions(secret, Set.of(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.GROUP_READ));

        assertThatThrownBy(() -> BackofficeBootstrapRunner.secureSecretFile(secret.toString(), "TEST"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("POSIX_PERMISSION_UNSAFE");
    }

    @Test
    void rejectsWeakOrLoginDerivedPassword() {
        assertThatThrownBy(() -> BackofficeBootstrapRunner.requireStrongPassword("operator01", "operator01!AA12".toCharArray()))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> BackofficeBootstrapRunner.requireStrongPassword("operator01", "short1!A".toCharArray()))
                .isInstanceOf(SecurityException.class);
    }

    private static void setOwnerOnlyWindows(Path path) throws Exception {
        AclFileAttributeView view = Files.getFileAttributeView(
                path, AclFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null) throw new IllegalStateException("TEST_ACL_UNSUPPORTED");
        UserPrincipal owner = Files.getOwner(path, LinkOption.NOFOLLOW_LINKS);
        AclEntry ownerOnly = AclEntry.newBuilder()
                .setType(AclEntryType.ALLOW)
                .setPrincipal(owner)
                .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                .build();
        view.setAcl(List.of(ownerOnly));
    }
}
