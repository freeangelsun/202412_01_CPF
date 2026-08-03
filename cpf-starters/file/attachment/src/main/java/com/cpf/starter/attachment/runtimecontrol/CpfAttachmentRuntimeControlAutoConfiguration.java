package com.cpf.starter.attachment.runtimecontrol;

import com.cpf.core.api.attachment.CpfAttachmentStoragePort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.common.attachment.CpfAttachmentRuntimePolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfAttachmentRuntimeControlAutoConfiguration {
    @Bean(name = "cpfAttachmentPolicyRuntimeApplier")
    @ConditionalOnBean({CpfAttachmentRuntimePolicy.class, CpfAttachmentStoragePort.class})
    @ConditionalOnMissingBean(name = "cpfAttachmentPolicyRuntimeApplier")
    CpfRuntimeChangeApplier attachmentPolicyRuntimeApplier(
            CpfAttachmentRuntimePolicy policy) {
        return new CpfAttachmentPolicyRuntimeApplier(policy);
    }

    @Bean(name = "cpfDownloadPolicyRuntimeApplier")
    @ConditionalOnBean({CpfAttachmentRuntimePolicy.class, CpfAttachmentStoragePort.class})
    @ConditionalOnMissingBean(name = "cpfDownloadPolicyRuntimeApplier")
    CpfRuntimeChangeApplier downloadPolicyRuntimeApplier(
            CpfAttachmentRuntimePolicy policy) {
        return new CpfDownloadPolicyRuntimeApplier(policy);
    }
}
