package com.cpf.starter.sftp;

import java.util.Locale;
import java.util.Objects;

/** Shared deterministic policy for SFTP upload publication and ambiguous provider outcomes. */
final class CpfSftpUploadOutcomePolicy {
    private CpfSftpUploadOutcomePolicy() {
    }

    static String remoteWorkPath(String target, String transferId, boolean resume) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(transferId, "transferId");
        return resume ? target : target + ".cpf-part-" + transferId;
    }

    static String failureStatus(boolean remoteMutationStarted) {
        return remoteMutationStarted ? "UNKNOWN" : "FAILED";
    }

    static boolean checksumMatches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.trim().toLowerCase(Locale.ROOT)
                .equals(actual.trim().toLowerCase(Locale.ROOT));
    }
}
