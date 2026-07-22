package com.cpf.core.channel.api;

import com.cpf.core.channel.model.CpfChannelDefinition;
import com.cpf.core.channel.model.CpfChannelExecutionPolicy;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;

/** CPF 채널 정책 저장소와 애플리케이션 계층을 분리하는 포트입니다. */
public interface CpfChannelRegistryPort {
    CpfChannelPolicySnapshot loadSnapshot();

    long saveChannel(CpfChannelDefinition channel, String actor, String reason);

    long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason);
}
