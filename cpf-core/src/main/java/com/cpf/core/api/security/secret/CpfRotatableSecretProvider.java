package com.cpf.core.api.security.secret;

/** Secret Rotation을 지원하는 Provider 확장 계약. */
public interface CpfRotatableSecretProvider extends CpfSecretProvider {
    CpfSecretMetadata rotate(CpfSecretReference reference, String reason, String actorId);
}
