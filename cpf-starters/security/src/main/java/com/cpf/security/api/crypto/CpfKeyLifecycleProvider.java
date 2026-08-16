package com.cpf.security.api.crypto;
/** Key Rotation과 Revocation을 지원하는 선택적 KMS/HSM Lifecycle 확장 계약입니다. */
public interface CpfKeyLifecycleProvider extends CpfKeyProvider {
    CpfSigningKey rotate(String keyId);
    CpfSigningKey revoke(String keyId, String reason);
}
