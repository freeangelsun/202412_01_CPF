package com.cpf.core.common.remotelog;

import java.util.List;

/** 마스킹을 적용한 로그 미리보기입니다. */
public record CpfRemoteLogPreview(
        CpfRemoteLogArtifact artifact,
        List<String> lines,
        int returnedLineCount,
        boolean truncated,
        String keyword) {
}
