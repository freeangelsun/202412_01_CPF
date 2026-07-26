package com.cpf.core.api.security.secret;

/** Vault/KMS/환경변수 등 Secret 저장소를 추상화하는 공개 Provider 계약. */
public interface CpfSecretProvider {
    String providerId();
    CpfSecretMetadata metadata(CpfSecretReference reference);
    CpfSecretValue resolve(CpfSecretReference reference);
}
