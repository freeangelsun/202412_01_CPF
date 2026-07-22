package com.cpf.core.config;

import com.cpf.core.common.attachment.CpfAttachmentStoragePort;
import com.cpf.core.common.attachment.LocalCpfAttachmentStorageAdapter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** CPF 첨부파일 저장 port의 기본 로컬 adapter를 구성합니다. */
@AutoConfiguration
public class CpfAttachmentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CpfAttachmentStoragePort.class)
    public CpfAttachmentStoragePort cpfAttachmentStoragePort(Environment environment) {
        String defaultRoot = Path.of(System.getProperty("java.io.tmpdir"), "cpf-attachments").toString();
        String configuredRoot = environment.getProperty("cpf.framework.attachment.root");
        if (environment.acceptsProfiles(Profiles.of("prod"))
                && (configuredRoot == null || configuredRoot.isBlank())) {
            throw new IllegalStateException("prod profile은 CPF_ATTACHMENT_ROOT 설정이 필요합니다.");
        }
        Path root = Path.of(configuredRoot == null || configuredRoot.isBlank() ? defaultRoot : configuredRoot);
        long maxBytes = environment.getProperty("cpf.framework.attachment.max-bytes", Long.class, 10_485_760L);
        String configured = environment.getProperty(
                "cpf.framework.attachment.allowed-extensions",
                "txt,csv,json,xml,pdf,png,jpg,jpeg,gif,zip");
        Set<String> extensions = Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());
        return new LocalCpfAttachmentStorageAdapter(root, maxBytes, extensions);
    }
}
