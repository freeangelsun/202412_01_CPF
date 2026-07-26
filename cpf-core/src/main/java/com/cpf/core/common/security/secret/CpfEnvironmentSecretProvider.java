package com.cpf.core.common.security.secret;

import com.cpf.core.api.security.secret.CpfSecretMetadata;
import com.cpf.core.api.security.secret.CpfSecretProvider;
import com.cpf.core.api.security.secret.CpfSecretReference;
import com.cpf.core.api.security.secret.CpfSecretValue;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Bootstrap/개발용 ENV Secret Provider.
 * 운영에서 Vault/KMS Adapter로 대체할 수 있으며 원문을 로그에 남기지 않습니다.
 */
@Component
public final class CpfEnvironmentSecretProvider implements CpfSecretProvider {
    @Override public String providerId() { return "ENV"; }

    @Override
    public CpfSecretMetadata metadata(CpfSecretReference reference) {
        ensure(reference);
        String value = System.getenv(reference.key());
        return new CpfSecretMetadata(reference, value == null ? "MISSING" : "PRESENT",
                null, null, false, Map.of("source", "environment"));
    }

    @Override
    public CpfSecretValue resolve(CpfSecretReference reference) {
        ensure(reference);
        String value = System.getenv(reference.key());
        if (value == null || value.isBlank()) throw new IllegalStateException("환경 Secret을 찾을 수 없습니다. key=" + reference.key());
        return new CpfSecretValue(value.toCharArray());
    }

    private void ensure(CpfSecretReference reference) {
        if (reference == null || !providerId().equalsIgnoreCase(reference.provider())) {
            throw new IllegalArgumentException("ENV Provider가 처리할 수 없는 Secret Reference입니다.");
        }
    }
}
