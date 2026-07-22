package cpf.pfw.common.filetransfer;

/**
 * 파일 checksum 검증 결과입니다.
 */
public record CpfFileChecksumValidationResult(
        boolean valid,
        String algorithm,
        String expectedChecksum,
        String actualChecksum,
        String detail) {
}
