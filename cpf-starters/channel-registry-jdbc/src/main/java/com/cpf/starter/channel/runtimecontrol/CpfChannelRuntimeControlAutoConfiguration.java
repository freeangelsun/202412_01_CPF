package com.cpf.starter.channel.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfChannelRuntimeControlAutoConfiguration {
    @Bean(name = "cpfChannelPolicyRuntimeApplier")
    @ConditionalOnBean(CpfChannelPolicyService.class)
    @ConditionalOnMissingBean(name = "cpfChannelPolicyRuntimeApplier")
    CpfRuntimeChangeApplier channelPolicyRuntimeApplier(CpfChannelPolicyService service) {
        return new CpfChannelPolicyRuntimeApplier(service);
    }
}
