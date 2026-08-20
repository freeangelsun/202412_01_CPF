package com.cpf.security.session.jdbc.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.security.api.CpfPasswordRuntimePolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * JDBC Session/Security capability의 Runtime 운영 상태와 제어 정보를 CPF 운영 관리 체계에 연결합니다.
 * <p>인증 업무 API가 아니라 운영용 자동구성이며 Security Starter 내부에서 자동 적용됩니다.
 */
@AutoConfiguration
public class CpfSecurityRuntimeControlAutoConfiguration {
    @Bean(name = "cpfPasswordPolicyRuntimeApplier")
    @ConditionalOnBean(CpfPasswordRuntimePolicy.class)
    @ConditionalOnMissingBean(name = "cpfPasswordPolicyRuntimeApplier")
    CpfRuntimeChangeApplier passwordPolicyRuntimeApplier(CpfPasswordRuntimePolicy policy) {
        return new CpfPasswordPolicyRuntimeApplier(policy);
    }
}
