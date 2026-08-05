package com.cpf.starter.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CpfSftpPathPolicyTest {
    @TempDir
    Path localRoot;

    @Test
    void permitsChildrenWhenRemoteRootIsFilesystemRoot() {
        CpfSftpPathPolicy policy = new CpfSftpPathPolicy(localRoot.toString(), "/");

        assertEquals("/sub/file.dat", policy.remote("sub/file.dat"));
        assertEquals("/absolute/file.dat", policy.remote("/absolute/file.dat"));
    }

    @Test
    void rejectsEscapesFromConfiguredRemoteRoot() {
        CpfSftpPathPolicy policy = new CpfSftpPathPolicy(localRoot.toString(), "/tenant/inbound");

        assertThrows(SecurityException.class, () -> policy.remote("../../other/file.dat"));
        assertThrows(SecurityException.class, () -> policy.remote("/tenant/outbound/file.dat"));
    }
}
