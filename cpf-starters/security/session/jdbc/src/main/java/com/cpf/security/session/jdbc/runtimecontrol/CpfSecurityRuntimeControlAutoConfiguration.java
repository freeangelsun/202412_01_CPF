package com.cpf.security.session.jdbc.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.security.api.CpfPasswordRuntimePolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfSecurityRuntimeControlAutoConfiguration {
    @Bean(name = "cpfPasswordPolicyRuntimeApplier")
    @ConditionalOnBean(CpfPasswordRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfPasswordPolicyRuntimeApplier")
    CpfRuntimeChangeApplier passwordPolicyRuntimeApplier(CpfPasswordRuntimePolicy policy) {
        return new CpfPasswordPolicyRuntimeApplier(policy);
    }
}
