package com.cpf.core.common.remotelog;

import java.util.List;
import java.util.Optional;

/** 원격 로그 대상 인스턴스를 서비스 registry 구현과 분리하는 port입니다. */
public interface CpfRemoteLogNodeRegistryPort {

    List<CpfRemoteLogNode> findOnlineNodes(CpfRemoteLogArtifactSearch search);

    Optional<CpfRemoteLogNode> findById(String nodeId);
}
