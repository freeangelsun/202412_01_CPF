package com.cpf.starter.security.session.jdbc.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.common.security.password.CpfPasswordRuntimePolicy;
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
