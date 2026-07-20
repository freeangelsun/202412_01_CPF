package cpf.pfw.channel.api;

import cpf.pfw.channel.model.CpfChannelDefinition;
import cpf.pfw.channel.model.CpfChannelExecutionPolicy;
import cpf.pfw.channel.model.CpfChannelPolicySnapshot;

/** PFW 채널 정책 저장소와 애플리케이션 계층을 분리하는 포트입니다. */
public interface CpfChannelRegistryPort {
    CpfChannelPolicySnapshot loadSnapshot();

    long saveChannel(CpfChannelDefinition channel, String actor, String reason);

    long savePolicy(CpfChannelExecutionPolicy policy, String actor, String reason);
}
