package com.cpf.starter.sftp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CpfSftpPathPolicyTest {
    @TempDir Path root;

    @Test
    void rejectsRemoteSiblingPrefixAndTraversal() {
        CpfSftpPathPolicy policy = new CpfSftpPathPolicy(root.toString(), "/safe");
        assertThat(policy.remote("orders/a.txt")).isEqualTo("/safe/orders/a.txt");
        assertThatThrownBy(() -> policy.remote("/safe2/a.txt"))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> policy.remote("../../etc/passwd"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void rejectsLocalLexicalTraversal() {
        CpfSftpPathPolicy policy = new CpfSftpPathPolicy(root.toString(), "/safe");
        assertThatThrownBy(() -> policy.localTarget(Path.of("../outside.txt")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void rejectsLocalSymbolicLinkEscapeWhenSupported() throws Exception {
        Path outside = Files.createTempDirectory("cpf-sftp-outside-");
        Path link = root.resolve("escape");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (UnsupportedOperationException | java.nio.file.FileSystemException exception) {
            return;
        }
        CpfSftpPathPolicy policy = new CpfSftpPathPolicy(root.toString(), "/safe");
        assertThatThrownBy(() -> policy.localTarget(Path.of("escape/file.txt")))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void resolvesExistingFileInsideRoot() throws Exception {
        Path file = root.resolve("orders/a.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "payload");
        CpfSftpPathPolicy policy = new CpfSftpPathPolicy(root.toString(), "/safe");
        assertThat(policy.existingLocalFile(Path.of("orders/a.txt")))
                .isEqualTo(file.toRealPath());
    }
}
