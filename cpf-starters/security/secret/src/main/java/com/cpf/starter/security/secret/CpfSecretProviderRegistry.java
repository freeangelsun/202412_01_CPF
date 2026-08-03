package com.cpf.starter.security.secret;

import com.cpf.core.api.security.secret.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Provider가 없거나 중복되면 시작을 거부하는 Secret Provider 정본 Registry입니다. */
public final class CpfSecretProviderRegistry {
    private final Map<String, CpfSecretProvider> providers;
    public CpfSecretProviderRegistry(List<CpfSecretProvider> providers) {
        try {
            this.providers = providers.stream().collect(Collectors.toUnmodifiableMap(CpfSecretProvider::providerId, Function.identity()));
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Duplicate CPF SecretProvider providerId.", e);
        }
    }
    public CpfSecretValue resolve(CpfSecretReference reference) {
        CpfSecretProvider provider = providers.get(reference.provider());
        if (provider == null) throw new IllegalStateException("No approved SecretProvider for " + reference.provider());
        return provider.resolve(reference);
    }
    public CpfSecretMetadata metadata(CpfSecretReference reference) {
        CpfSecretProvider provider = providers.get(reference.provider());
        if (provider == null) throw new IllegalStateException("No approved SecretProvider for " + reference.provider());
        return provider.metadata(reference);
    }
}
