package com.cpf.starter.attachment;

import com.cpf.core.api.attachment.CpfAttachmentStoragePort;
import java.util.Set;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Spring Web가 실제 존재할 때만 multipart convenience bean을 노출합니다. */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.multipart.MultipartFile")
@ConditionalOnBean(CpfAttachmentStoragePort.class)
public class CpfMultipartAttachmentAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfAttachmentUploadPolicy cpfAttachmentUploadPolicy() {
        return new CpfAttachmentUploadPolicy(CpfAttachmentUploadPolicy.DEFAULT_MAX_BYTES, Set.of(), Set.of());
    }
    @Bean @ConditionalOnMissingBean
    CpfMultipartAttachmentAdapter cpfMultipartAttachmentAdapter(CpfAttachmentStoragePort storage, CpfAttachmentUploadPolicy policy) {
        return new CpfMultipartAttachmentAdapter(storage, policy);
    }
}
