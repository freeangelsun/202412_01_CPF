package com.cpf.core.common.remotelog;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** ADM과 실행 인스턴스 로그 adapter 사이의 CPF 공통 port입니다. */
public interface CpfRemoteLogArtifactPort {

    List<CpfRemoteLogArtifact> search(CpfRemoteLogArtifactSearch search);

    CpfRemoteLogPreview preview(String artifactId, int lastLines, String keyword);

    Path resolveDownload(String artifactId);

    default CpfRemoteLogBundle createBundle(List<String> artifactIds) {
        throw new UnsupportedOperationException("현재 로그 adapter는 묶음 다운로드를 지원하지 않습니다.");
    }

    default Map<String, Object> diagnostics() {
        return Map.of("adapter", getClass().getSimpleName());
    }
}
