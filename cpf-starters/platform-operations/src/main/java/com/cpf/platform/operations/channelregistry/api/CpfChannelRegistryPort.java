package com.cpf.platform.operations.channelregistry.api;

import com.cpf.platform.operations.channelregistry.model.CpfChannelDefinition;
import com.cpf.platform.operations.channelregistry.model.CpfChannelExecutionPolicy;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicySnapshot;

/** CPF 채널 정책 저장소와 애플리케이션 계층을 분리하는 포트입니다. */
public interface CpfChannelRegistryPort {
    CpfChannelPolicySnapshot loadSnapshot();

    long saveChannel(CpfChannelDefinition channel, String actor, String reason);

    long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason);
}
