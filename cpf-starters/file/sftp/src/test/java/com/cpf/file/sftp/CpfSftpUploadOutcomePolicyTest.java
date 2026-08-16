package com.cpf.file.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfSftpUploadOutcomePolicyTest {
    @Test
    void classifiesFailureBeforeAndAfterRemoteMutationBoundary() {
        assertEquals("FAILED", CpfSftpUploadOutcomePolicy.failureStatus(false));
        assertEquals("UNKNOWN", CpfSftpUploadOutcomePolicy.failureStatus(true));
    }

    @Test
    void usesTemporaryRemotePathForAtomicNonResumePublication() {
        assertEquals(
                "/root/file.dat.cpf-part-tx-1",
                CpfSftpUploadOutcomePolicy.remoteWorkPath("/root/file.dat", "tx-1", false));
        assertEquals(
                "/root/file.dat",
                CpfSftpUploadOutcomePolicy.remoteWorkPath("/root/file.dat", "tx-1", true));
    }

    @Test
    void comparesChecksumsCaseInsensitivelyButFailsClosedForMissingValues() {
        assertTrue(CpfSftpUploadOutcomePolicy.checksumMatches("aBcD", "ABCD"));
        assertFalse(CpfSftpUploadOutcomePolicy.checksumMatches("abcd", "abce"));
        assertFalse(CpfSftpUploadOutcomePolicy.checksumMatches(null, "abcd"));
    }
}
