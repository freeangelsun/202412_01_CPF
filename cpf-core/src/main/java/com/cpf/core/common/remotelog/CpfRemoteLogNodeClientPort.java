package com.cpf.core.common.remotelog;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

/** 로컬 또는 원격 인스턴스의 로그 artifact API를 호출하는 client port입니다. */
public interface CpfRemoteLogNodeClientPort {

    List<CpfRemoteLogArtifact> search(
            CpfRemoteLogNode node,
            CpfRemoteLogArtifactSearch search,
            CpfRemoteLogAccessContext context);

    CpfRemoteLogPreview preview(
            CpfRemoteLogNode node,
            String artifactId,
            int lastLines,
            String keyword,
            CpfRemoteLogAccessContext context);

    Path stageDownload(CpfRemoteLogNode node, String artifactId, CpfRemoteLogAccessContext context);

    record CpfRemoteLogAccessContext(String serviceToken, Duration timeout) {
    }
}
