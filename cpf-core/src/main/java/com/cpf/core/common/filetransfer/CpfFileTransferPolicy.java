package com.cpf.core.common.filetransfer;

/**
 * temp/rename/archive/중복 방지 정책 후보입니다.
 */
public record CpfFileTransferPolicy(
        boolean useTempFile,
        String tempSuffix,
        boolean atomicRename,
        String archiveDirectory,
        String duplicatePolicy,
        CpfFileTransferRetryPolicy retryPolicy) {

    public CpfFileTransferPolicy {
        tempSuffix = tempSuffix == null || tempSuffix.isBlank() ? ".tmp" : tempSuffix;
        duplicatePolicy = duplicatePolicy == null || duplicatePolicy.isBlank() ? "REJECT" : duplicatePolicy;
        retryPolicy = retryPolicy == null ? new CpfFileTransferRetryPolicy(1, null, false) : retryPolicy;
    }
}
