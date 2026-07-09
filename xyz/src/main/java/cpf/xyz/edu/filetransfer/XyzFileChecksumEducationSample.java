package cpf.xyz.edu.filetransfer;

import cpf.pfw.common.archive.CpfArchiveChecksum;

import java.nio.charset.StandardCharsets;

/**
 * 업무 파일 전송 전 checksum을 계산하는 샘플입니다.
 */
public class XyzFileChecksumEducationSample {

    public String sha256(String content) {
        return CpfArchiveChecksum.sha256(content.getBytes(StandardCharsets.UTF_8));
    }
}
