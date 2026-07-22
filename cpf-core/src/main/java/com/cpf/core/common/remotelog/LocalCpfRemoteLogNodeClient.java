package com.cpf.core.common.remotelog;

import java.nio.file.Path;
import java.util.List;

/** 현재 프로세스 로그 adapter를 node client 계약에 연결합니다. */
public final class LocalCpfRemoteLogNodeClient implements CpfRemoteLogNodeClientPort {
    private final String localNodeId;
    private final CpfRemoteLogArtifactPort delegate;

    public LocalCpfRemoteLogNodeClient(String localNodeId, CpfRemoteLogArtifactPort delegate) {
        this.localNodeId = localNodeId;
        this.delegate = delegate;
    }

    @Override
    public List<CpfRemoteLogArtifact> search(
            CpfRemoteLogNode node,
            CpfRemoteLogArtifactSearch search,
            CpfRemoteLogAccessContext context) {
        requireLocal(node);
        return delegate.search(search);
    }

    @Override
    public CpfRemoteLogPreview preview(
            CpfRemoteLogNode node,
            String artifactId,
            int lastLines,
            String keyword,
            CpfRemoteLogAccessContext context) {
        requireLocal(node);
        return delegate.preview(artifactId, lastLines, keyword);
    }

    @Override
    public Path stageDownload(CpfRemoteLogNode node, String artifactId, CpfRemoteLogAccessContext context) {
        requireLocal(node);
        return delegate.resolveDownload(artifactId);
    }

    private void requireLocal(CpfRemoteLogNode node) {
        if (!node.local() || !localNodeId.equals(node.nodeId())) {
            throw new IllegalStateException("원격 로그 node client adapter가 구성되지 않았습니다. node=" + node.nodeId());
        }
    }
}
