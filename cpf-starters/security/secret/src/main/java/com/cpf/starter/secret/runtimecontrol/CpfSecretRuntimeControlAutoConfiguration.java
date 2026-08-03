package com.cpf.starter.secret.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.security.runtime.CpfCertificateReloadPort;
import com.cpf.core.api.security.runtime.CpfEncryptionKeyReloadPort;
import com.cpf.core.api.security.runtime.CpfJwtKeyReloadPort;
import com.cpf.core.api.security.runtime.CpfSecretReferenceReloadPort;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfSecretRuntimeControlAutoConfiguration {
    @Bean(name = "cpfCertificateRuntimeApplier")
    @ConditionalOnBean(CpfCertificateReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfCertificateRuntimeApplier")
    CpfRuntimeChangeApplier certificateRuntimeApplier(CpfCertificateReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier(
                "CERTIFICATE", port::reloadCertificates);
    }

    @Bean(name = "cpfSecretReferenceRuntimeApplier")
    @ConditionalOnBean(CpfSecretReferenceReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfSecretReferenceRuntimeApplier")
    CpfRuntimeChangeApplier secretReferenceRuntimeApplier(CpfSecretReferenceReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier(
                "SECRET_REFERENCE", port::reloadSecretReferences);
    }

    @Bean(name = "cpfJwtKeyRuntimeApplier")
    @ConditionalOnBean(CpfJwtKeyReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfJwtKeyRuntimeApplier")
    CpfRuntimeChangeApplier jwtKeyRuntimeApplier(CpfJwtKeyReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier("JWT_KEY", port::reloadJwtKeys);
    }

    @Bean(name = "cpfEncryptionKeyRuntimeApplier")
    @ConditionalOnBean(CpfEncryptionKeyReloadPort.class)
    @ConditionalOnMissingBean(name = "cpfEncryptionKeyRuntimeApplier")
    CpfRuntimeChangeApplier encryptionKeyRuntimeApplier(CpfEncryptionKeyReloadPort port) {
        return new CpfSecurityMaterialRuntimeApplier(
                "ENCRYPTION_KEY", port::reloadEncryptionKeys);
    }
}
