package com.cpf.file.attachment;

import com.cpf.file.attachment.api.CpfAttachmentStoragePort;
import java.util.Set;
import com.cpf.file.config.CpfAttachmentAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Spring Web가 실제 존재할 때만 multipart convenience bean을 노출합니다.
 *
 * <p>{@code @ConditionalOnBean}은 대상 Bean을 등록하는 AutoConfiguration이 먼저 처리된 뒤에만
 * 참이 됩니다. CpfAttachmentStoragePort를 등록하는 AutoConfiguration 다음에 평가되도록 순서를
 * 명시합니다. 순서를 생략하면 Port가 있어도 조건이 거짓이 될 수 있습니다.</p>
 */
@AutoConfiguration(after = CpfAttachmentAutoConfiguration.class)
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
