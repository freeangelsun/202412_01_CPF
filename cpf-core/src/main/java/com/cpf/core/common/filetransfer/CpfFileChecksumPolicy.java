package com.cpf.core.common.filetransfer;

/**
 * 파일 checksum 검증 정책 후보입니다.
 */
public record CpfFileChecksumPolicy(
        String algorithm,
        boolean required,
        String expectedChecksum) {

    public CpfFileChecksumPolicy {
        algorithm = algorithm == null || algorithm.isBlank() ? "SHA-256" : algorithm;
    }
}
