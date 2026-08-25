package com.cpf.file.attachment.runtimecontrol;

import com.cpf.file.attachment.api.CpfAttachmentStoragePort;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.file.attachment.internal.CpfAttachmentRuntimePolicy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
/**
 * Attachment runtime-control capability를 구성하는 공개 AutoConfiguration입니다.
 * 고객 애플리케이션은 직접 인스턴스화하지 않고 Starter를 통해 활성화합니다.
 */
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
