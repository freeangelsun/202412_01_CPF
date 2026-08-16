package com.cpf.platform.operations.channelregistry.jdbc.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
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
