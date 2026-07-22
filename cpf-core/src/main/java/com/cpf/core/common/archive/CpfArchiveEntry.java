package cpf.pfw.common.archive;

import java.util.Arrays;

/**
 * 압축 파일 안에 들어갈 논리 entry입니다.
 */
public record CpfArchiveEntry(String name, byte[] content) {

    public CpfArchiveEntry {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("압축 entry 이름은 필수입니다.");
        }
        content = content == null ? new byte[0] : Arrays.copyOf(content, content.length);
    }

    @Override
    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

    public long size() {
        return content.length;
    }
}
