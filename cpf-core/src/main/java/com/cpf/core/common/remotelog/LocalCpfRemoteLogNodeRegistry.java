package com.cpf.core.common.remotelog;

import java.util.List;
import java.util.Optional;

/** 외부 registry가 없는 local profile에서 현재 인스턴스 하나만 노출합니다. */
public final class LocalCpfRemoteLogNodeRegistry implements CpfRemoteLogNodeRegistryPort {
    private final CpfRemoteLogNode localNode;

    public LocalCpfRemoteLogNodeRegistry(CpfRemoteLogNode localNode) {
        this.localNode = localNode;
    }

    @Override
    public List<CpfRemoteLogNode> findOnlineNodes(CpfRemoteLogArtifactSearch search) {
        if (search != null && (!matches(search.environment(), localNode.environment())
                || !matches(search.module(), localNode.module())
                || !matches(search.service(), localNode.service())
                || !matches(search.instance(), localNode.instance()))) {
            return List.of();
        }
        return List.of(localNode);
    }

    @Override
    public Optional<CpfRemoteLogNode> findById(String nodeId) {
        return localNode.nodeId().equals(nodeId) ? Optional.of(localNode) : Optional.empty();
    }

    private boolean matches(String expected, String actual) {
        return expected == null || expected.isBlank() || expected.equalsIgnoreCase(actual);
    }
}
