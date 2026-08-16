package com.cpf.batch.agent;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.BatchRuntimePolicyApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Host Agent가 실제 소비하는 명령/로그 정책 capability만 등록합니다. */
@Configuration
public class AgentRuntimePolicyConfiguration {
    @Bean(name = "batchAgentPolicyRuntimeApplier")
    @ConditionalOnMissingBean(name = "batchAgentPolicyRuntimeApplier")
    CpfRuntimeChangeApplier batchAgentPolicyRuntimeApplier(BatchRuntimePolicy policy) {
        return new BatchRuntimePolicyApplier(BatchRuntimePolicyApplier.AGENT_POLICY, policy);
    }
}
